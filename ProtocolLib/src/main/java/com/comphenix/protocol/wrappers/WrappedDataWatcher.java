/**
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
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

public class WrappedDataWatcher extends AbstractWrapper implements Iterable<WrappedWatchableObject> {
	private static ConstructorAccessor dataWatcherObjectConstructor = null;
	private static MethodAccessor getter = null;
	private static MethodAccessor setter = null;

	@Deprecated
	public WrappedDataWatcher() {
		super(MinecraftReflection.getDataWatcherClass());
	}

	public WrappedDataWatcher(Object handle) {
		this();
		setHandle(handle);
	}

	@Override
	public Iterator<WrappedWatchableObject> iterator() {
		return getWatchableObjects().iterator();
	}

	public List<WrappedWatchableObject> getWatchableObjects() {
		return new ArrayList<>(asMap().values());
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, WrappedWatchableObject> asMap() {
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
		List<Field> candidates = fuzzy.getFieldListByType(Map.class);

		Field match = null;
		for (Field candidate : candidates) {
			if (Modifier.isStatic(candidate.getModifiers())) {
				// This is the entity class to current index map, which we really don't have a use for
			} else {
				// This is the map we're looking for
				match = candidate;
			}
		}

		if (match == null) {
			throw new FieldAccessException("Could not find index -> object map.");
		}

		Map<Integer, ?> map = null;

		try {
			match.setAccessible(true);
			map = (Map<Integer, ?>) match.get(handle);
		} catch (IllegalArgumentException e) {
			throw new FieldAccessException(e);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException(e);
		}

		Map<Integer, WrappedWatchableObject> ret = new HashMap<>();
		for (Entry<Integer, ?> entry : map.entrySet()) {
			ret.put(entry.getKey(), new WrappedWatchableObject(entry.getValue()));
		}

		return ret;
	}

	public int size() {
		return asMap().size();
	}

	// ---- Object Getters

    /**
     * Get a watched byte.
     * @param index - index of the watched byte.
     * @return The watched byte, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Byte getByte(int index) throws FieldAccessException {
    	return (Byte) getObject(index);
    }

    /**
     * Get a watched short.
     * @param index - index of the watched short.
     * @return The watched short, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Short getShort(int index) throws FieldAccessException {
    	return (Short) getObject(index);
    }

    /**
     * Get a watched integer.
     * @param index - index of the watched integer.
     * @return The watched integer, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Integer getInteger(int index) throws FieldAccessException {
    	return (Integer) getObject(index);
    }

    /**
     * Get a watched float.
     * @param index - index of the watched float.
     * @return The watched float, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Float getFloat(int index) throws FieldAccessException {
    	return (Float) getObject(index);
    }

    /**
     * Get a watched string.
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public String getString(int index) throws FieldAccessException {
    	return (String) getObject(index);
    }

    /**
     * Get a watched string.
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public ItemStack getItemStack(int index) throws FieldAccessException {
    	return (ItemStack) getObject(index);
    }

    /**
     * Get a watched string.
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public WrappedChunkCoordinate getChunkCoordinate(int index) throws FieldAccessException {
    	return (WrappedChunkCoordinate) getObject(index);
    }

    /**
     * Retrieve a watchable object by index.
     * @param index - index of the object to retrieve.
     * @return The watched object.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Object getObject(int index) throws FieldAccessException {
    	return WrappedWatchableObject.getWrapped(getWatchedObject(index));
    }

	private Object getWatchedObject(int index) {
		if (dataWatcherObjectConstructor == null) {
			dataWatcherObjectConstructor = Accessors.getConstructorAccessor(MinecraftReflection.getDataWatcherObjectClass().getConstructors()[0]);
		}

		Object object = dataWatcherObjectConstructor.invoke(index, null);

		if (getter == null) {
			getter = Accessors.getMethodAccessor(handleType, "get", object.getClass());
		}

		return getter.invoke(handle, object);
	}

	// ---- Object Setters

	public void setObject(int index, Object value) {
		if (dataWatcherObjectConstructor == null) {
			dataWatcherObjectConstructor = Accessors.getConstructorAccessor(MinecraftReflection.getDataWatcherObjectClass().getConstructors()[0]);
		}

		Object object = dataWatcherObjectConstructor.invoke(index, null);

		if (setter == null) {
			setter = Accessors.getMethodAccessor(handleType, "set", object.getClass(), Object.class);
		}

		setter.invoke(handle, object, WrappedWatchableObject.getUnwrapped(value));
	}

	public WrappedDataWatcher deepClone() {
		// TODO This
		return null;
	}

	@Deprecated
	@SuppressWarnings("unused")
	public static Integer getTypeID(Class<?> clazz) {
		return null;
	}

	@Deprecated
	@SuppressWarnings("unused")
	public static Class<?> getTypeClass(int typeID) {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null) return false;

		if (obj instanceof WrappedDataWatcher) {
			WrappedDataWatcher other = (WrappedDataWatcher) obj;
			Iterator<WrappedWatchableObject> first = iterator(), second = other.iterator();

			// Make sure they're the same size
			if (size() != other.size())
				return false;

			for (; first.hasNext() && second.hasNext();) {
				// See if the two elements are equal
				if (!first.next().equals(second.next()))
					return false;
			}

			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getWatchableObjects().hashCode();
	}
}