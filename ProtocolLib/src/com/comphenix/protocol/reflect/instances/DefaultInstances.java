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

package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.util.*;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Used to construct default instances of any type.
 * @author Kristian
 *
 */
public class DefaultInstances {

	/**
	 * Standard default instance provider.
	 */
	public static DefaultInstances DEFAULT = DefaultInstances.fromArray(
			PrimitiveGenerator.INSTANCE, CollectionGenerator.INSTANCE);
		
	/**
	 * The maximum height of the hierachy of creates types. Used to prevent cycles.
	 */
	private final static int MAXIMUM_RECURSION = 20;
	
	/**
	 * Ordered list of instance provider, from highest priority to lowest.
	 */
	private ImmutableList<InstanceProvider> registered;
	
	/**
	 * Construct a default instance generator using the given instance providers.
	 * @param registered - list of instance providers.
	 */
	public DefaultInstances(ImmutableList<InstanceProvider> registered) {
		this.registered = registered;
	}
	
	/**
	 * Construct a default instance generator using the given instance providers.
	 * @param instaceProviders - array of instance providers.
	 */
	public DefaultInstances(InstanceProvider... instaceProviders) {
		this(ImmutableList.copyOf(instaceProviders));
	}
	
	/**
	 * Construct a default instance generator using the given instance providers.
	 * @param instaceProviders - array of instance providers.
	 * @return An default instance generator.
	 */
	public static DefaultInstances fromArray(InstanceProvider... instaceProviders) {
		return new DefaultInstances(ImmutableList.copyOf(instaceProviders));
	}
	
	/**
	 * Retrieves a immutable list of every default object providers that generates instances.
	 * @return Table of instance providers.
	 */
	public ImmutableList<InstanceProvider> getRegistered() {
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
	public <T> T getDefault(Class<T> type) {
		return getDefaultInternal(type, registered, 0);
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
	 * @param providers - instance providers used during the 
	 * @return A default value/instance, or NULL if not possible.
	 */
	public <T> T getDefault(Class<T> type, List<InstanceProvider> providers) {
		return getDefaultInternal(type, providers, 0);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getDefaultInternal(Class<T> type, List<InstanceProvider> providers, int recursionLevel) {
		
		// Guard against recursion
		if (recursionLevel > MAXIMUM_RECURSION) {
			return null;
		}
		
		for (InstanceProvider generator : providers) {
			Object value = generator.create(type);
			
			if (value != null)
				return (T) value;
		}
		
		Constructor<T> minimum = null;
		int lastCount = Integer.MAX_VALUE;
		
		// Find the constructor with the fewest parameters
		for (Constructor<?> candidate : type.getConstructors()) {
			Class<?>[] types = candidate.getParameterTypes();
			
			// Note that we don't allow recursive types - that is, types that
			// require itself in the constructor.
			if (types.length < lastCount) {
				if (!contains(types, type)) {
					minimum = (Constructor<T>) candidate;
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
					params[i] = getDefaultInternal(types[i], providers, recursionLevel + 1);
				}
				
				return createInstance(type, minimum, types, params);
			}
			
		} catch (Exception e) {
			// Nope, we couldn't create this type
		}
		
		// No suitable default value could be found
		return null;
	}
	
	/**
	 * Used by the default instance provider to create a class from a given constructor. 
	 * The default method uses reflection.
	 * @param type - the type to create.
	 * @param constructor - the constructor to use.
	 * @param types - type of each parameter in order.
	 * @param params - value of each parameter in order.
	 * @return The constructed instance.
	 */
	protected <T> T createInstance(Class<T> type, Constructor<T> constructor, Class<?>[] types, Object[] params) {
		try {
			return (T) constructor.newInstance(params);
		} catch (Exception e) {
			// Cannot create it
			return null;
		}
	}
	
	// We avoid Apache's utility methods to stay backwards compatible
	protected <T> boolean contains(T[] elements, T elementToFind) {
		// Search for the given element in the array
		for (T element : elements) {
			if (Objects.equal(elementToFind, element))
				return true;
		}
		return false;
	}
}
