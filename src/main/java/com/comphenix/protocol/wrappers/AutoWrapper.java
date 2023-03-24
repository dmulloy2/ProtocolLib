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

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.base.Defaults;
import com.google.common.base.Preconditions;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Automatically wraps an internal NMS class to a non-versioned, deofbuscated class.
 * Requirements:
 * <ul>
 *     <li>The wrapper must be public</li>
 *     <li>If the wrapper is an internal class, it must be static</li>
 *     <li>The wrapper must have one public constructor with no arguments (the default constructor is acceptable)</li>
 *     <li>The wrapper must have the the same number of fields as the NMS class</li>
 *     <li>Each field should correspond, in order, to its NMS counterpart</li>
 *     <li>Non-generic fields must have a converter</li>
 * </ul>
 *
 * @author dmulloy2
 */
public class AutoWrapper<T> implements EquivalentConverter<T> {
	private static final Object[] NO_ARGS = new Object[0];

	private Map<Integer, Function<Object, Object>> wrappers = new HashMap<>();
	private Map<Integer, Function<Object, Object>> unwrappers = new HashMap<>();

	// lazy
	private FieldAccessor[] nmsAccessors;
	private FieldAccessor[] wrapperAccessors;

	private Object[] nmsDefaultArgs;
	private ConstructorAccessor nmsInstanceCreator;

	private Class<T> wrapperClass;
	private Class<?> nmsClass;

	private AutoWrapper(Class<T> wrapperClass, Class<?> nmsClass) {
		this.wrapperClass = wrapperClass;
		this.nmsClass = nmsClass;
	}

	public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass, Class<?> nmsClass) {
		return new AutoWrapper<>(wrapperClass, nmsClass);
	}

	public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass, String nmsClassName) {
		return wrap(wrapperClass, MinecraftReflection.getMinecraftClass(nmsClassName));
	}

	public static <T> AutoWrapper<T> wrap(Class<T> wrapperClass, String nmsClassName, String... aliases) {
		return wrap(wrapperClass, MinecraftReflection.getMinecraftClass(nmsClassName, aliases));
	}

	public AutoWrapper<T> field(int index, Function<Object, Object> wrapper, Function<Object, Object> unwrapper) {
		wrappers.put(index, wrapper);
		unwrappers.put(index, unwrapper);
		return this;
	}

	public AutoWrapper<T> field(int index, EquivalentConverter converter) {
		return field(index, converter::getSpecific, specific -> converter.getGeneric(specific));
	}

	public T wrap(Object nmsObject) {
		Preconditions.checkNotNull(nmsObject);

		T instance;

		try {
			instance = wrapperClass.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new InvalidWrapperException(wrapperClass.getSimpleName() + " is not accessible!", ex);
		}

		// ensures that all accessors are present
		computeFieldAccessors();

		for (int i = 0; i < wrapperAccessors.length; i++) {
			FieldAccessor source = nmsAccessors[i];
			FieldAccessor target = wrapperAccessors[i];

			Object value = source.get(nmsObject);
			if (wrappers.containsKey(i))
				value = wrappers.get(i).apply(value);

			target.set(instance, value);
		}

		return instance;
	}

	public Object unwrap(Object wrapper) {
		Preconditions.checkNotNull(wrapper);

		// ensures that all accessors are present
		computeFieldAccessors();
		computeNmsConstructorAccess();

		Object instance = nmsInstanceCreator.invoke(nmsDefaultArgs);

		for (int i = 0; i < wrapperAccessors.length; i++) {
			FieldAccessor source = wrapperAccessors[i];
			FieldAccessor target = nmsAccessors[i];

			Object value = source.get(wrapper);
			if (unwrappers.containsKey(i))
				value = unwrappers.get(i).apply(value);

			target.set(instance, value);
		}

		return instance;
	}

	private void computeFieldAccessors() {
		if (nmsAccessors == null) {
			nmsAccessors = Arrays
					.stream(nmsClass.getDeclaredFields())
					.filter(field -> !Modifier.isStatic(field.getModifiers()))
					.map(Accessors::getFieldAccessor)
					.toArray(FieldAccessor[]::new);
		}

		if (wrapperAccessors == null) {
			wrapperAccessors = Arrays
					.stream(wrapperClass.getDeclaredFields())
					.map(Accessors::getFieldAccessor)
					.toArray(FieldAccessor[]::new);
		}
	}

	private void computeNmsConstructorAccess() {
		if (nmsInstanceCreator == null) {
			ConstructorAccessor noArgs = Accessors.getConstructorAccessorOrNull(nmsClass);
			if (noArgs != null) {
				// no args constructor is available - use it
				nmsInstanceCreator = noArgs;
				nmsDefaultArgs = NO_ARGS;
			} else {
				// use the first constructor of the class
				nmsInstanceCreator = Accessors.getConstructorAccessor(nmsClass.getDeclaredConstructors()[0]);
				nmsDefaultArgs = Arrays
						.stream(nmsInstanceCreator.getConstructor().getParameterTypes())
						.map(type -> type.isPrimitive() ? Defaults.defaultValue(type) : null)
						.toArray(Object[]::new);
			}
		}
	}

	// ---- Equivalent conversion

	@Override
	public T getSpecific(Object generic) {
		return wrap(generic);
	}

	@Override
	public Object getGeneric(Object specific) {
		return unwrap(specific);
	}

	@Override
	public Class<T> getSpecificType() {
		return wrapperClass;
	}

	public static class InvalidWrapperException extends RuntimeException {
		private InvalidWrapperException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
