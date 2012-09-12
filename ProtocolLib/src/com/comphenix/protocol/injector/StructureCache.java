package com.comphenix.protocol.injector;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.Packet;

import com.comphenix.protocol.reflect.StructureModifier;

public class StructureCache {
	// Structure modifiers
	private static Map<Integer, StructureModifier<Object>> structureModifiers = new HashMap<Integer, StructureModifier<Object>>();
	
	/**
	 * Creates an empty Minecraft packet of the given ID.
	 * @param id - packet ID.
	 * @return Created packet.
	 */
	public static Packet newPacket(int id) {
		try {
			return (Packet) MinecraftRegistry.getPacketClassFromID(id, true).newInstance();
		} catch (InstantiationException e) {
			return null;
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Access denied.", e);
		}
	}
	
	/**
	 * Retrieve a cached structure modifier for the given packet id.
	 * @param id - packet ID.
	 * @return A structure modifier.
	 */
	public static StructureModifier<Object> getStructure(int id) {
		
		StructureModifier<Object> result = structureModifiers.get(id);
		
		// Use the vanilla class definition
		if (result == null) {
			result = new StructureModifier<Object>(MinecraftRegistry.getPacketClassFromID(id, true));
			structureModifiers.put(id, result);
		}
		
		return result;
	}
}
