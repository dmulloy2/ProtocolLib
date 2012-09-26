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
import java.util.Set;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;

abstract class PlayerInjector {
	
	// Cache previously retrieved fields
	protected static Field serverHandlerField;
	protected static Field networkManagerField;
	protected static Field inputField;
	protected static Field netHandlerField;
	
	// To add our injected array lists
	protected static StructureModifier<Object> networkModifier;
	
	// And methods
	protected static Method queueMethod;
	protected static Method processMethod;
		
	protected Player player;
	protected boolean hasInitialized;
	
	// Reference to the player's network manager
	protected VolatileField networkManagerRef;
	protected VolatileField serverHandlerRef;
	protected Object networkManager;
	
	// Current net handler
	protected Object serverHandler;
	protected Object netHandler;
	
	// The packet manager and filters
	protected PacketFilterManager manager;
	protected Set<Integer> sendingFilters;
	
	// Previous data input
	protected DataInputStream cachedInput;

	public PlayerInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		this.player = player;
		this.manager = manager;
		this.sendingFilters = sendingFilters;
		initialize();
	}

	/**
	 * Retrieve the notch (NMS) entity player object.
	 * @return Notch player object.
	 */
	protected EntityPlayer getEntityPlayer() {
		CraftPlayer craft = (CraftPlayer) player;
		return craft.getHandle();
	}
	
	protected void initialize() throws IllegalAccessException {
	
		EntityPlayer notchEntity = getEntityPlayer();
		
		if (!hasInitialized) {
			// Do this first, in case we encounter an exception
			hasInitialized = true;
			
			// Retrieve the server handler
			if (serverHandlerField == null)
				serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType(".*NetServerHandler");
			serverHandlerRef = new VolatileField(serverHandlerField, notchEntity);
			serverHandler = serverHandlerRef.getValue();
			
			// Next, get the network manager 
			if (networkManagerField == null) 
				networkManagerField = FuzzyReflection.fromObject(serverHandler).getFieldByType(".*NetworkManager");
			networkManagerRef = new VolatileField(networkManagerField, serverHandler);
			networkManager = networkManagerRef.getValue();
			
			// Create the network manager modifier from the actual object type
			if (networkManager != null && networkModifier == null)
				networkModifier = new StructureModifier<Object>(networkManager.getClass(), null, false);
			
			// And the queue method
			if (queueMethod == null)
				queueMethod = FuzzyReflection.fromClass(networkManagerField.getType()).
								getMethodByParameters("queue", Packet.class );
			
			// And the data input stream that we'll use to identify a player
			if (inputField == null)
				inputField = FuzzyReflection.fromObject(networkManager, true).
								getFieldByType("java\\.io\\.DataInputStream");
		}
	}
	
	/**
	 * Retrieves the current net handler for this player.
	 * @return Current net handler.
	 * @throws IllegalAccessException Unable to find or retrieve net handler.
	 */
	private Object getNetHandler() throws IllegalAccessException {
		
		// What a mess
		try {
			if (netHandlerField == null)
				netHandlerField = FuzzyReflection.fromClass(networkManagerField.getType(), true).
									getFieldByType("net\\.minecraft\\.NetHandler");
		} catch (RuntimeException e1) {
			try {
				// Well, that sucks. Try just Minecraft objects then.
				netHandlerField = FuzzyReflection.fromClass(networkManagerField.getType(), true).
									 getFieldByType(FuzzyReflection.MINECRAFT_OBJECT);
				
			} catch (RuntimeException e2) {
				return new IllegalAccessException("Cannot locate net handler. " + e2.getMessage());
			}
		}
		
		// Get the handler
		if (netHandler != null)
			netHandler = FieldUtils.readField(netHandlerField, networkManager, true);
		return netHandler;
	}
	
	/**
	 * Processes the given packet as if it was transmitted by the current player.
	 * @param packet - packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Packet packet) throws IllegalAccessException, InvocationTargetException {
		
		Object netHandler = getNetHandler();
		
		// Get the process method
		if (processMethod == null) {
			try {
				processMethod = FuzzyReflection.fromClass(Packet.class).
						getMethodByParameters("processPacket", netHandlerField.getType());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Cannot locate process packet method: " + e.getMessage());
			}
		}
	
		// We're ready
		try {
			processMethod.invoke(packet, netHandler);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Method " + processMethod.getName() + " is not compatible.");
		} catch (InvocationTargetException e) {
			throw e;
		}
	}
	
	/**
	 * Send a packet to the client.
	 * @param packet - server packet to send.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @param InvocationTargetException If an error occured when sending the packet.
	 */
	public abstract void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException;
	
	/**
	 * Inject a hook to catch packets sent to the current player.
	 */
	public abstract void injectManager();
	
	/**
	 * Remove all hooks and modifications.
	 */
	public abstract void cleanupAll();
	
	/**
	 * Determine if we actually can inject.
	 * @return TRUE if this injector is compatible with the current CraftBukkit version, FALSE otherwise.
	 */
	public abstract boolean canInject();
	
	/**
	 * Allows a packet to be recieved by the listeners.
	 * @param packet - packet to recieve.
	 * @return The given packet, or the packet replaced by the listeners.
	 */
	Packet handlePacketRecieved(Packet packet) {
		// Get the packet ID too
		Integer id = MinecraftRegistry.getPacketToID().get(packet.getClass());

		// Make sure we're listening
		if (sendingFilters.contains(id)) {	
			// A packet has been sent guys!
			PacketContainer container = new PacketContainer(id, packet);
			PacketEvent event = PacketEvent.fromServer(manager, container, player);
			manager.invokePacketSending(event);
			
			// Cancelling is pretty simple. Just ignore the packet.
			if (event.isCancelled())
				return null;
			
			// Right, remember to replace the packet again
			return event.getPacket().getHandle();
		}
		
		return packet;
	}
	
	/**
	 * Retrieve the current player's input stream.
	 * @param cache - whether or not to cache the result of this method.
	 * @return The player's input stream.
	 */
	public DataInputStream getInputStream(boolean cache) {
		// Get the associated input stream
		try {
			if (cache && cachedInput != null)
				return cachedInput;
			
			// Save to cache
			cachedInput = (DataInputStream) FieldUtils.readField(inputField, networkManager, true);
			return cachedInput;
			
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to read input stream.", e);
		}
	}
}
