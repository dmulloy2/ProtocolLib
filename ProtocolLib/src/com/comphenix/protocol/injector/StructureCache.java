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
			result = new StructureModifier<Object>(
					MinecraftRegistry.getPacketClassFromID(id, true), Packet.class, true);
			
			structureModifiers.put(id, result);
		}
		
		return result;
	}
}
