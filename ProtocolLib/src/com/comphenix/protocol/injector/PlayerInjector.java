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
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.google.common.collect.Sets;

class PlayerInjector {
	
	/**
	 * Marker interface that indicates a packet is fake and should not be processed.
	 * @author Kristian
	 */
	public interface FakePacket {
		// Nothing
	}

	// Cache previously retrieved fields
	private static Field serverHandlerField;
	private static Field networkManagerField;
	private static Field inputField;
	private static Field netHandlerField;
	
	// To add our injected array lists
	private static StructureModifier<Object> networkModifier;
	
	// And methods
	private static Method queueMethod;
	private static Method processMethod;
		
	private Player player;
	private boolean hasInitialized;
	
	// Reference to the player's network manager
	private Object networkManager;
	
	// Current net handler
	private Object netHandler;
	
	// Overridden fields
	private List<VolatileField> overridenLists = new ArrayList<VolatileField>();
	
	// Packets to ignore
	private Set<Packet> ignoredPackets = Sets.newSetFromMap(new ConcurrentHashMap<Packet, Boolean>());
	
	// The packet manager and filters
	private PacketFilterManager manager;
	private Set<Integer> sendingFilters;
	
	// Previous data input
	private DataInputStream cachedInput;

	public PlayerInjector(Player player, PacketFilterManager manager, Set<Integer> sendingFilters) throws IllegalAccessException {
		this.player = player;
		this.manager = manager;
		this.sendingFilters = sendingFilters;
		initialize();
	}

	private void initialize() throws IllegalAccessException {
	
		CraftPlayer craft = (CraftPlayer) player;
		EntityPlayer notchEntity = craft.getHandle();
		
		if (!hasInitialized) {
			// Do this first, in case we encounter an exception
			hasInitialized = true;
			
			// Retrieve the server handler
			if (serverHandlerField == null)
				serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType(".*NetServerHandler");
			Object serverHandler = FieldUtils.readField(serverHandlerField, notchEntity);
			
			// Next, get the network manager 
			if (networkManagerField == null) 
				networkManagerField = FuzzyReflection.fromObject(serverHandler).getFieldByType(".*NetworkManager");
			networkManager = FieldUtils.readField(networkManagerField, serverHandler);
			
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
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		
		if (networkManager != null) {
			try {
				if (!filtered) {
					ignoredPackets.add(packet);
				}
				
				// Note that invocation target exception is a wrapper for a checked exception
				queueMethod.invoke(networkManager, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access queue method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
		}
	}
	
	@SuppressWarnings("serial")
	public void injectManager() {
		
		if (networkManager != null) {

			@SuppressWarnings("rawtypes")
			StructureModifier<List> list = networkModifier.withType(List.class);

			// Subclass both send queues
			for (Field field : list.getFields()) {
				VolatileField overwriter = new VolatileField(field, networkManager, true);
				
				@SuppressWarnings("unchecked")
				List<Packet> minecraftList = (List<Packet>) overwriter.getOldValue();
				
				synchronized(minecraftList) {
					// The list we'll be inserting
					List<Packet> hackedList = new ArrayList<Packet>() {
						@Override
						public boolean add(Packet packet) {
	
							Packet result = null;
							
							// Check for fake packets and ignored packets
							if (packet instanceof FakePacket) {
								return true;
							} else if (ignoredPackets.contains(packet)) {
								ignoredPackets.remove(packet);
							} else {
								result = handlePacketRecieved(packet);
							}
							
							// A NULL packet indicate cancelling
							try {
								if (result != null) {
									super.add(result);
								} else {
									// We'll use the FakePacket marker instead of preventing the filters
									sendServerPacket(createNegativePacket(packet), true);
								}
								
								// Collection.add contract
								return true;
								
							} catch (InvocationTargetException e) {
								throw new RuntimeException("Reverting cancelled packet failed.", e.getTargetException());
							}
						}
					};
					
					// Add every previously stored packet
					for (Packet packet : minecraftList) {
						hackedList.add(packet);
					}
					
					// Don' keep stale packets around
					minecraftList.clear();
					overwriter.setValue(Collections.synchronizedList(hackedList));
				}
				
				overridenLists.add(overwriter);
			}
		}
	}
	
	/**
	 * Used by a hack that reverses the effect of a cancelled packet. Returns a packet
	 * whereby every int method's return value is inverted (a => -a).
	 * 
	 * @param source - packet to invert.
	 * @return The inverted packet.
	 */
	private Packet createNegativePacket(Packet source) {
		Enhancer ex = new Enhancer();
		Class<?> type = source.getClass();
		
		// We want to subtract the byte amount that were added to the running
		// total of outstanding packets. Otherwise, cancelling too many packets
		// might cause a "disconnect.overflow" error.
		//
		// We do that by constructing a special packet of the same type that returns 
		// a negative integer for all zero-parameter integer methods. This includes the
		// size() method, which is used by the queue method to count the number of
		// bytes to add.
		//
		// Essentially, we have:
		//
		//   public class NegativePacket extends [a packet] {
		//      @Override
		//      public int size() {
		//         return -super.size();
		//      }
		//   ect.
		//   }
		ex.setInterfaces(new Class[] { FakePacket.class } );
		ex.setUseCache(true);
		ex.setClassLoader(manager.getClassLoader());
		ex.setSuperclass(type);
		ex.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
				if (method.getReturnType().equals(int.class) && args.length == 0) {
					Integer result = (Integer) proxy.invokeSuper(obj, args);
					return -result;
				} else {
					return proxy.invokeSuper(obj, args);
				}
			}
		});
		
		return (Packet) ex.create();
	}
	
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
	
	@SuppressWarnings("unchecked")
	public void cleanupAll() {
		// Clean up
		for (VolatileField overriden : overridenLists) {
			List<Packet> minecraftList = (List<Packet>) overriden.getOldValue();
			List<Packet> hacketList = (List<Packet>) overriden.getValue();
			
			if (minecraftList == hacketList) {
				return;
			}
	
			// Get a lock before we modify the list
			synchronized(hacketList) {
				try {
					// Copy over current packets
					for (Packet packet : (List<Packet>) overriden.getValue()) {
						minecraftList.add(packet);
					}
				} finally {
					overriden.revertValue();
				}
			}
		}
		overridenLists.clear();
	}
}
