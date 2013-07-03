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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.sf.cglib.proxy.Factory;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.TroveWrapper;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * Static packet registry in Minecraft.
 * 
 * @author Kristian
 */
@SuppressWarnings("rawtypes")
public class PacketRegistry {
	public static final ReportType REPORT_CANNOT_CORRECT_TROVE_MAP = new ReportType("Unable to correct no entry value.");
	
	public static final ReportType REPORT_INSUFFICIENT_SERVER_PACKETS = new ReportType("Too few server packets detected: %s");
	public static final ReportType REPORT_INSUFFICIENT_CLIENT_PACKETS = new ReportType("Too few client packets detected: %s");
	
	private static final int MIN_SERVER_PACKETS = 5;
	private static final int MIN_CLIENT_PACKETS = 5;

	// Fuzzy reflection
	private static FuzzyReflection packetRegistry;
	
	// The packet class to packet ID translator
	private static Map<Class, Integer> packetToID;
	
	// Packet IDs to classes, grouped by whether or not they're vanilla or custom defined
	private static Multimap<Integer, Class> customIdToPacket;
	private static Map<Integer, Class> vanillaIdToPacket;
	
	// Whether or not certain packets are sent by the client or the server
	private static ImmutableSet<Integer> serverPackets;
	private static ImmutableSet<Integer> clientPackets;
	
