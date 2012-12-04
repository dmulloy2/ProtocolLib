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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Factory;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * Static registries in Minecraft.
 * 
 * @author Kristian
 */
@SuppressWarnings("rawtypes")
class MinecraftRegistry {

	// Fuzzy reflection
	private static FuzzyReflection packetRegistry;
	
	// The packet class to packet ID translator
	private static Map<Class, Integer> packetToID;
	
	// Whether or not certain packets are sent by the client or the server
	private static Set<Integer> serverPackets;
	private static Set<Integer> clientPackets;
	
	// New proxy values
	private static Map<Integer, Class> overwrittenPackets = new HashMap<Integer, Class>();
	
	// Vanilla packets
	private static Map<Integer, Class> previousValues = new HashMap<Integer, Class>();
	
	@SuppressWarnings({ "unchecked" })
	public static Map<Class, Integer> getPacketToID() {
		// Initialize it, if we haven't already
		if (packetToID == null) {
			try {
				Field packetsField = getPacketRegistry().getFieldByType("packetsField", Map.class);
				packetToID = (Map<Class, Integer>) FieldUtils.readStaticField(packetsField, true);
				
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to retrieve the packetClassToIdMap", e);
			}
		}
		
		return packetToID;
	}
	
	/**
	 * Retrieve the cached fuzzy reflection instance allowing access to the packet registry.
	 * @return Reflected packet registry.
	 */ 
	private static FuzzyReflection getPacketRegistry() {
		if (packetRegistry == null)
			packetRegistry = FuzzyReflection.fromClass(Packet.class, true);
		return packetRegistry;
	}
	
	/**
	 * Retrieve the injected proxy classes handlig each packet ID.
	 * @return Injected classes.
	 */
	public static Map<Integer, Class> getOverwrittenPackets() {
		return overwrittenPackets;
	}
	
	/**
	 * Retrieve the vanilla classes handling each packet ID.
	 * @return Vanilla classes.
	 */
	public static Map<Integer, Class> getPreviousPackets() {
		return previousValues;
	}
	
	/**
	 * Retrieve every known and supported server packet.
	 * @return An immutable set of every known server packet.
	 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
	 */
	public static Set<Integer> getServerPackets() throws FieldAccessException {
		initializeSets();
		return serverPackets;
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	public static Set<Integer> getClientPackets() throws FieldAccessException {
		initializeSets();
		return clientPackets;
	}
	
	@SuppressWarnings("unchecked")
	private static void initializeSets() throws FieldAccessException {
		if (serverPackets == null || clientPackets == null) {
			List<Field> sets = getPacketRegistry().getFieldListByType(Set.class);
			
			try {
				if (sets.size() > 1) {
					serverPackets = (Set<Integer>) FieldUtils.readStaticField(sets.get(0), true);
					clientPackets = (Set<Integer>) FieldUtils.readStaticField(sets.get(1), true);
					
					// Impossible
					if (serverPackets == null || clientPackets == null)
						throw new FieldAccessException("Packet sets are in an illegal state.");
					
					// NEVER allow callers to modify the underlying sets
					serverPackets = ImmutableSet.copyOf(serverPackets);
					clientPackets = ImmutableSet.copyOf(clientPackets);
					
				} else {
					throw new FieldAccessException("Cannot retrieve packet client/server sets.");
				}
				
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot access field.", e);
			}
		}
	}
	
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * @param packetID - the packet ID.
	 * @return The associated class.
	 */
	public static Class getPacketClassFromID(int packetID) {
		return getPacketClassFromID(packetID, false);
	}
	
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * @param packetID - the packet ID.
 	 * @param forceVanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 */
	public static Class getPacketClassFromID(int packetID, boolean forceVanilla) {
		
		Map<Integer, Class> lookup = forceVanilla ? previousValues : overwrittenPackets;
		
		// Optimized lookup
		if (lookup.containsKey(packetID)) {
			return removeEnhancer(lookup.get(packetID), forceVanilla);
		}

		// Will most likely not be used
		for (Map.Entry<Class, Integer> entry : getPacketToID().entrySet()) {
			if (Objects.equal(entry.getValue(), packetID)) {
				// Attempt to get the vanilla class here too
				if (!forceVanilla || entry.getKey().getName().startsWith("net.minecraft.server"))
					return removeEnhancer(entry.getKey(), forceVanilla);
			}
		}
		
		throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
	}
	
	/**
	 * Find the first superclass that is not a CBLib proxy object.
	 * @param clazz - the class whose hierachy we're going to search through.
	 * @param remove - whether or not to skip enhanced (proxy) classes.
	 * @return If remove is TRUE, the first superclass that is not a proxy.
	 */
	private static Class removeEnhancer(Class clazz, boolean remove) {
		if (remove) {
			// Get the underlying vanilla class
			while (Factory.class.isAssignableFrom(clazz) && !clazz.equals(Object.class)) {
				clazz = clazz.getSuperclass();
			}
		}
		
		return clazz;
	}
}
