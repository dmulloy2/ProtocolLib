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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

public class WrappedDataWatcher extends AbstractWrapper implements Iterable<WrappedWatchableObject> {
	private static MethodAccessor getter = null;
	private static MethodAccessor setter = null;

	private static Field MAP_FIELD = null;
	private static Field ENTITY_DATA_FIELD = null;
	private static Field ENTITY_FIELD = null;

	/**
	 * Constructs a new Data Watcher. This is currently unsupported in 1.9 and up due to changes in data watcher structure.
	 * Essentially, Mojang further tied data watchers to their entities.
	 * @deprecated
	 */
	@Deprecated
	public WrappedDataWatcher() {
		super(MinecraftReflection.getDataWatcherClass());
	}

	/**
	 * Constructs a wrapped data watcher around an existing NMS data watcher.
	 * @param handle NMS data watcher
	 */
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

		for (Field candidate : candidates) {
			if (Modifier.isStatic(candidate.getModifiers())) {
				// This is the entity class to current index map, which we really don't have a use for
			} else {
				// This is the map we're looking for
				MAP_FIELD = candidate;
				MAP_FIELD.setAccessible(true);
			}
		}

		if (MAP_FIELD == null) {
			throw new FieldAccessException("Could not find index -> object map.");
		}

		Map<Integer, ?> map = null;

		try {
			map = (Map<Integer, ?>) MAP_FIELD.get(handle);
		} catch (ReflectiveOperationException e) {
			throw new FieldAccessException("Failed to access index -> object map", e);
		}

		Map<Integer, WrappedWatchableObject> ret = new HashMap<>();
		for (Entry<Integer, ?> entry : map.entrySet()) {
			ret.put(entry.getKey(), new WrappedWatchableObject(entry.getValue()));
		}

