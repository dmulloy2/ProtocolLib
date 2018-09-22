/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2017 Dan Mulloy
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
package com.comphenix.protocol.wrappers;

import java.util.function.Function;

import com.comphenix.protocol.reflect.EquivalentConverter;

/**
 * Utility class for converters
 * @author dmulloy2
 */
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
	                                                final Function<Object, T> fromHandle) {
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
				return null;
			}
		};
	}
}
