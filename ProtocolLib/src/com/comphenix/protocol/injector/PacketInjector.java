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

package com.comphenix.protocol.injector;

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;

/**
 * This class is responsible for adding or removing proxy objects that intercepts recieved packets.
 * 
 * @author Kristian
 */
class PacketInjector {

	// The "put" method that associates a packet ID with a packet class
	private static Method putMethod;
	private static Object intHashMap;
	
	// The packet filter manager
	private ListenerInvoker manager;
	
	// Allows us to determine the sender
	private PlayerInjectionHandler playerInjection;
	
	// Allows us to look up read packet injectors
	private Map<Integer, ReadPacketModifier> readModifier;
	
	// Class loader
	private ClassLoader classLoader;
			
	public PacketInjector(ClassLoader classLoader, ListenerInvoker manager, 
						  PlayerInjectionHandler playerInjection) throws IllegalAccessException {
		
		this.classLoader = classLoader;
		this.manager = manager;
		this.playerInjection = playerInjection;
		this.readModifier = new ConcurrentHashMap<Integer, ReadPacketModifier>();
		initialize();
	}
	
	/**
	 * Undo a packet cancel.
	 * @param id - the id of the packet.
	 * @param packet - packet to uncancel.
	 */
	public void undoCancel(Integer id, Packet packet) {
		ReadPacketModifier modifier = readModifier.get(id);
		
		// Cancelled packets are represented with NULL
		if (modifier != null && modifier.getOverride(packet) == null) {
			modifier.removeOverride(packet);
		}
	}
	
	private void initialize() throws IllegalAccessException {
		if (intHashMap == null) {
			// We're looking for the first static field with a Minecraft-object. This should be a IntHashMap.
			Field intHashMapField = FuzzyReflection.fromClass(Packet.class, true).getFieldByType(FuzzyReflection.MINECRAFT_OBJECT);
			
			try {
				intHashMap = FieldUtils.readField(intHashMapField, (Object) null, true);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException("Minecraft is incompatible.", e);
			}
			
			// Now, get the "put" method.
			putMethod = FuzzyReflection.fromObject(intHashMap).getMethodByParameters("put", int.class, Object.class);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean addPacketHandler(int packetID) {
		if (hasPacketHandler(packetID))
			return false;
		
		Enhancer ex = new Enhancer();
		
		// Unfortunately, we can't easily distinguish between these two functions:
		//   * Object lookup(int par1)
		//   * Object removeObject(int par1)
		
		// So, we'll use the classMapToInt registry instead.
		Map<Integer, Class> overwritten = MinecraftRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = MinecraftRegistry.getPreviousPackets();
		Map<Class, Integer> registry = MinecraftRegistry.getPacketToID();
		Class old = MinecraftRegistry.getPacketClassFromID(packetID);
		
		// Check for previous injections
		if (!old.getName().startsWith("net.minecraft.")) {
			throw new IllegalStateException("Packet " + packetID + " has already been injected.");
		}
		
		// Subclass the specific packet class
		ex.setSuperclass(old);
		ex.setCallbackType(ReadPacketModifier.class);
		ex.setUseCache(false);
		ex.setClassLoader(classLoader);
		Class proxy = ex.createClass();
		
		// Create the proxy handler
		ReadPacketModifier modifier = new ReadPacketModifier(packetID, this);
		readModifier.put(packetID, modifier);
		
		// Add a static reference
		Enhancer.registerStaticCallbacks(proxy, new Callback[] { modifier });
		
		try {
			// Override values
			putMethod.invoke(intHashMap, packetID, proxy);
			previous.put(packetID, old);
			registry.put(proxy, packetID);
			overwritten.put(packetID, proxy);
			return true;
			
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal argument.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access method.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception occured in IntHashMap.put.", e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public boolean removePacketHandler(int packetID) {
		if (!hasPacketHandler(packetID))
			return false;
		
		Map<Class, Integer> registry = MinecraftRegistry.getPacketToID();
		Map<Integer, Class> previous = MinecraftRegistry.getPreviousPackets();
		Map<Integer, Class> overwritten = MinecraftRegistry.getOverwrittenPackets();
		
		// Use the old class definition
		try {
			Class old = previous.get(packetID);
			Class proxy = MinecraftRegistry.getPacketClassFromID(packetID);
			
			putMethod.invoke(intHashMap, packetID, old);
			previous.remove(packetID);
			readModifier.remove(packetID);
			registry.remove(proxy);
			overwritten.remove(packetID);
			return true;
			
			// Handle some problems
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access method.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception occured in IntHashMap.put.", e);
		}
	}
	
	public boolean hasPacketHandler(int packetID) {
		return MinecraftRegistry.getPreviousPackets().containsKey(packetID);
	}
	
	public Set<Integer> getPacketHandlers() {
		return MinecraftRegistry.getPreviousPackets().keySet();
	}
	
	// Called from the ReadPacketModified monitor
	PacketEvent packetRecieved(PacketContainer packet, DataInputStream input) {
		
		Player client = playerInjection.getPlayerByConnection(input);
		return packetRecieved(packet, client);
	}
	
	/**
	 * Let the packet listeners process the given packet.
	 * @param packet - a packet to process.
	 * @param client - the client that sent the packet.
	 * @return The resulting packet event.
	 */
	public PacketEvent packetRecieved(PacketContainer packet, Player client) {
	
		PacketEvent event = PacketEvent.fromClient((Object) manager, packet, client);
		
		manager.invokePacketRecieving(event);
		return event;
	}
	
	@SuppressWarnings("rawtypes")
	public void cleanupAll() {
		Map<Integer, Class> overwritten = MinecraftRegistry.getOverwrittenPackets();
		Map<Integer, Class> previous = MinecraftRegistry.getPreviousPackets();
		
		// Remove every packet handler
		for (Integer id : previous.keySet()) {
			removePacketHandler(id);
		}
		
		overwritten.clear();
		previous.clear();
	}
}