		return ret;
	}

	public WrappedWatchableObject getWatchableObject(int index) {
		return asMap().get(index);
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
		return getWatcherObject(new WrappedDataWatcherObject(index, null));
	}
   
    private Object getWatcherObject(WrappedDataWatcherObject watcherObject) {
		if (getter == null) {
			getter = Accessors.getMethodAccessor(handleType, "get", watcherObject.getHandleType());
		}

		return getter.invoke(handle, watcherObject.getHandle());
    }

	// ---- Object Setters

	public void setObject(int index, Object value) {
		setObject(new WrappedDataWatcherObject(index, null), value);
	}

	public void setObject(WrappedDataWatcherObject watcherObject, Object value) {
		if (setter == null) {
			setter = Accessors.getMethodAccessor(handleType, "set", watcherObject.getHandleType(), Object.class);
		}

		setter.invoke(handle, watcherObject.getHandle(), WrappedWatchableObject.getUnwrapped(value));
	}

	/**
	 * Clone the content of the current DataWatcher.
	 * 
	 * @return A cloned data watcher.
	 */
	public WrappedDataWatcher deepClone() {
		// TODO This
		return null;
	}

	/**
	 * Retrieve the data watcher associated with an entity.
	 * 
	 * @param entity - the entity to read from.
	 * @return Associated data watcher.
	 * @throws FieldAccessException Reflection failed.
	 */
	public static WrappedDataWatcher getEntityWatcher(Entity entity) throws FieldAccessException {
		if (ENTITY_DATA_FIELD == null)
			ENTITY_DATA_FIELD = FuzzyReflection.fromClass(MinecraftReflection.getEntityClass(), true).getFieldByType("datawatcher", MinecraftReflection.getDataWatcherClass());

		BukkitUnwrapper unwrapper = new BukkitUnwrapper();

		try {
			Object nsmWatcher = FieldUtils.readField(ENTITY_DATA_FIELD, unwrapper.unwrapItem(entity), true);

			if (nsmWatcher != null)
				return new WrappedDataWatcher(nsmWatcher);
			else
				return null;

		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Cannot access DataWatcher field.", e);
		}
	}

	/**
	 * Retrieve the entity associated with this data watcher.
	 * <p>
	 * <b>Warning:</b> This is only supported on 1.7.2 and above.
	 * 
	 * @return The entity, or NULL.
	 */
	public Entity getEntity() {
		if (!MinecraftReflection.isUsingNetty())
			throw new IllegalStateException("This method is only supported on 1.7.2 and above.");

		try {
			return (Entity) MinecraftReflection.getBukkitEntity(ENTITY_FIELD.get(handle));
		} catch (Exception e) {
			throw new RuntimeException("Unable to retrieve entity.", e);
		}
	}

	/**
	 * Set the entity associated with this data watcher.
	 * <p>
	 * <b>Warning:</b> This is only supported on 1.7.2 and above.
	 * 
	 * @param entity - the new entity.
	 */
	public void setEntity(Entity entity) {
		if (!MinecraftReflection.isUsingNetty())
			throw new IllegalStateException("This method is only supported on 1.7.2 and above.");

		try {
			ENTITY_FIELD.set(handle, BukkitUnwrapper.getInstance().unwrapItem(entity));
		} catch (Exception e) {
			throw new RuntimeException("Unable to set entity.", e);
		}
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

	public static class WrappedDataWatcherObject extends AbstractWrapper {
		private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherSerializerClass();
		private static Constructor<?> constructor = null;

		private StructureModifier<Object> modifier = new StructureModifier<Object>(HANDLE_TYPE);

		public WrappedDataWatcherObject(Object handle) {
			super(HANDLE_TYPE);
			setHandle(handle);
			modifier.withTarget(handle);
		}

		public WrappedDataWatcherObject(int index, Serializer serializer) {
			this(newHandle(index, serializer.getHandle()));
		}

		private static Object newHandle(int index, Object serializer) {
			if (constructor == null) {
				constructor = HANDLE_TYPE.getConstructors()[0];
			}

			try {
				return constructor.newInstance(index, serializer);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to create new DataWatcherObject", e);
			}
		}

		public int getIndex() {
			return (int) modifier.read(0);
		}

		public Serializer getSerializer() {
			// TODO this
			return null;
		}
	}

	public static class Serializer extends AbstractWrapper {
		private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherSerializerClass();

		private final Class<?> type;
		private final boolean optional;
		
		public Serializer(Class<?> type, Object handle, boolean optional) {
			super(HANDLE_TYPE);
			this.type = type;
			this.optional = optional;

			setHandle(handle);
		}

		public Class<?> getType() {
			return type;
		}

		public boolean isOptional() {
			return optional;
		}
	}

	public static class Registry {
		private static boolean INITIALIZED = false;
		private static Map<Class<?>, Serializer> REGISTRY = new HashMap<>();

		public static Serializer get(Class<?> clazz) {
			if (! INITIALIZED) {
				initialize();
				INITIALIZED = true;
			}

			return REGISTRY.get(clazz);
		}

		private static void initialize() {
			List<Field> candidates = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftClass("DataWatcherRegistry"), true)
				.getFieldListByType(MinecraftReflection.getDataWatcherSerializerClass());
			for (Field candidate : candidates) {
				Type generic = candidate.getGenericType();
				if (generic instanceof ParameterizedType) {
					ParameterizedType type = (ParameterizedType) generic;
					Type[] args = type.getActualTypeArguments();
					Type arg = args[0];

					Class<?> innerClass = null;
					boolean optional = false;

					if (arg instanceof Class<?>) {
						innerClass = (Class<?>) arg;
					} else if (arg instanceof ParameterizedType) {
						innerClass = (Class<?>) ((ParameterizedType) arg).getActualTypeArguments()[0];
						optional = true;
					} else {
						throw new IllegalStateException("Failed to find inner class of field " + candidate);
					}

					Object serializer;

					try {
						serializer = candidate.get(null);
					} catch (ReflectiveOperationException e) {
						throw new IllegalStateException("Failed to read field " + candidate);
					}

					REGISTRY.put(innerClass, new Serializer(innerClass, serializer, optional));
				}
			}
		}
	}
}