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

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;

import javax.annotation.Nullable;

import com.google.common.base.Defaults;
import com.google.common.base.Function;
import com.google.common.base.Objects;

/**
 * Used to construct default instances of any type.
 * @author Kristian
 *
 */
public class DefaultInstances {

	private static List<Function<Class<?>, Object>> registered = new ArrayList<Function<Class<?>, Object>>();
	
	/**
	 * Default value for Strings.
	 */
	public final static String STRING_DEFAULT = "";
	
	/**
	 * The maximum height of the hierachy of creates types. Used to prevent cycles.
	 */
	private final static int MAXIMUM_RECURSION = 20;
	
	// Provide default registrations
	static {
		registered.add(new PrimitiveGenerator());
		registered.add(new CollectionGenerator());
	}
	
	/**
	 * Retrieves the default object providers used to generate default values.
	 * @return Table of object providers.
	 */
	public static List<Function<Class<?>, Object>> getRegistered() {
		return registered;
	}
	
	/**
	 * Retrieves a default instance or value that is assignable to this type.
	 * <p>
	 * This includes, but isn't limited too:
	 * <ul>
	 *   <li>Primitive types. Returns either zero or null.</li>
	 *   <li>Primitive wrappers.</li>
	 *   <li>String types. Returns an empty string.</li>
	 *   <li>Arrays. Returns a zero-length array of the same type.</li>
	 *   <li>Enums. Returns the first declared element.</li>
	 *   <li>Collection interfaces, such as List and Set. Returns the most appropriate empty container.</li>
	 *   <li>Any type with a public constructor that has parameters with defaults.</li>
	 *   </ul>
	 * </ul>
	 * @param type - the type to construct a default value.
	 * @return A default value/instance, or NULL if not possible.
	 */
	public static <T> T getDefault(Class<T> type) {
		return getDefaultInternal(type, 0);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getDefaultInternal(Class<T> type, int recursionLevel) {
		
		// Guard against recursion
		if (recursionLevel > MAXIMUM_RECURSION) {
			return null;
		}
		
		for (Function<Class<?>, Object> generator : registered) {
			Object value = generator.apply(type);
			
			if (value != null)
				return (T) value;
		}
		
		Constructor<?> minimum = null;
		int lastCount = Integer.MAX_VALUE;
		
		// Find the constructor with the fewest parameters
		for (Constructor<?> candidate : type.getConstructors()) {
			Class<?>[] types = candidate.getParameterTypes();
			
			// Note that we don't allow recursive types - that is, types that
			// require itself in the constructor.
			if (types.length < lastCount) {
				if (!contains(types, type)) {
					minimum = candidate;
					lastCount = types.length;
					
					// Don't loop again if we've already found the best possible constructor
					if (lastCount == 0)
						break;
				}
			}
		}
		
		// Create the type with this constructor using default values. This might fail, though.
		try {
			if (minimum != null) {
				Object[] params = new Object[lastCount];
				Class<?>[] types = minimum.getParameterTypes();
				
				// Fill out 
				for (int i = 0; i < lastCount; i++) {
					params[i] = getDefaultInternal(types[i], recursionLevel + 1);
				}
				
				return (T) minimum.newInstance(params);
			}
			
		} catch (Exception e) {
			// Nope, we couldn't create this type
		}
		
		// No suitable default value could be found
		return null;
	}
	
	private static <T> boolean contains(T[] elements, T elementToFind) {
		// Search for the given element in the array
		for (T element : elements) {
			if (Objects.equal(elementToFind, element))
				return true;
		}
		return false;
	}
	
	/**
	 * Provides constructors for primtive types, wrappers, arrays and strings.
	 * @author Kristian
	 */
	private static class PrimitiveGenerator implements Function<Class<?>, Object> {
		
		@Override
		public Object apply(@Nullable Class<?> type) {
			
			if (PrimitiveUtils.isPrimitive(type)) {
				return Defaults.defaultValue(type);
			} else if (PrimitiveUtils.isWrapperType(type)) {
				return Defaults.defaultValue(PrimitiveUtils.unwrap(type));
			} else if (type.isArray()) {
				Class<?> arrayType = type.getComponentType();
				return Array.newInstance(arrayType, 0);
			} else if (type.isEnum()) {
				Object[] values = type.getEnumConstants();
				if (values != null && values.length > 0)
					return values[0];
			} else if (type.equals(String.class)) {
				return STRING_DEFAULT;
			} 
			
			// Cannot handle this type
			return null;
		}	
	}
	
	/**
	 * Provides simple constructors for collection interfaces.
	 * @author Kristian
	 */
	private static class CollectionGenerator implements Function<Class<?>, Object> {

		@Override
		public Object apply(@Nullable Class<?> type) {
			// Standard collection types
			if (type.isInterface()) {
				if (type.equals(Collection.class) || type.equals(List.class))
					return new ArrayList<Object>();
				else if (type.equals(Set.class))
					return new HashSet<Object>();
				else if (type.equals(Map.class))
					return new HashMap<Object, Object>();
				else if (type.equals(SortedSet.class))
					return new TreeSet<Object>();
				else if (type.equals(SortedMap.class))
					return new TreeMap<Object, Object>();
				else if (type.equals(Queue.class))
					return new LinkedList<Object>();
			}
			
			// Cannot provide an instance
			return null;
		}
	}
}
