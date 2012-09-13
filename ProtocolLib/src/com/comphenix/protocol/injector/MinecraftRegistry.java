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
import java.util.Map;

import net.minecraft.server.Packet;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.base.Objects;

/**
 * Static registries in Minecraft.
 * 
 * @author Kristian
 */
@SuppressWarnings("rawtypes")
class MinecraftRegistry {

	// The packet class to packet ID translator
	private static Map<Class, Integer> packetToID;
	
	// New proxy values
	private static Map<Integer, Class> overwrittenPackets = new HashMap<Integer, Class>();
	
	// Vanilla packets
	private static Map<Integer, Class> previousValues = new HashMap<Integer, Class>();
	
	@SuppressWarnings({ "unchecked" })
	public static Map<Class, Integer> getPacketToID() {
		// Initialize it, if we haven't already
		if (packetToID == null) {
			try {
				Field packetsField = FuzzyReflection.fromClass(Packet.class, true).getFieldByType("java\\.util\\.Map");
				packetToID = (Map<Class, Integer>) FieldUtils.readStaticField(packetsField, true);
				
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to retrieve the packetClassToIdMap", e);
			}
		}
		
		return packetToID;
	}
	
	public static Map<Integer, Class> getOverwrittenPackets() {
		return overwrittenPackets;
	}
	
	public static Map<Integer, Class> getPreviousPackets() {
		return previousValues;
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
 	 * @param vanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 */
	public static Class getPacketClassFromID(int packetID, boolean forceVanilla) {
		
		Map<Integer, Class> lookup = forceVanilla ? previousValues : overwrittenPackets;
		
		// Optimized lookup
		if (lookup.containsKey(packetToID)) {
			return lookup.get(packetToID);
		}

		// Will most likely not be used
		for (Map.Entry<Class, Integer> entry : getPacketToID().entrySet()) {
			if (Objects.equal(entry.getValue(), packetID)) {
				return entry.getKey();
			}
		}
		
		throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
	}
}
