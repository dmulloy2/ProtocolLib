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

package com.comphenix.protocol.injector.player;

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Factory;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler.GamePhase;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;

abstract class PlayerInjector {
	
	// Cache previously retrieved fields
	protected static Field serverHandlerField;
	protected static Field proxyServerField;
	
	protected static Field networkManagerField;
	protected static Field inputField;
	protected static Field netHandlerField;
	
	// Whether or not we're using a proxy type
	private static boolean hasProxyType;
	
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
	protected ListenerInvoker invoker;
	
	// Previous data input
	protected DataInputStream cachedInput;
	
	// Handle errors
	protected Logger logger;

	public PlayerInjector(Logger logger, Player player, ListenerInvoker invoker) throws IllegalAccessException {
		this.logger = logger;
		this.player = player;
		this.invoker = invoker;
	}

	/**
	 * Retrieve the notch (NMS) entity player object.
	 * @return Notch player object.
	 */
	protected EntityPlayer getEntityPlayer() {
		CraftPlayer craft = (CraftPlayer) player;
		return craft.getHandle();
	}
	
	/**
	 * Initialize all fields for this player injector, if it hasn't already.
	 * @throws IllegalAccessException An error has occured.
	 */
	public void initialize() throws IllegalAccessException {
	
		EntityPlayer notchEntity = getEntityPlayer();
		
		if (!hasInitialized) {
			// Do this first, in case we encounter an exception
			hasInitialized = true;
			
			// Retrieve the server handler
			if (serverHandlerField == null) {
				serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType(".*NetServerHandler");
				proxyServerField = getProxyField(notchEntity, serverHandlerField);
			}
			
			// Yo dawg
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
	 * Retrieve whether or not the server handler is a proxy object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	protected boolean hasProxyServerHandler() {
		return hasProxyType;
	}
	
	private Field getProxyField(EntityPlayer notchEntity, Field serverField) {

		try {
			Object handler = FieldUtils.readField(serverHandlerField, notchEntity, true);
			
			// Is this a Minecraft hook?
			if (handler != null && !handler.getClass().getName().startsWith("net.minecraft.server")) {
				
				// This is our proxy object
				if (handler instanceof Factory)
					return null;
				
				hasProxyType = true;
				logger.log(Level.WARNING, "Detected server handler proxy type by another plugin. Conflict may occur!");
				
				// No? Is it a Proxy type?
				try {
					FuzzyReflection reflection = FuzzyReflection.fromObject(handler, true);
					
					// It might be
					return reflection.getFieldByType(".*NetServerHandler");
					
				} catch (RuntimeException e) {
					// Damn
				}
			}
			
		} catch (IllegalAccessException e) {
			logger.warning("Unable to load server handler from proxy type.");
		}

		// Nope, just go with it
		return null;
	}
	
	/**
	 * Retrieves the current net handler for this player.
	 * @return Current net handler.
	 * @throws IllegalAccessException Unable to find or retrieve net handler.
	 */
	protected Object getNetHandler() throws IllegalAccessException {
		
		// What a mess
		try {
			if (netHandlerField == null)
				netHandlerField = FuzzyReflection.fromClass(networkManager.getClass(), true).
									getFieldByType("net\\.minecraft\\.NetHandler");
		} catch (RuntimeException e1) {
			// Swallow it
		}
		
		// Second attempt
		if (netHandlerField == null) {
			try {
				// Well, that sucks. Try just Minecraft objects then.
				netHandlerField = FuzzyReflection.fromClass(networkManager.getClass(), true).
									 getFieldByType(FuzzyReflection.MINECRAFT_OBJECT);
				
			} catch (RuntimeException e2) {
				throw new IllegalAccessException("Cannot locate net handler. " + e2.getMessage());
			}
		}
		
		// Get the handler
		if (netHandler == null)
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
	 * Determine if this inject method can even be attempted.
	 * @return TRUE if can be attempted, though possibly with failure, FALSE otherwise.
	 */
	public abstract boolean canInject(GamePhase state);
	
	/**
	 * Retrieve the hook type this class represents.
	 * @return Hook type this class represents.
	 */
	public abstract PlayerInjectHooks getHookType();
	
	/**
	 * Invoked before a new listener is registered.
	 * <p>
	 * The player injector should throw an exception if this listener cannot be properly supplied with packet events. 
	 * @param listener - the listener that is about to be registered.
	 */
	public abstract void checkListener(PacketListener listener);
	
	/**
	 * Allows a packet to be recieved by the listeners.
	 * @param packet - packet to recieve.
	 * @return The given packet, or the packet replaced by the listeners.
	 */
	public Packet handlePacketRecieved(Packet packet) {
		// Get the packet ID too
		Integer id = invoker.getPacketID(packet);

		// Make sure we're listening
		if (id != null && hasListener(id)) {
			// A packet has been sent guys!
			PacketContainer container = new PacketContainer(id, packet);
			PacketEvent event = PacketEvent.fromServer(invoker, container, player);
			invoker.invokePacketSending(event);
			
			// Cancelling is pretty simple. Just ignore the packet.
			if (event.isCancelled())
				return null;
			
			// Right, remember to replace the packet again
			return event.getPacket().getHandle();
		}
		
		return packet;
	}
	
	/**
	 * Determine if the given injector is listening for this packet ID.
	 * @param packetID - packet ID to check.
	 * @return TRUE if it is, FALSE oterhwise.
	 */
	protected abstract boolean hasListener(int packetID);
	
	/**
	 * Retrieve the current player's input stream.
	 * @param cache - whether or not to cache the result of this method.
	 * @return The player's input stream.
	 */
	public DataInputStream getInputStream(boolean cache) {
		if (inputField == null)
			throw new IllegalStateException("Input field is NULL.");
		if (networkManager == null)
				throw new IllegalStateException("Network manager is NULL.");
		
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
