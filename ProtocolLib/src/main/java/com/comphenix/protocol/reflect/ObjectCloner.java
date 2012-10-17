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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Can copy an object field by field.
 * 
 * @author Kristian
 */
public class ObjectCloner {

	// Cache structure modifiers
	@SuppressWarnings("rawtypes")
	private static ConcurrentMap<Class, StructureModifier<Object>> cache =
			new ConcurrentHashMap<Class, StructureModifier<Object>>();
	
	/**
	 * Copy every field in object A to object B.
	 * <p>
	 * The two objects must have the same number of fields of the same type.
	 * @param source - fields to copy.
	 * @param destination - fields to copy to.
	 * @param commonType - type containing each field to copy.
	 */
	public static void copyTo(Object source, Object destination, Class<?> commonType) {
		
		if (source == null)
			throw new IllegalArgumentException("Source cannot be NULL");
		if (destination == null)
			throw new IllegalArgumentException("Destination cannot be NULL");
		
		StructureModifier<Object> modifier = cache.get(commonType);

		// Create the structure modifier if we haven't already
		if (modifier == null) {
			StructureModifier<Object> value = new StructureModifier<Object>(commonType, null, false);
			modifier = cache.putIfAbsent(commonType, value);
			
			if (modifier == null)
				modifier = value;
		}
		
		// Add target
		StructureModifier<Object> modifierSource = modifier.withTarget(source);
		StructureModifier<Object> modifierDest = modifier.withTarget(destination);
		
		// Copy every field
		try {
			for (int i = 0; i < modifierSource.size(); i++) {
				Object value = modifierSource.read(i);
				modifierDest.write(i, value);
				
				// System.out.println(String.format("Writing value %s to %s", 
				//		value, modifier.getFields().get(i).getName()));
			}
			
			// Copy private fields underneath
			Class<?> superclass = commonType.getSuperclass();
			
			if (!superclass.equals(Object.class)) {
				copyTo(source, destination, superclass);
			}
			
		} catch (FieldAccessException e) {
			throw new RuntimeException("Unable to copy fields from " + commonType.getName(), e);
		}
	}
}
