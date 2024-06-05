/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2017 Dan Mulloy
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;

/**
 * Utility class for converters
 * @author dmulloy2
 */
@SuppressWarnings("unchecked")
public class Converters {

    /**
     * Returns a converter that ignores null elements, so that the underlying converter doesn't have to worry about them.
     * @param converter Underlying converter
     * @param <T> Element type
     * @return An ignore null converter
     */
    public static <T> EquivalentConverter<T> ignoreNull(final EquivalentConverter<T> converter) {
        return new EquivalentConverter<T>() {
            @Override
            public T getSpecific(Object generic) {
                return generic != null ? converter.getSpecific(generic) : null;
            }

            @Override
            public Object getGeneric(T specific) {
                return specific != null ? converter.getGeneric(specific) : null;
            }

            @Override
            public Class<T> getSpecificType() {
                return converter.getSpecificType();
            }
        };
    }

    /**
     * Returns a converter that passes generic and specific values through without converting.
     * @param clazz Element class
     * @param <T> Element type
     * @return A passthrough converter
     */
    public static <T> EquivalentConverter<T> passthrough(final Class<T> clazz) {
        return ignoreNull(new EquivalentConverter<T>() {
            @Override
            public T getSpecific(Object generic) {
                return (T) generic;
            }

            @Override
            public Object getGeneric(T specific) {
                return specific;
            }

            @Override
            public Class<T> getSpecificType() {
                return clazz;
            }
        });
    }

    /**
     * Creates a simple converter for wrappers with {@code getHandle()} and {@code fromHandle(...)} methods. With Java 8,
     * converters can be reduced to a single line (see {@link BukkitConverters#getWrappedGameProfileConverter()}).
     * @param toHandle Function from wrapper to handle (i.e. {@code getHandle()})
     * @param fromHandle Function from handle to wrapper (i.e. {@code fromHandle(Object)})
     * @param <T> Wrapper type
     * @return A handle converter
     */
    public static <T> EquivalentConverter<T> handle(final Function<T, Object> toHandle,
            final Function<Object, T> fromHandle, final Class<T> specificType) {
        return new EquivalentConverter<T>() {
            @Override
            public T getSpecific(Object generic) {
                return fromHandle.apply(generic);
            }

            @Override
            public Object getGeneric(T specific) {
                return toHandle.apply(specific);
            }

            @Override
            public Class<T> getSpecificType() {
                return specificType;
            }
        };
    }

    /**
     * Creates a generic array converter. Converts a NMS object array to and from a wrapper array by converting
     * each element individually.
     *
     * @param nmsClass NMS class
     * @param converter Underlying converter
     * @param <T> Generic type
     * @return An array converter
     */
    public static <T> EquivalentConverter<T[]> array(final Class<?> nmsClass, final EquivalentConverter<T> converter) {
        return new EquivalentConverter<T[]>() {
            @Override
            public T[] getSpecific(Object generic) {
                Object[] array = (Object[]) generic;
                Class<T[]> clazz = getSpecificType();
                T[] result = clazz.cast(Array.newInstance(clazz.getComponentType(), array.length));

                // Unwrap every item
                for (int i = 0; i < result.length; i++) {
                    result[i] = converter.getSpecific(array[i]);
                }

                return result;
            }

            @Override
            public Object getGeneric(T[] specific) {
                Object[] result = (Object[]) Array.newInstance(nmsClass, specific.length);

                // Wrap every item
                for (int i = 0; i < result.length; i++) {
                    result[i] = converter.getGeneric(specific[i]);
                }

                return result;
            }

            @Override
            public Class<T[]> getSpecificType() {
                return (Class<T[]>) MinecraftReflection.getArrayClass(converter.getSpecificType());
            }
        };
    }

    public static <T> EquivalentConverter<Optional<T>> optional(final EquivalentConverter<T> converter) {
        return new EquivalentConverter<Optional<T>>() {
            @Override
            public Object getGeneric(Optional<T> specific) {
                return specific.map(converter::getGeneric);
            }

            @Override
            public Optional<T> getSpecific(Object generic) {
                if (generic == null) return Optional.empty();
                Optional<Object> optional = (Optional<Object>) generic;
                return optional.map(converter::getSpecific);
            }

            @Override
            public Class<Optional<T>> getSpecificType() {
                return (Class<Optional<T>>) Optional.empty().getClass();
            }
        };
    }

