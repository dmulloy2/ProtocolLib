/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector.packet;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.NoOp;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedIntHashMap;

/**
 * This class is responsible for adding or removing proxy objects that intercepts received packets.
 * 
 * @author Kristian
 */
class ProxyPacketInjector implements PacketInjector {
	public static final ReportType REPORT_CANNOT_FIND_READ_PACKET_METHOD = new ReportType("Cannot find read packet method for ID %s.");
	public static final ReportType REPORT_UNKNOWN_ORIGIN_FOR_PACKET = new ReportType("Timeout: Unknown origin %s for packet %s. Are you using GamePhase.LOGIN?");
	
	/**
	 * Represents a way to update the packet ID to class lookup table.
	 * @author Kristian
	 */
	private static interface PacketClassLookup {
		public void setLookup(int packetID, Class<?> clazz);
	}
	
	private static class IntHashMapLookup implements PacketClassLookup {
		private WrappedIntHashMap intHashMap;
		
		public IntHashMapLookup() throws IllegalAccessException {
			initialize();
		}
		
		@Override
		public void setLookup(int packetID, Class<?> clazz) {
			intHashMap.put(packetID, clazz);
		}

		private void initialize() throws IllegalAccessException {
			if (intHashMap == null) {
				// We're looking for the first static field with a Minecraft-object. This should be a IntHashMap.
				Field intHashMapField = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).
						getFieldByType("packetIdMap", MinecraftReflection.getIntHashMapClass());
				
				try {
					intHashMap = WrappedIntHashMap.fromHandle(
						FieldUtils.readField(intHashMapField, (Object) null, true));
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Minecraft is incompatible.", e);
				}
			}
		}
	}
	
	private static class ArrayLookup implements PacketClassLookup {
		private Class<?>[] array;
		
		public ArrayLookup() throws IllegalAccessException {
			initialize();
		}
		
		@Override
		public void setLookup(int packetID, Class<?> clazz) {
			array[packetID] = clazz;
		}

		private void initialize() throws IllegalAccessException {
			FuzzyReflection reflection = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass());
			
			// Is there a Class array with 256 elements instead?
			for (Field field : reflection.getFieldListByType(Class[].class)) {
				Class<?>[] test = (Class<?>[]) FieldUtils.readField(field, (Object)null);
				
				if (test.length == 256) {
					array = test;
					return;
				}
			}
			throw new IllegalArgumentException(
					"Unable to find an array with the type " + Class[].class + 
					" in " + MinecraftReflection.getPacketClass());
		}
	}
	
	/**
	 * Matches the readPacketData(DataInputStream) method in Packet.
	 */
	private static FuzzyMethodContract READ_PACKET = FuzzyMethodContract.newBuilder().
			returnTypeVoid().
			parameterDerivedOf(DataInput.class).
			parameterCount(1).
			build();
	
	private static PacketClassLookup lookup;
	
	// The packet filter manager
	private ListenerInvoker manager;
	
	// Error reporter
	private ErrorReporter reporter;
	
	// Allows us to determine the sender
	private PlayerInjectionHandler playerInjection;
	
	// Share callback filter
	private CallbackFilter filter;
	
	// Determine if the read packet method was found
	private boolean readPacketIntercepted = false;
	
	public ProxyPacketInjector(ListenerInvoker manager, PlayerInjectionHandler playerInjection, 
							   ErrorReporter reporter) throws FieldAccessException {
		
		this.manager = manager;
		this.playerInjection = playerInjection;
		this.reporter = reporter;
		initialize();
	}
	
	@Override
	public boolean isCancelled(Object packet) {
		return ReadPacketModifier.isCancelled(packet);
	}
	
	@Override
	public void setCancelled(Object packet, boolean cancelled) {
		if (cancelled) {
			ReadPacketModifier.setOverride(packet, null);
		} else {
			ReadPacketModifier.removeOverride(packet);
		}
	}
	
	private void initialize() throws FieldAccessException {
		if (lookup == null) {
			try {
				lookup = new IntHashMapLookup();
			} catch (Exception e1) {
				
				try { 
					lookup = new ArrayLookup();
				} catch (Exception e2) {
					// Wow
					throw new FieldAccessException(e1.getMessage() + ". Workaround failed too.", e2);
				}
			}
			
			// Should work fine now
		}
	}
	
	@Override
	public void inputBuffersChanged(Set<PacketType> set) {
		// No need to do anything
	}
	
	@Override
	@SuppressWarnings({"rawtypes", "deprecation"})
	public boolean addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		final int packetID = type.getLegacyId();
		
		if (hasPacketHandler(type))
			return false;
		
		Enhancer ex = EnhancerFactory.getInstance().createEnhancer();
		
		// Unfortunately, we can't easily distinguish between these two functions:
		//   * Object lookup(int par1)
		//   * Object removeObject(int par1)
		
		// So, we'll use the classMapToInt registry instead.
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		Map<Class, Integer> registry = PacketRegistry.getPacketToID();
		Class old = PacketRegistry.getPacketClassFromType(type);
		
		// If this packet is not known
		if (old == null) {
			throw new IllegalStateException("Packet ID " + type + " is not a valid packet type in this version.");
		}
		// Check for previous injections
		if (Factory.class.isAssignableFrom(old)) {
			throw new IllegalStateException("Packet " + type + " has already been injected.");
		}
		
		if (filter == null) {
			readPacketIntercepted = false;
			
			filter = new CallbackFilter() {
				@Override
				public int accept(Method method) {
					// Skip methods defined in Object
					if (method.getDeclaringClass().equals(Object.class)) {
						return 0;
					} else if (READ_PACKET.isMatch(MethodInfo.fromMethod(method), null)) {
						readPacketIntercepted = true;
						return 1;
					} else {
						return 2;
					}
				}
			};
		}
		
		// Subclass the specific packet class
		ex.setSuperclass(old);
		ex.setCallbackFilter(filter);
		ex.setCallbackTypes(new Class<?>[] { NoOp.class, ReadPacketModifier.class, ReadPacketModifier.class });
		Class proxy = ex.createClass();
		
		// Create the proxy handlers
		ReadPacketModifier modifierReadPacket = new ReadPacketModifier(packetID, this, reporter, true);
		ReadPacketModifier modifierRest = new ReadPacketModifier(packetID, this, reporter, false);

		// Add a static reference
		Enhancer.registerStaticCallbacks(proxy, new Callback[] { NoOp.INSTANCE, modifierReadPacket, modifierRest });
		
		// Check that we found the read method
		if (!readPacketIntercepted) {
			reporter.reportWarning(this, 
				Report.newBuilder(REPORT_CANNOT_FIND_READ_PACKET_METHOD).messageParam(packetID));
		}
		
		// Override values
		previous.put(packetID, old);
		registry.put(proxy, packetID);
		overwritten.put(packetID, proxy);
		lookup.setLookup(packetID, proxy);
		return true;
	}
	
	@Override
	@SuppressWarnings({"rawtypes", "deprecation"})
	public boolean removePacketHandler(PacketType type) {
		final int packetID = type.getLegacyId();
		
		if (!hasPacketHandler(type))
			return false;
		
		Map<Class, Integer> registry = PacketRegistry.getPacketToID();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		
		Class old = previous.get(packetID);
		Class proxy = PacketRegistry.getPacketClassFromType(type);
		
		lookup.setLookup(packetID, old);
		previous.remove(packetID);
		registry.remove(proxy);
		overwritten.remove(packetID);
		return true;
	}
	
	/**
	 * Determine if the data a packet read must be buffered.
	 * @param packetId - the packet to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	@Deprecated
	public boolean requireInputBuffers(int packetId) {
		return manager.requireInputBuffer(packetId);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean hasPacketHandler(PacketType type) {
		return PacketRegistry.getPreviousPackets().containsKey(type.getLegacyId());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public Set<PacketType> getPacketHandlers() {
		return PacketRegistry.toPacketTypes(PacketRegistry.getPreviousPackets().keySet(), Sender.CLIENT);
	}
	
	// Called from the ReadPacketModified monitor
	public PacketEvent packetRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
		if (playerInjection.canRecievePackets()) {
			return playerInjection.handlePacketRecieved(packet, input, buffered);
		}
		
		try {
			Player client = playerInjection.getPlayerByConnection((DataInputStream) input);

			// Never invoke a event if we don't know where it's from
			if (client != null) {
				return packetRecieved(packet, client, buffered);
			} else {
				// The timeout elapsed!
				reporter.reportDetailed(this, Report.newBuilder(REPORT_UNKNOWN_ORIGIN_FOR_PACKET).messageParam(input, packet.getType()));
				return null;
			}
			
		} catch (InterruptedException e) {
			// We will ignore this - it occurs when a player disconnects
			//reporter.reportDetailed(this, "Thread was interrupted.", e, packet, input);
			return null;
		} 
	}
	
	@Override
	public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
		NetworkMarker marker = buffered != null ? new LegacyNetworkMarker(ConnectionSide.CLIENT_SIDE, buffered, packet.getType()) : null;
		PacketEvent event = PacketEvent.fromClient(manager, packet, marker, client);
		
		manager.invokePacketRecieving(event);
		return event;
	}
	
	@Override
	@SuppressWarnings({"rawtypes", "deprecation"})
	public synchronized void cleanupAll() {
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		
		// Remove every packet handler
		for (Integer id : previous.keySet().toArray(new Integer[0])) {
			removePacketHandler(PacketType.findLegacy(id, Sender.CLIENT));
			removePacketHandler(PacketType.findLegacy(id, Sender.SERVER));
		}
		
		overwritten.clear();
		previous.clear();
	}
}
