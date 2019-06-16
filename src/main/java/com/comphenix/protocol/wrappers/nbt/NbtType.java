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

package com.comphenix.protocol.wrappers.nbt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.primitives.Primitives;

/**
 * Represents all the element types 
 * 
 * @author Kristian
 */
public enum NbtType {
	/**
	 * Used to mark the end of compound tags. CANNOT be constructed.
	 */
	TAG_END(0, Void.class),
	
	/**
	 * A signed 1 byte integral type. Sometimes used for booleans.
	 */
	TAG_BYTE(1, byte.class),
	
	/**
	 * A signed 2 byte integral type.
	 */
	TAG_SHORT(2, short.class),
	
	/**
	 * A signed 4 byte integral type.
	 */
	TAG_INT(3, int.class),
	
	/**
	 * A signed 8 byte integral type.
	 */
	TAG_LONG(4, long.class),
	
	/**
	 * A signed 4 byte floating point type.
	 */
	TAG_FLOAT(5, float.class),
	
	/**
	 * A signed 8 byte floating point type.
	 */
	TAG_DOUBLE(6, double.class),
	
	/**
	 * An array of bytes.
	 */
	TAG_BYTE_ARRAY(7, byte[].class),
	
	/**
	 * An array of TAG_Int's payloads..
	 */
	TAG_INT_ARRAY(11, int[].class),
	
	/**
	 * A UTF-8 string
	 */
	TAG_STRING(8, String.class),
	
	/**
	 * A list of tag payloads, without repeated tag IDs or any tag names.
	 */
	TAG_LIST(9, List.class),
	
	/**
	 * A list of fully formed tags, including their IDs, names, and payloads. No two tags may have the same name.
	 */
	TAG_COMPOUND(10, Map.class);
	
	private int rawID;
	private Class<?> valueType;
	
	// Used to lookup a specified NBT
	private static NbtType[] lookup;
	
	// Lookup NBT by class
	private static Map<Class<?>, NbtType> classLookup;
	
	static {
		NbtType[] values = values();
		lookup = new NbtType[values.length];
		classLookup = new HashMap<Class<?>, NbtType>();
		
		// Initialize lookup tables
		for (NbtType type : values) {
			lookup[type.getRawID()] = type;
			classLookup.put(type.getValueType(), type);
			
			// Add a wrapper type
			if (type.getValueType().isPrimitive()) {
				classLookup.put(Primitives.wrap(type.getValueType()), type);
			}
		}
		
		// Additional lookup
		classLookup.put(NbtList.class, TAG_LIST);
		classLookup.put(NbtCompound.class, TAG_COMPOUND);
	}
	
	private NbtType(int rawID, Class<?> valueType) {
		this.rawID = rawID;
		this.valueType = valueType;
	}
	
	/**
	 * Determine if the given NBT can store multiple children NBT tags.
	 * @return TRUE if this is a composite NBT tag, FALSE otherwise.
	 */
	public boolean isComposite() {
		return this == TAG_COMPOUND || this == TAG_LIST;
	}
	
	/**
	 * Retrieves the raw unique integer that identifies the type of the parent NBT element.
	 * @return Integer that uniquely identifying the type.
	 */
	public int getRawID() {
		return rawID;
	}
	
	/**
	 * Retrieves the type of the value stored in the NBT element.
	 * @return Type of the stored value.
	 */
	public Class<?> getValueType() {
		return valueType;
	}
	
	/**
	 * Retrieve an NBT type from a given raw ID.
	 * @param rawID - the raw ID to lookup.
	 * @return The associated NBT value.
	 */
	public static NbtType getTypeFromID(int rawID) {
		if (rawID < 0 || rawID >= lookup.length)
			throw new IllegalArgumentException("Unrecognized raw ID " + rawID);
		return lookup[rawID];
	}
	
	/**
	 * Retrieve an NBT type from the given Java class.
	 * @param clazz - type of the value the NBT type can contain.
	 * @return The NBT type.
	 * @throws IllegalArgumentException If this class type cannot be represented by NBT tags.
	 */
	public static NbtType getTypeFromClass(Class<?> clazz) {
		NbtType result = classLookup.get(clazz);
		
		// Try to lookup this value
		if (result != null) {
			return result;
		} else { 
			// Look for interfaces
			for (Class<?> implemented : clazz.getInterfaces()) {
				if (classLookup.containsKey(implemented))
					return classLookup.get(implemented);
			}
			
			throw new IllegalArgumentException("No NBT tag can represent a " + clazz);
		}
	}
}
