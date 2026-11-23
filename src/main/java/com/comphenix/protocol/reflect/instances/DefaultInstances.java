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

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;

/**
 * Used to construct default instances of any type.
 * @author Kristian
 */
public class DefaultInstances implements InstanceProvider {

    /**
     * Standard default instance provider.
     */
    public static final DefaultInstances DEFAULT = DefaultInstances.fromArray(
            PrimitiveGenerator.INSTANCE,
            CollectionGenerator.INSTANCE,
            MinecraftGenerator.INSTANCE
    );

    /**
     * The maximum height of the heirarchy of creates types. Used to prevent cycles.
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
     * </ul>
     * @param <T> Type
     * @param type - the type to construct a default value.
     * @return A default value/instance, or NULL if not possible.
     */
    public <T> T getDefault(Class<T> type) {
        return getDefaultInternal(type, registered, 0, null);
    }

    private final ThreadLocal<Map<Class<?>, Boolean>> cache = ThreadLocal.withInitial(WeakHashMap::new);

    /**
     * Determines if a given class has a default value.
     *
     * @param type - the class to check
     * @return true if the class has a default value, false otherwise
     */
    public boolean hasDefault(Class<?> type) {
        return cache.get().computeIfAbsent(type, aClass -> getDefaultInternal(aClass, registered, 0, null) != null);
    }

    /**
     * Retrieve the constructor with the fewest number of parameters.
     * <p>
     * If several constructors have that number of parameters, the first one listed by the JVM is returned. That order
     * is unspecified and may differ between runs.
     * @param <T> Type
     * @param type - type to construct.
     * @return A constructor with the fewest number of parameters, or NULL if the type has no constructors.
     */
    public <T> Constructor<T> getMinimumConstructor(Class<T> type) {
        List<Constructor<T>> minimum = getMinimumConstructors(type, registered, 0, null);
        return minimum.isEmpty() ? null : minimum.get(0);
    }
    
    /**
     * Retrieve every usable constructor that has the fewest number of parameters, in the order listed by the JVM.
     */
    @SuppressWarnings("unchecked")
    private <T> List<Constructor<T>> getMinimumConstructors(Class<T> type, List<InstanceProvider> providers, int recursionLevel,
            List<Class<?>> creating) {
        List<Constructor<T>> minimum = new ArrayList<>(1);
        int lastCount = Integer.MAX_VALUE;
        
        // Find the constructors with the fewest parameters
        for (Constructor<?> candidate : type.getConstructors()) {
            Class<?>[] types = candidate.getParameterTypes();
            
            // Note that we don't allow recursive types - that is, types that
            // require itself in the constructor.
            if (types.length <= lastCount) {
                if (!contains(types, type) && !contains(types, MinecraftReflection.getPacketDataSerializerClass())) {
                    if (nonNull) {
                        // Make sure all of these types are non-null
                        if (isAnyNull(types, providers, recursionLevel, creating)) {
                            continue;
                        }
                    }

                    if (types.length < lastCount) {
                        minimum.clear();
                        lastCount = types.length;
                    }
                    minimum.add((Constructor<T>) candidate);

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
     * @param creating - the types that are being created, or NULL if none are.
     * @return TRUE if any of the types will return NULL, FALSE otherwise.
     */
    private boolean isAnyNull(Class<?>[] types, List<InstanceProvider> providers, int recursionLevel, List<Class<?>> creating) {
        // Just check if any of them are NULL
        for (Class<?> type : types) {
            if (getDefaultInternal(type, providers, recursionLevel, creating) == null) {
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
     * </ul>
     * @param <T> Type
     * @param type - the type to construct a default value.
     * @param providers - instance providers used during the construction.
     * @return A default value/instance, or NULL if not possible.
     */
    public <T> T getDefault(Class<T> type, List<InstanceProvider> providers) {
        return getDefaultInternal(type, providers, 0, null);
    }
    
    /**
     * @param creating - the types whose default values are being created on the way to this one, or NULL if none are.
     */
    @SuppressWarnings("unchecked")
    private <T> T getDefaultInternal(Class<T> type, List<InstanceProvider> providers, int recursionLevel, List<Class<?>> creating) {
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
        
        // A type that is needed again while its own default value is being created gets NULL, as it would once the
        // maximum recursion is reached. Otherwise trying every constructor below would explore the same cycle again
        // and again until that limit.
        if (creating == null) {
            creating = new ArrayList<>(4);
        } else if (creating.contains(type)) {
            return null;
        }

        creating.add(type);
        try {
            // Several constructors can have the fewest parameters, and the JVM lists them in an unspecified order that
            // can differ between runs. Try each of them, so that one which cannot be created from default values, such
            // as Vec3(Vector3fc) since Minecraft 1.21.11, does not hide one that can, such as Vec3(Vec3i).
            for (Constructor<T> constructor : getMinimumConstructors(type, providers, recursionLevel + 1, creating)) {
                T instance = createWithDefaults(type, constructor, providers, recursionLevel + 1, creating);
                if (instance != null) {
                    return instance;
                }
            }
        } finally {
            creating.remove(creating.size() - 1);
        }

        // No suitable default value could be found
        return null;
    }

    private <T> T createWithDefaults(Class<T> type, Constructor<T> constructor, List<InstanceProvider> providers, int recursionLevel,
            List<Class<?>> creating) {
        // Create the type with this constructor using default values. This might fail, though.
        try {
            Class<?>[] types = constructor.getParameterTypes();
            Object[] params = new Object[types.length];

            // Fill out
            for (int i = 0; i < types.length; i++) {
                params[i] = getDefaultInternal(types[i], providers, recursionLevel, creating);

                // Did we break the non-null contract?
                if (params[i] == null && nonNull) {
                    ProtocolLogger.log(Level.WARNING, "Nonnull contract broken.");
                    return null;
                }
            }

            return createInstance(type, constructor, types, params);
        } catch (Exception e) {
            // Nope, we couldn't create this type. Might for instance be NotConstructableException.
            return null;
        }
    }
    
    /**
     * Used by the default instance provider to create a class from a given constructor.
     * The default method uses reflection.
     * @param <T> Type
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
