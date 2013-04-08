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

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.NoOp;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * This class is responsible for adding or removing proxy objects that intercepts recieved packets.
 * 
 * @author Kristian
 */
class ProxyPacketInjector implements PacketInjector {
	/**
	 * Represents a way to update the packet ID to class lookup table.
	 * @author Kristian
	 */
	private static interface PacketClassLookup {
		public void setLookup(int packetID, Class<?> clazz);
	}
	
	private static class IntHashMapLookup implements PacketClassLookup {
		// The "put" method that associates a packet ID with a packet class
		private Method putMethod;
		private Object intHashMap;
		
		public IntHashMapLookup() throws IllegalAccessException {
			initialize();
		}
		
		@Override
		public void setLookup(int packetID, Class<?> clazz) {
			try {
				putMethod.invoke(intHashMap, packetID, clazz);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Illegal argument.", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot access method.", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Exception occured in IntHashMap.put.", e);
			}
		}

		private void initialize() throws IllegalAccessException {
			if (intHashMap == null) {
				// We're looking for the first static field with a Minecraft-object. This should be a IntHashMap.
				Field intHashMapField = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true).
						getFieldByType(MinecraftReflection.getMinecraftObjectRegex());
				
				try {
					intHashMap = FieldUtils.readField(intHashMapField, (Object) null, true);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Minecraft is incompatible.", e);
				}
				
				// Now, get the "put" method.
				putMethod = FuzzyReflection.fromObject(intHashMap).
						getMethodByParameters("put", int.class, Object.class);
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
	private static FuzzyMethodContract readPacket = FuzzyMethodContract.newBuilder().
			returnTypeVoid().
			parameterExactType(DataInputStream.class).
			parameterCount(1).
			build();
	
	private static PacketClassLookup lookup;
	
	// The packet filter manager
	private ListenerInvoker manager;
	
	// Error reporter
	private ErrorReporter reporter;
	
	// Allows us to determine the sender
	private PlayerInjectionHandler playerInjection;
	
	// Class loader
	private ClassLoader classLoader;
	
	// Share callback filter
	private CallbackFilter filter;
			
	public ProxyPacketInjector(ClassLoader classLoader, ListenerInvoker manager, 
						  PlayerInjectionHandler playerInjection, ErrorReporter reporter) throws FieldAccessException {
		
		this.classLoader = classLoader;
		this.manager = manager;
		this.playerInjection = playerInjection;
		this.reporter = reporter;
		initialize();
	}
	
	/**
	 * Undo a packet cancel.
	 * @param id - the id of the packet.
	 * @param packet - packet to uncancel.
	 */
	@Override
	public void undoCancel(Integer id, Object packet) {
		// See if this packet has been cancelled before
		if (ReadPacketModifier.hasCancelled(packet)) {
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
	@SuppressWarnings("rawtypes")
	public boolean addPacketHandler(int packetID) {
		if (hasPacketHandler(packetID))
			return false;
		
		Enhancer ex = new Enhancer();
		
		// Unfortunately, we can't easily distinguish between these two functions:
		//   * Object lookup(int par1)
		//   * Object removeObject(int par1)
		
		// So, we'll use the classMapToInt registry instead.
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		Map<Class, Integer> registry = PacketRegistry.getPacketToID();
		Class old = PacketRegistry.getPacketClassFromID(packetID);
		
		// If this packet is not known
		if (old == null) {
			throw new IllegalStateException("Packet ID " + packetID + " is not a valid packet ID in this version.");
		}
		// Check for previous injections
		if (Factory.class.isAssignableFrom(old)) {
			throw new IllegalStateException("Packet " + packetID + " has already been injected.");
		}
		
		if (filter == null) {
			filter = new CallbackFilter() {
				@Override
				public int accept(Method method) {
					// Skip methods defined in Object
					if (method.getDeclaringClass().equals(Object.class))
						return 0;
					else if (readPacket.isMatch(MethodInfo.fromMethod(method), null))
						return 1;
					else
						return 2;
				}
			};
		}
		
		// Subclass the specific packet class
		ex.setSuperclass(old);
		ex.setCallbackFilter(filter);
		ex.setCallbackTypes(new Class<?>[] { NoOp.class, ReadPacketModifier.class, ReadPacketModifier.class });
		ex.setClassLoader(classLoader);
		Class proxy = ex.createClass();
		
		// Create the proxy handlers
		ReadPacketModifier modifierReadPacket = new ReadPacketModifier(packetID, this, reporter, true);
		ReadPacketModifier modifierRest = new ReadPacketModifier(packetID, this, reporter, false);

		// Add a static reference
		Enhancer.registerStaticCallbacks(proxy, new Callback[] { NoOp.INSTANCE, modifierReadPacket, modifierRest });
		
		// Override values
		previous.put(packetID, old);
		registry.put(proxy, packetID);
		overwritten.put(packetID, proxy);
		lookup.setLookup(packetID, proxy);
		return true;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public boolean removePacketHandler(int packetID) {
		if (!hasPacketHandler(packetID))
			return false;
		
		Map<Class, Integer> registry = PacketRegistry.getPacketToID();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		
		Class old = previous.get(packetID);
		Class proxy = PacketRegistry.getPacketClassFromID(packetID);
		
		lookup.setLookup(packetID, old);
		previous.remove(packetID);
		registry.remove(proxy);
		overwritten.remove(packetID);
		return true;
	}
	
	@Override
	public boolean hasPacketHandler(int packetID) {
		return PacketRegistry.getPreviousPackets().containsKey(packetID);
	}
	
	@Override
	public Set<Integer> getPacketHandlers() {
		return PacketRegistry.getPreviousPackets().keySet();
	}
	
	// Called from the ReadPacketModified monitor
	public PacketEvent packetRecieved(PacketContainer packet, DataInputStream input) {
		try {
			Player client = playerInjection.getPlayerByConnection(input);

			// Never invoke a event if we don't know where it's from
			if (client != null) {
				return packetRecieved(packet, client);
			} else {
				// Hack #2 - Caused by our server socket injector
				if (packet.getID() != Packets.Client.GET_INFO)
					System.out.println("[ProtocolLib] Unknown origin " + input + " for packet " + packet.getID());
				return null;
			}
			
		} catch (InterruptedException e) {
			// We will ignore this - it occurs when a player disconnects
			//reporter.reportDetailed(this, "Thread was interrupted.", e, packet, input);
			return null;
		} 
	}
	
	/**
	 * Let the packet listeners process the given packet.
	 * @param packet - a packet to process.
	 * @param client - the client that sent the packet.
	 * @return The resulting packet event.
	 */
	@Override
	public PacketEvent packetRecieved(PacketContainer packet, Player client) {
		PacketEvent event = PacketEvent.fromClient((Object) manager, packet, client);
		
		manager.invokePacketRecieving(event);
		return event;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public synchronized void cleanupAll() {
		Map<Integer, Class> overwritten = PacketRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = PacketRegistry.getPreviousPackets();
		
		// Remove every packet handler
		for (Integer id : previous.keySet().toArray(new Integer[0])) {
			removePacketHandler(id);
		}
		
		overwritten.clear();
		previous.clear();
	}
}
