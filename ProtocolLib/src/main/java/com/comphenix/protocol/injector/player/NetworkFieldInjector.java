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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.Sets;

/**
 * Injection hook that overrides the packet queue lists in NetworkHandler.
 * 
 * @author Kristian
 */
class NetworkFieldInjector extends PlayerInjector {

	/**
	 * Marker interface that indicates a packet is fake and should not be processed.
	 * @author Kristian
	 */
	public interface FakePacket {
		// Nothing
	}
	
	// After commit 336a4e00668fd2518c41242755ed6b3bdc3b0e6c (Update CraftBukkit to Minecraft 1.4.4.), 
	// CraftBukkit stopped redirecting map chunk and map chunk bulk packets to a separate queue.
	// Thus, NetworkFieldInjector can safely handle every packet (though not perfectly - some packets
	// will be slightly processed).
	private MinecraftVersion safeVersion = new MinecraftVersion("1.4.4");
	
	// Packets to ignore
	private Set<Object> ignoredPackets = Sets.newSetFromMap(new ConcurrentHashMap<Object, Boolean>());
	
	// Overridden fields
	private List<VolatileField> overridenLists = new ArrayList<VolatileField>();
	
	// Sync field
	private static Field syncField;
	private Object syncObject;

	// Determine if we're listening
	private IntegerSet sendingFilters;

	public NetworkFieldInjector(ErrorReporter reporter, Player player, 
								ListenerInvoker manager, IntegerSet sendingFilters) throws IllegalAccessException {
		
		super(reporter, player, manager);
		this.sendingFilters = sendingFilters;
	}
	
	@Override
	protected boolean hasListener(int packetID) {
		return sendingFilters.contains(packetID);
	}
	
	@Override
	public synchronized void initialize(Object injectionSource)  throws IllegalAccessException {
		super.initialize(injectionSource);

		// Get the sync field as well
		if (hasInitialized) {
			if (syncField == null)
				syncField = FuzzyReflection.fromObject(networkManager, true).getFieldByType("java\\.lang\\.Object");
			syncObject = FieldUtils.readField(syncField, networkManager, true);
		}
	}

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException {
		if (networkManager != null) {
			try {
				if (!filtered) {
					ignoredPackets.add(packet);
				}
				if (marker != null) {
					queuedMarkers.put(packet, marker);
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
	
	@Override
	public UnsupportedListener checkListener(MinecraftVersion version, PacketListener listener) {
		if (version != null && version.compareTo(safeVersion) > 0) {
			return null;
			
		} else {
			@SuppressWarnings("deprecation")
			int[] unsupported = { Packets.Server.MAP_CHUNK, Packets.Server.MAP_CHUNK_BULK };
			
			// Unfortunately, we don't support chunk packets
			if (ListeningWhitelist.containsAny(listener.getSendingWhitelist(), unsupported)) {
				return new UnsupportedListener("The NETWORK_FIELD_INJECTOR hook doesn't support map chunk listeners.", unsupported);
			} else {
				return null;
			}
		}
	}
	
	@Override
	public void injectManager() {
		if (networkManager != null) {

			@SuppressWarnings("rawtypes")
			StructureModifier<List> list = networkModifier.withType(List.class);

			// Subclass both send queues
			for (Field field : list.getFields()) {
				VolatileField overwriter = new VolatileField(field, networkManager, true);
				
				@SuppressWarnings("unchecked")
				List<Object> minecraftList = (List<Object>) overwriter.getOldValue();
				
				synchronized(syncObject) {
					// The list we'll be inserting
					List<Object> hackedList = new InjectedArrayList(this, ignoredPackets);
					
					// Add every previously stored packet
					for (Object packet : minecraftList) {
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
	
	@SuppressWarnings("unchecked")
	protected void cleanHook() {
		// Clean up
		for (VolatileField overriden : overridenLists) {
			List<Object> minecraftList = (List<Object>) overriden.getOldValue();
			List<Object> hacketList = (List<Object>) overriden.getValue();
			
			if (minecraftList == hacketList) {
				return;
			}
	
			// Get a lock before we modify the list
			synchronized(syncObject) {
				try {
					// Copy over current packets
					for (Object packet : (List<Object>) overriden.getValue()) {
						minecraftList.add(packet);
					}
				} finally {
					overriden.revertValue();
				}
			}
		}
		overridenLists.clear();
	}

	@Override
	public void handleDisconnect() {
		// No need to do anything
	}
	
	@Override
	public boolean canInject(GamePhase phase) {
		// All phases should work
		return true;
	}

	@Override
	public PlayerInjectHooks getHookType() {
		return PlayerInjectHooks.NETWORK_HANDLER_FIELDS;
	}
}
