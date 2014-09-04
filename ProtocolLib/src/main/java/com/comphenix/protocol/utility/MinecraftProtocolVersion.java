package com.comphenix.protocol.utility;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.Maps;

/**
 * A lookup of the associated protocol version of a given Minecraft server.
 * @author Kristian
 */
public class MinecraftProtocolVersion {
	private static final NavigableMap<MinecraftVersion, Integer> lookup = createLookup();
	
	private static NavigableMap<MinecraftVersion, Integer> createLookup() {
		TreeMap<MinecraftVersion, Integer> map = Maps.newTreeMap();
		
		// Source: http://wiki.vg/Protocol_version_numbers
		// Doesn't include pre-releases
		map.put(new MinecraftVersion(1, 0, 0), 22);
		map.put(new MinecraftVersion(1, 1, 0), 23);
		map.put(new MinecraftVersion(1, 2, 2), 28);
		map.put(new MinecraftVersion(1, 2, 4), 29);
		map.put(new MinecraftVersion(1, 3, 1), 39);
		map.put(new MinecraftVersion(1, 4, 2), 47);
		map.put(new MinecraftVersion(1, 4, 3), 48);
		map.put(new MinecraftVersion(1, 4, 4), 49);
		map.put(new MinecraftVersion(1, 4, 6), 51);
		map.put(new MinecraftVersion(1, 5, 0), 60);
		map.put(new MinecraftVersion(1, 5, 2), 61);
		map.put(new MinecraftVersion(1, 6, 0), 72);
		map.put(new MinecraftVersion(1, 6, 1), 73);
		map.put(new MinecraftVersion(1, 6, 2), 74);
		map.put(new MinecraftVersion(1, 6, 4), 78);
		
		// After Netty
		map.put(new MinecraftVersion(1, 7, 1), 4);
		map.put(new MinecraftVersion(1, 7, 6), 5);
		map.put(new MinecraftVersion(1, 8, 0), 47);
		
		// Unknown number
		map.put(new MinecraftVersion(1, 8, 1), Integer.MIN_VALUE);
		return map;
	}
	
	/**
	 * Retrieve the version of the Minecraft protocol for the current version of Minecraft.
	 * @return The version number.
	 */
	public static int getCurrentVersion() {
		return getVersion(ProtocolLibrary.getProtocolManager().getMinecraftVersion());
	}
	
	/**
	 * Retrieve the version of the Minecraft protocol for this version of Minecraft.
	 * @param version - the version.
	 * @return The version number.
	 */
	public static int getVersion(MinecraftVersion version) {
		Entry<MinecraftVersion, Integer> result = lookup.floorEntry(version);
		return result != null ? result.getValue() : Integer.MIN_VALUE;
	}
}
