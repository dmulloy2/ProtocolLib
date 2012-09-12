package com.comphenix.protocol.injector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import net.minecraft.server.Packet;

import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;

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
			if (ObjectUtils.equals(entry.getValue(), packetID)) {
				return entry.getKey();
			}
		}
		
		throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
	}
}