	// The underlying sets
	private static Set<Integer> serverPacketsRef;
	private static Set<Integer> clientPacketsRef;
	
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
			} catch (IllegalArgumentException e) {
				// Spigot 1.2.5 MCPC workaround
				try {
					packetToID = getSpigotWrapper();
				} catch (Exception e2) {
					// Very bad indeed
					throw new IllegalArgumentException(e.getMessage() + "; Spigot workaround failed.", e2);
				}
				
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to retrieve the packetClassToIdMap", e);
			}
			
			// Create the inverse maps
			customIdToPacket = InverseMaps.inverseMultimap(packetToID, new Predicate<Map.Entry<Class, Integer>>() {
				@Override
				public boolean apply(@Nullable Entry<Class, Integer> entry) {
					return !MinecraftReflection.isMinecraftClass(entry.getKey());
				}
			});
			
			// And the vanilla pack - here we assume a unique ID to class mapping
			vanillaIdToPacket = InverseMaps.inverseMap(packetToID, new Predicate<Map.Entry<Class, Integer>>() {
				@Override
				public boolean apply(@Nullable Entry<Class, Integer> entry) {
					return MinecraftReflection.isMinecraftClass(entry.getKey());
				}
			});
		}
		return packetToID;
	}
	
	private static Map<Class, Integer> getSpigotWrapper() throws IllegalAccessException {
		// If it talks like a duck, etc.
		// Perhaps it would be nice to have a proper duck typing library as well
		FuzzyClassContract mapLike = FuzzyClassContract.newBuilder().
				method(FuzzyMethodContract.newBuilder().
						nameExact("size").returnTypeExact(int.class)).
				method(FuzzyMethodContract.newBuilder().
						nameExact("put").parameterCount(2)).
				method(FuzzyMethodContract.newBuilder().
						nameExact("get").parameterCount(1)).
				build();
		
		Field packetsField = getPacketRegistry().getField(
				FuzzyFieldContract.newBuilder().typeMatches(mapLike).build());
		Object troveMap = FieldUtils.readStaticField(packetsField, true);
		
		// Check for stupid no_entry_values
		try {
			Field field = FieldUtils.getField(troveMap.getClass(), "no_entry_value", true);
			Integer value = (Integer) FieldUtils.readField(field, troveMap, true);
			
			if (value >= 0 && value < 256) {
				// Someone forgot to set the no entry value. Let's help them.
				FieldUtils.writeField(field, troveMap, -1);
			}
		} catch (IllegalArgumentException e) {
			// Whatever			
			ProtocolLibrary.getErrorReporter().reportWarning(PacketRegistry.class, 
					Report.newBuilder(REPORT_CANNOT_CORRECT_TROVE_MAP).error(e));
		}
		
		// We'll assume this a Trove map
		return TroveWrapper.getDecoratedMap(troveMap);
	}
	
	/**
	 * Retrieve the cached fuzzy reflection instance allowing access to the packet registry.
	 * @return Reflected packet registry.
	 */ 
	private static FuzzyReflection getPacketRegistry() {
		if (packetRegistry == null)
			packetRegistry = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true);
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
		
		// Sanity check. This is impossible!
		if (serverPackets != null && serverPackets.size() < MIN_SERVER_PACKETS) 
			throw new FieldAccessException("Server packet list is empty. Seems to be unsupported");
		return serverPackets;
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	public static Set<Integer> getClientPackets() throws FieldAccessException {
		initializeSets();
		
		// As above
		if (clientPackets != null && clientPackets.size() < MIN_CLIENT_PACKETS) 
			throw new FieldAccessException("Client packet list is empty. Seems to be unsupported");
		return clientPackets;
	}
	
	@SuppressWarnings("unchecked")
	private static void initializeSets() throws FieldAccessException {
		if (serverPacketsRef == null || clientPacketsRef == null) {
			List<Field> sets = getPacketRegistry().getFieldListByType(Set.class);
			
			try {
				if (sets.size() > 1) {
					serverPacketsRef = (Set<Integer>) FieldUtils.readStaticField(sets.get(0), true);
					clientPacketsRef = (Set<Integer>) FieldUtils.readStaticField(sets.get(1), true);
					
					// Impossible
					if (serverPacketsRef == null || clientPacketsRef == null)
						throw new FieldAccessException("Packet sets are in an illegal state.");
					
					// NEVER allow callers to modify the underlying sets
					serverPackets = ImmutableSet.copyOf(serverPacketsRef);
					clientPackets = ImmutableSet.copyOf(clientPacketsRef);
					
					// Check sizes
					if (serverPackets.size() < MIN_SERVER_PACKETS)
						ProtocolLibrary.getErrorReporter().reportWarning(
							PacketRegistry.class, Report.newBuilder(REPORT_INSUFFICIENT_SERVER_PACKETS).messageParam(serverPackets.size())
						);
					if (clientPackets.size() < MIN_CLIENT_PACKETS)
						ProtocolLibrary.getErrorReporter().reportWarning(
								PacketRegistry.class, Report.newBuilder(REPORT_INSUFFICIENT_CLIENT_PACKETS).messageParam(clientPackets.size())
							);
					
				} else {
					throw new FieldAccessException("Cannot retrieve packet client/server sets.");
				}
				
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot access field.", e);
			}
			
		} else {
			// Copy over again if it has changed
			if (serverPacketsRef != null && serverPacketsRef.size() != serverPackets.size())
				serverPackets = ImmutableSet.copyOf(serverPacketsRef);
			if (clientPacketsRef != null && clientPacketsRef.size() != clientPackets.size())
				clientPackets = ImmutableSet.copyOf(clientPacketsRef);
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
		Class<?> result = null;
		
		// Optimized lookup
		if (lookup.containsKey(packetID)) {
			return removeEnhancer(lookup.get(packetID), forceVanilla);
		}
		
		// Refresh lookup tables
		getPacketToID();

		// See if we can look for non-vanilla classes
		if (!forceVanilla) {
			result = Iterables.getFirst(customIdToPacket.get(packetID), null);
		}
		if (result == null) {
			result = vanillaIdToPacket.get(packetID);
		}
		
		// See if we got it
		if (result != null)
			return result;
		else
			throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
	}
	
	/**
	 * Retrieve the packet ID of a given packet.
	 * @param packet - the type of packet to check.
	 * @return The ID of the given packet.
	 * @throws IllegalArgumentException If this is not a valid packet.
	 */
	public static int getPacketID(Class<?> packet) {
		if (packet == null)
			throw new IllegalArgumentException("Packet type class cannot be NULL.");
		if (!MinecraftReflection.getPacketClass().isAssignableFrom(packet))
			throw new IllegalArgumentException("Type must be a packet.");
		
		// The registry contains both the overridden and original packets
		return getPacketToID().get(packet);
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
