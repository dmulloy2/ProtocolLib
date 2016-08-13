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
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import net.sf.cglib.proxy.Enhancer;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Used to construct default instances of any type.
 * @author Kristian
 *
 */
public class DefaultInstances implements InstanceProvider {

	/**
	 * Standard default instance provider.
	 */
	public static final DefaultInstances DEFAULT = DefaultInstances.fromArray(
			PrimitiveGenerator.INSTANCE, CollectionGenerator.INSTANCE);
		
	/**
	 * The maximum height of the hierachy of creates types. Used to prevent cycles.
	 */
	private int maximumRecursion = 20;
	
	/**
	 * Ordered list of instance provider, from highest priority to lowest.
	 */
	private ImmutableList<InstanceProvider> registered;
	
	/**
	 * Whether or not the constructor must be non-null.
	 */
	private boolean nonNull;
	
	/**
	 * Construct a default instance generator using the given instance providers.
	 * @param registered - list of instance providers.
	 */
	public DefaultInstances(ImmutableList<InstanceProvider> registered) {
		this.registered = registered;
	}
	
	/**
	 * Copy a given instance provider.
	 * @param other - instance provider to copy.
	 */
	public DefaultInstances(DefaultInstances other) {
		this.nonNull = other.nonNull;
		this.maximumRecursion = other.maximumRecursion;
		this.registered = other.registered;
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
	 * @param instanceProviders - array of instance providers.
	 * @return An default instance generator.
	 */
	public static DefaultInstances fromArray(InstanceProvider... instanceProviders) {
		return new DefaultInstances(ImmutableList.copyOf(instanceProviders));
	}
	
	/**
	 * Construct a default instance generator using the given instance providers.
	 * @param instanceProviders - collection of instance providers.
	 * @return An default instance generator.
	 */
	public static DefaultInstances fromCollection(Collection<InstanceProvider> instanceProviders) {
		return new DefaultInstances(ImmutableList.copyOf(instanceProviders));
	}
	
	/**
	 * Retrieves a immutable list of every default object providers that generates instances.
	 * @return Table of instance providers.
	 */
	public ImmutableList<InstanceProvider> getRegistered() {
		return registered;
	}
	
	/**
	 * Retrieve whether or not the constructor's parameters must be non-null.
	 * @return TRUE if they must be non-null, FALSE otherwise.
	 */
	public boolean isNonNull() {
		return nonNull;
	}

	/**
	 * Set whether or not the constructor's parameters must be non-null.
	 * @param nonNull - TRUE if they must be non-null, FALSE otherwise.
	 */
	public void setNonNull(boolean nonNull) {
		this.nonNull = nonNull;
	}
	
	/**
	 * Retrieve the the maximum height of the hierachy of creates types. 
	 * @return Maximum height.
	 */
	public int getMaximumRecursion() {
		return maximumRecursion;
	}

	/**
	 * Set the maximum height of the hierachy of creates types. Used to prevent cycles.
	 * @param maximumRecursion - maximum recursion height.
	 */
	public void setMaximumRecursion(int maximumRecursion) {
		if (maximumRecursion < 1)
			throw new IllegalArgumentException("Maxmimum recursion height must be one or higher.");
		this.maximumRecursion = maximumRecursion;
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
	 * Retrieve the constructor with the fewest number of parameters.
	 * @param type - type to construct.
	 * @return A constructor with the fewest number of parameters, or NULL if the type has no constructors.
	 */
	public <T> Constructor<T> getMinimumConstructor(Class<T> type) {
		return getMinimumConstructor(type, registered, 0);
	}
	
	@SuppressWarnings("unchecked")
	private <T> Constructor<T> getMinimumConstructor(Class<T> type, List<InstanceProvider> providers, int recursionLevel) {
		Constructor<T> minimum = null;
		int lastCount = Integer.MAX_VALUE;
		
		// Find the constructor with the fewest parameters
		for (Constructor<?> candidate : type.getConstructors()) {
			Class<?>[] types = candidate.getParameterTypes();
			
			// Note that we don't allow recursive types - that is, types that
			// require itself in the constructor.
			if (types.length < lastCount) {
				if (!contains(types, type)) {
					if (nonNull) {
						// Make sure all of these types are non-null
						if (isAnyNull(types, providers, recursionLevel)) {
							continue;
						}
					}
					
					minimum = (Constructor<T>) candidate;
					lastCount = types.length;
					
					// Don't loop again if we've already found the best possible constructor
					if (lastCount == 0)
						break;
				}
			}
		}
		
		return minimum;
	}
	
	/**
	 * Determine if any of the given types will be NULL once created.
	 * <p>
	 * Recursion level is the number of times the default method has been called.
	 * @param types - types to check.
	 * @param providers - instance providers.
	 * @param recursionLevel - current recursion level.
	 * @return TRUE if any of the types will return NULL, FALSE otherwise.
	 */
	private boolean isAnyNull(Class<?>[] types, List<InstanceProvider> providers, int recursionLevel) {
		// Just check if any of them are NULL
		for (Class<?> type : types) {
			if (getDefaultInternal(type, providers, recursionLevel) == null) {
				return true;
			}
		}	
		
		return false;
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
	 * @param providers - instance providers used during the construction.
	 * @return A default value/instance, or NULL if not possible.
	 */
	public <T> T getDefault(Class<T> type, List<InstanceProvider> providers) {
		return getDefaultInternal(type, providers, 0);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getDefaultInternal(Class<T> type, List<InstanceProvider> providers, int recursionLevel) {
			
		// The instance providiers should protect themselves against recursion
		try {
			for (InstanceProvider generator : providers) {
				Object value = generator.create(type);
				
				if (value != null)
					return (T) value;
			}
		} catch (NotConstructableException e) {
			return null;
		}

		// Guard against recursion
		if (recursionLevel >= maximumRecursion) {
			return null;
		}
		
		Constructor<T> minimum = getMinimumConstructor(type, providers, recursionLevel + 1);

		// Create the type with this constructor using default values. This might fail, though.
		try {
			if (minimum != null) {
				int parameterCount = minimum.getParameterTypes().length;
				Object[] params = new Object[parameterCount];
				Class<?>[] types = minimum.getParameterTypes();
				
				// Fill out 
				for (int i = 0; i < parameterCount; i++) {
					params[i] = getDefaultInternal(types[i], providers, recursionLevel + 1);
					
					// Did we break the non-null contract?
					if (params[i] == null && nonNull) {
						return null;
					}
				}

				return createInstance(type, minimum, types, params);
			}
			
		} catch (Exception e) {
			// Nope, we couldn't create this type. Might for instance be NotConstructableException.
		}
		
		// No suitable default value could be found
		return null;
	}
	
	/**
	 * Construct default instances using the CGLIB enhancer object instead.
	 * @param enhancer - a CGLIB enhancer to use.
	 * @return A default instance generator that uses the CGLIB enhancer.
	 */
	public DefaultInstances forEnhancer(Enhancer enhancer) {
		final Enhancer ex = enhancer;
		
		return new DefaultInstances(this) {
			@SuppressWarnings("unchecked")
			@Override
			protected <T> T createInstance(Class<T> type, Constructor<T> constructor, Class<?>[] types, Object[] params) {
				// Use the enhancer instead
				return (T) ex.create(types, params);
			}
		};
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
			return constructor.newInstance(params);
		} catch (Exception e) {
			//e.printStackTrace();
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

	@Override
	public Object create(@Nullable Class<?> type) {
		return getDefault(type);
	}
}
