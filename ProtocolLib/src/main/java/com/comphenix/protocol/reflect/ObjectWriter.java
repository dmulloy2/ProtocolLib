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

package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.comphenix.protocol.injector.StructureCache;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Can copy an object field by field.
 * 
 * @author Kristian
 */
public class ObjectWriter {
	// Cache structure modifiers
	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Class, StructureModifier<Object>> cache =
			new ConcurrentHashMap<Class, StructureModifier<Object>>();
	
	/**
	 * Retrieve a usable structure modifier for the given object type.
	 * <p>
	 * Will attempt to reuse any other structure modifiers we have cached.
	 * @param type - the type of the object we are modifying.
	 * @return A structure modifier for the given type.
	 */
	private StructureModifier<Object> getModifier(Class<?> type) {
		Class<?> packetClass = MinecraftReflection.getPacketClass();
		
		// Handle subclasses of the packet class with our custom structure cache
		if (!type.equals(packetClass) && packetClass.isAssignableFrom(type)) {
			// Delegate to our already existing registry of structure modifiers
			return StructureCache.getStructure(type);
		}
		
		StructureModifier<Object> modifier = cache.get(type);
		
		// Create the structure modifier if we haven't already
		if (modifier == null) {
			StructureModifier<Object> value = new StructureModifier<Object>(type, null, false);
			modifier = cache.putIfAbsent(type, value);
			
			if (modifier == null)
				modifier = value;
		}
		
		// And we're done
		return modifier;
	}
	
	/**
	 * Copy every field in object A to object B. Each value is copied directly, and is not cloned.
	 * <p>
	 * The two objects must have the same number of fields of the same type.
	 * @param source - fields to copy.
	 * @param destination - fields to copy to.
	 * @param commonType - type containing each field to copy.
	 */
	public void copyTo(Object source, Object destination, Class<?> commonType) {
		// Note that we indicate that public fields will be copied the first time around
		copyToInternal(source, destination, commonType, true);
	}

	/**
	 * Called for every non-static field that will be copied.
	 * @param modifierSource - modifier for the original object.
	 * @param modifierDest - modifier for the new cloned object.
	 * @param fieldIndex - the current field index.
	 */
	protected void transformField(StructureModifier<Object> modifierSource, StructureModifier<Object> modifierDest, int fieldIndex) {
		Object value = modifierSource.read(fieldIndex);
		modifierDest.write(fieldIndex, value);
	}
	
	// Internal method that will actually implement the recursion
	private void copyToInternal(Object source, Object destination, Class<?> commonType, boolean copyPublic) {
		if (source == null)
			throw new IllegalArgumentException("Source cannot be NULL");
		if (destination == null)
			throw new IllegalArgumentException("Destination cannot be NULL");
		
		StructureModifier<Object> modifier = getModifier(commonType);
		
		// Add target
		StructureModifier<Object> modifierSource = modifier.withTarget(source);
		StructureModifier<Object> modifierDest = modifier.withTarget(destination);
		
		// Copy every field
		try {
			for (int i = 0; i < modifierSource.size(); i++) {
				Field field = modifierSource.getField(i);
				int mod = field.getModifiers();
				
				// Skip static fields. We also get the "public" fields fairly often, so we'll skip that.
				if (!Modifier.isStatic(mod) && (!Modifier.isPublic(mod) || copyPublic)) {
					transformField(modifierSource, modifierDest, i);
				}
			}
			
			// Copy private fields underneath
			Class<?> superclass = commonType.getSuperclass();
			
			if (superclass != null && !superclass.equals(Object.class)) {
				copyToInternal(source, destination, superclass, false);
			}
			
		} catch (FieldAccessException e) {
			throw new RuntimeException("Unable to copy fields from " + commonType.getName(), e);
		}
	}
}