    public static <T, C extends Collection<T>> EquivalentConverter<C> collection(
            final EquivalentConverter<T> elementConverter,
            final Function<Collection<Object>, C> genericToSpecificCollectionFactory,
            final Function<C, Collection<?>> specificToGenericCollectionFactory
    ) {
        return ignoreNull(new EquivalentConverter<C>() {

            @Override
            public Object getGeneric(C specific) {
                // generics are very cool, thank you java
                Collection<Object> targetCollection = (Collection<Object>) specificToGenericCollectionFactory.apply(specific);
                for (T element : specific) {
                    Object generic = elementConverter.getGeneric(element);
                    if (generic != null) {
                        targetCollection.add(generic);
                    }
                }

                return targetCollection;
            }

            @Override
            public C getSpecific(Object generic) {
                if (generic instanceof Collection<?>) {
                    Collection<Object> sourceCollection = (Collection<Object>) generic;
                    C targetCollection = genericToSpecificCollectionFactory.apply(sourceCollection);
                    // copy over all elements into a new collection
                    for (Object element : sourceCollection) {
                        T specific = elementConverter.getSpecific(element);
                        if (specific != null) {
                            targetCollection.add(specific);
                        }
                    }

                    return targetCollection;
                }
                // not valid
                return null;
            }

            @Override
            public Class<C> getSpecificType() {
                return (Class) Collection.class;
            }
        });
    }

    public static <T> EquivalentConverter<Iterable<T>> iterable(
            final EquivalentConverter<T> elementConverter,
            final Supplier<List<T>> specificCollectionFactory,
            final Supplier<List<?>> genericCollectionFactory
    ) {
        return ignoreNull(new EquivalentConverter<Iterable<T>>() {

            @Override
            public Object getGeneric(Iterable<T> specific) {
                // generics are very cool, thank you java
                List<Object> targetCollection = (List<Object>) genericCollectionFactory.get();
                for (T element : specific) {
                    Object generic = elementConverter.getGeneric(element);
                    if (generic != null) {
                        targetCollection.add(generic);
                    }
                }

                return targetCollection;
            }

            @Override
            public Iterable<T> getSpecific(Object generic) {
                if (generic instanceof Iterable<?>) {
                    Iterable<Object> sourceCollection = (Iterable<Object>) generic;
                    List<T> targetCollection = specificCollectionFactory.get();
                    // copy over all elements into a new collection
                    for (Object element : sourceCollection) {
                        T specific = elementConverter.getSpecific(element);
                        if (specific != null) {
                            targetCollection.add(specific);
                        }
                    }

                    return targetCollection;
                }
                // not valid
                return null;
            }

            @Override
            public Class<Iterable<T>> getSpecificType() {
                return (Class) Iterable.class;
            }
        });
    }

    private static MethodAccessor holderGetValue;

    public static <T> EquivalentConverter<T> holder(final EquivalentConverter<T> converter,
                                                    final WrappedRegistry registry) {
        return new EquivalentConverter<T>() {
            @Override
            public Object getGeneric(T specific) {
                Object generic = converter.getGeneric(specific);
                return registry.getHolder(generic);
            }

            @Override
            public T getSpecific(Object generic) {
                Preconditions.checkNotNull(generic, "generic cannot be null");

                if (holderGetValue == null) {
                    Class<?> holderClass = MinecraftReflection.getHolderClass();
                    FuzzyReflection fuzzy = FuzzyReflection.fromClass(holderClass, false);
                    holderGetValue = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                            .newBuilder()
                            .parameterCount(0)
                            .banModifier(Modifier.STATIC)
                            .returnTypeExact(Object.class)
                            .build()));
                }

                if (holderGetValue == null) {
                    throw new IllegalStateException("Unable to find Holder#value method.");
                }

                Object value = holderGetValue.invoke(generic);
                return converter.getSpecific(value);
            }

            @Override
            public Class<T> getSpecificType() {
                return converter.getSpecificType();
            }
        };
    }

    public static <T> List<T> toList(Iterable<? extends T> iterable) {
        if (iterable instanceof List) {
            return (List<T>) iterable;
        }

        List<T> result = new ArrayList<>();
        if (iterable instanceof Collection) {
            Collection<T> coll = (Collection<T>) iterable;
            result.addAll(coll);
        } else {
            for (T elem : iterable) {
                result.add(elem);
            }
        }

        return result;
    }
}
