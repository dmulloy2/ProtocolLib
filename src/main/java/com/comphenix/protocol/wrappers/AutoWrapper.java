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

import java.lang.reflect.Field;
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
	private Map<Integer, Function<Object, Object>> wrappers = new HashMap<>();
	private Map<Integer, Function<Object, Object>> unwrappers = new HashMap<>();

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

	public AutoWrapper<T> field(int index, Function<Object, Object> wrapper, Function<Object, Object> unwrapper) {
		wrappers.put(index, wrapper);
		unwrappers.put(index, unwrapper);
		return this;
	}

	public AutoWrapper<T> field(int index, EquivalentConverter converter) {
		return field(index, converter::getSpecific, specific -> converter.getGeneric(specific));
	}

	public T wrap(Object nmsObject) {
		T instance;

		try {
			instance = wrapperClass.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new InvalidWrapperException(wrapperClass.getSimpleName() + " is not accessible!", ex);
		}

		Field[] wrapperFields = wrapperClass.getDeclaredFields();
		Field[] nmsFields = Arrays
				.stream(nmsClass.getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.toArray(Field[]::new);

		for (int i = 0; i < wrapperFields.length; i++) {
			try {
				Field wrapperField = wrapperFields[i];

				Field nmsField = nmsFields[i];
				if (!nmsField.isAccessible())
					nmsField.setAccessible(true);

				Object value = nmsField.get(nmsObject);
				if (wrappers.containsKey(i))
					value = wrappers.get(i).apply(value);

				wrapperField.set(instance, value);
			} catch (ReflectiveOperationException ex) {
				throw new InvalidWrapperException("Failed to wrap field", ex);
			}
		}

		return instance;
	}

	public Object unwrap(Object wrapper) {
		Object instance;

		try {
			instance = nmsClass.newInstance();
		} catch (ReflectiveOperationException ex) {
			throw new InvalidWrapperException("Failed to construct new " + nmsClass.getSimpleName(), ex);
		}

		Field[] wrapperFields = wrapperClass.getDeclaredFields();
		Field[] nmsFields = Arrays
				.stream(nmsClass.getDeclaredFields())
				.filter(field -> !Modifier.isStatic(field.getModifiers()))
				.toArray(Field[]::new);

		for (int i = 0; i < wrapperFields.length; i++) {
			try {
				Field wrapperField = wrapperFields[i];

				Field nmsField = nmsFields[i];
				if (!nmsField.isAccessible())
					nmsField.setAccessible(true);
				if (Modifier.isFinal(nmsField.getModifiers()))
					unsetFinal(nmsField);

				Object value = wrapperField.get(wrapper);
				if (unwrappers.containsKey(i))
					value = unwrappers.get(i).apply(value);

				nmsField.set(instance, value);
			} catch (ReflectiveOperationException ex) {
				throw new InvalidWrapperException("Failed to unwrap field", ex);
			}
		}

		return instance;
	}

	private void unsetFinal(Field field) throws ReflectiveOperationException {
		Field modifiers = Field.class.getDeclaredField("modifiers");
		modifiers.setAccessible(true);
		modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
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
