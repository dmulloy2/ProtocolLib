/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.google.common.base.Optional;

/**
 * Represents a DataWatcher in 1.9
 * 
 * @author dmulloy2
 */
public class WrappedDataWatcher extends AbstractWrapper implements Iterable<WrappedWatchableObject> {
	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherClass();

	private static MethodAccessor GETTER = null;
	private static MethodAccessor SETTER = null;
	private static MethodAccessor REGISTER = null;

	private static FieldAccessor ENTITY_DATA_FIELD = null;
	private static FieldAccessor ENTITY_FIELD = null;
	private static FieldAccessor MAP_FIELD = null;

	private static ConstructorAccessor constructor = null;
	private static ConstructorAccessor lightningConstructor = null;

	private static Object fakeEntity = null;

	// ---- Construction

	/**
	 * Constructs a wrapped data watcher around an existing NMS data watcher.
	 * @param handle NMS data watcher
	 */
	public WrappedDataWatcher(Object handle) {
		super(HANDLE_TYPE);
		setHandle(handle);
	}

	/**
	 * Constructs a new DataWatcher using a fake entity.
	 */
	public WrappedDataWatcher() {
		this(newHandle(fakeEntity()));
	}

	/**
	 * Constructs a new DataWatcher using a real entity.
	 * @param entity The entity
	 */
	public WrappedDataWatcher(Entity entity) {
		this(newHandle(BukkitUnwrapper.getInstance().unwrapItem(entity)));
	}

	private static Object newHandle(Object entity) {
		if (constructor == null) {
			constructor = Accessors.getConstructorAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass());
		}

		return constructor.invoke(entity);
	}

	private static Object fakeEntity() {
		if (fakeEntity != null) {
			return fakeEntity;
		}

		// We can create a fake lightning strike without it affecting anything
		if (lightningConstructor == null) {
			lightningConstructor = Accessors.getConstructorAccessor(MinecraftReflection.getMinecraftClass("EntityLightning"),
					MinecraftReflection.getNmsWorldClass(), double.class, double.class, double.class, boolean.class);
		}

		return fakeEntity = lightningConstructor.invoke(null, 0, 0, 0, true);
	}

	// ---- Collection Methods

	@SuppressWarnings("unchecked")
	private Map<Integer, Object> getMap() {
		if (MAP_FIELD == null) {
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
			List<Field> candidates = fuzzy.getFieldListByType(Map.class);

			for (Field candidate : candidates) {
				if (Modifier.isStatic(candidate.getModifiers())) {
					// This is the entity class to current index map, which we really don't have a use for
				} else {
					// This is the map we're looking for
					MAP_FIELD = Accessors.getFieldAccessor(candidate);
				}
			}
		}

		if (MAP_FIELD == null) {
			throw new FieldAccessException("Could not find index <-> Item map.");
		}

		return (Map<Integer, Object>) MAP_FIELD.get(handle);
	}

	/**
	 * Gets the contents of this DataWatcher as a map.
	 * @return The contents
	 */
	public Map<Integer, WrappedWatchableObject> asMap() {
		return new ConvertedMap<Integer, Object, WrappedWatchableObject>(getMap()) {
			@Override
			protected WrappedWatchableObject toOuter(Object inner) {
				return inner != null ? new WrappedWatchableObject(inner) : null;
			}

			@Override
			protected Object toInner(WrappedWatchableObject outer) {
				return outer != null ? outer.getHandle() : null;
			}
		};
	}

	/**
	 * Gets a set containing the registered indexes.
	 * @return The set
	 */
	public Set<Integer> getIndexes() {
		return getMap().keySet();
	}

	/**
	 * Gets a list of the contents of this DataWatcher.
	 * @return The contents
	 */
	public List<WrappedWatchableObject> getWatchableObjects() {
		return new ArrayList<>(asMap().values());
	}

	@Override
	public Iterator<WrappedWatchableObject> iterator() {
		return getWatchableObjects().iterator();
	}

	/**
	 * Gets the size of this DataWatcher's contents.
	 * @return The size
	 */
	public int size() {
		return getMap().size();
	}

	/**
	 * Gets the item at a given index.
	 * 
	 * @param index Index to get
	 * @return The watchable object, or null if none exists
	 */
	public WrappedWatchableObject getWatchableObject(int index) {
		Object handle = getMap().get(index);
		if (handle != null) {
			return new WrappedWatchableObject(handle);
		} else {
			return null;
		}
	}

	/**
	 * Removes the item at a given index.
	 * 
	 * @param index Index to remove
	 * @return The previous value, or null if none existed
	 */
	public WrappedWatchableObject remove(int index) {
		Object removed = getMap().remove(index);
		return removed != null ? new WrappedWatchableObject(removed) : null;
	}

	/**
	 * Whether or not this DataWatcher has an object at a given index.
	 * 
	 * @param index Index to check for
	 * @return True if it does, false if not
	 */
	public boolean hasIndex(int index) {
		return getMap().containsKey(index);
	}

	// ---- Object Getters

	/**
	 * Get a watched byte.
	 * 
	 * @param index - index of the watched byte.
	 * @return The watched byte, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public Byte getByte(int index) throws FieldAccessException {
		return (Byte) getObject(index);
	}

	/**
	 * Get a watched short.
	 * 
	 * @param index - index of the watched short.
	 * @return The watched short, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public Short getShort(int index) throws FieldAccessException {
		return (Short) getObject(index);
	}

	/**
	 * Get a watched integer.
	 * 
	 * @param index - index of the watched integer.
	 * @return The watched integer, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public Integer getInteger(int index) throws FieldAccessException {
		return (Integer) getObject(index);
	}

	/**
	 * Get a watched float.
	 * 
	 * @param index - index of the watched float.
	 * @return The watched float, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public Float getFloat(int index) throws FieldAccessException {
		return (Float) getObject(index);
	}

	/**
	 * Get a watched string.
	 * 
	 * @param index - index of the watched string.
	 * @return The watched string, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public String getString(int index) throws FieldAccessException {
		return (String) getObject(index);
	}

	/**
	 * Get a watched string.
	 * 
	 * @param index - index of the watched string.
	 * @return The watched string, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public ItemStack getItemStack(int index) throws FieldAccessException {
		return (ItemStack) getObject(index);
	}

	/**
	 * Get a watched string.
	 * 
	 * @param index - index of the watched string.
	 * @return The watched string, or NULL if this value doesn't exist.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public WrappedChunkCoordinate getChunkCoordinate(int index) throws FieldAccessException {
		return (WrappedChunkCoordinate) getObject(index);
	}

	/**
	 * Retrieve a watchable object by index.
	 * 
	 * @param index - index of the object to retrieve.
	 * @return The watched object.
	 * @throws FieldAccessException Cannot read underlying field.
	 */
	public Object getObject(int index) throws FieldAccessException {
		return getObject(new WrappedDataWatcherObject(index, null));
	}

	/**
	 * Retrieve a watchable object by watcher object.
	 * 
	 * @param object The watcher object
	 * @return The watched object
	 */
	public Object getObject(WrappedDataWatcherObject object) {
		Validate.notNull(object, "Watcher object cannot be null!");

		if (GETTER == null) {
			GETTER = Accessors.getMethodAccessor(handleType, "get", object.getHandleType());
		}

		Object value = GETTER.invoke(handle, object.getHandle());
		return WrappedWatchableObject.getWrapped(value);
	}

	// ---- Object Setters

	/**
	 * Sets the DataWatcher Item at a given index to a new value.
	 * 
	 * @param index Index
	 * @param value New value
	 * @deprecated Usage of this method is discouraged due to changes in 1.9.
	 */
	@Deprecated
	public void setObject(int index, Object value) {
		Validate.isTrue(hasIndex(index), "You cannot register objects without the watcher object!");
		setObject(new WrappedDataWatcherObject(index, null), value);
	}

	/**
	 * Sets the DataWatcher Item associated with a given watcher object to a new value.
	 * 
	 * @param object Associated watcher object
	 * @param value New value
	 */
	public void setObject(WrappedDataWatcherObject object, Object value) {
		Validate.notNull(object, "Watcher object cannot be null!");

		if (SETTER == null || REGISTER == null) {
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
			List<Method> methods = fuzzy.getMethodList(FuzzyMethodContract.newBuilder()
					.banModifier(Modifier.STATIC)
					.requireModifier(Modifier.PUBLIC)
					.parameterExactArray(object.getHandleType(), Object.class)
					.build());
			for (Method method : methods) {
				if (method.getName().equals("set")) {
					SETTER = Accessors.getMethodAccessor(method);
				} else if (method.getName().equals("register")) {
					REGISTER = Accessors.getMethodAccessor(method);
				}
			}
		}

		if (hasIndex(object.getIndex())) {
			SETTER.invoke(handle, object.getHandle(), WrappedWatchableObject.getUnwrapped(value));
		} else {
			Serializer serializer = object.getSerializer();
			Validate.notNull(serializer, "You must specify a serializer to register an object!");
			REGISTER.invoke(handle, object.getHandle(), WrappedWatchableObject.getUnwrapped(value));
		}
	}

	// ---- Utility Methods

	/**
	 * Clone the content of the current DataWatcher.
	 * 
	 * @return A cloned data watcher.
	 */
	public WrappedDataWatcher deepClone() {
		WrappedDataWatcher clone = new WrappedDataWatcher(getEntity());

		for (WrappedWatchableObject wrapper : this) {
			clone.setObject(wrapper.getWatcherObject(), wrapper);
		}

		return clone;
	}

	/**
	 * Retrieve the data watcher associated with an entity.
	 * 
	 * @param entity - the entity to read from.
	 * @return Associated data watcher.
	 */
	public static WrappedDataWatcher getEntityWatcher(Entity entity) {
		if (ENTITY_DATA_FIELD == null) {
			ENTITY_DATA_FIELD = Accessors.getFieldAccessor(MinecraftReflection.getEntityClass(), MinecraftReflection.getDataWatcherClass(), true);
		}

		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		Object handle = ENTITY_DATA_FIELD.get(unwrapper.unwrapItem(entity));
		return handle != null ? new WrappedDataWatcher(handle) : null;
	}

	/**
	 * Retrieve the entity associated with this data watcher.
	 * @return The entity, or NULL.
	 */
	public Entity getEntity() {
		if (ENTITY_FIELD == null) {
			ENTITY_FIELD = Accessors.getFieldAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass(), true);
		}

		return (Entity) MinecraftReflection.getBukkitEntity(ENTITY_FIELD.get(handle));
	}

	/**
	 * Set the entity associated with this data watcher.
	 * @param entity - the new entity.
	 */
	public void setEntity(Entity entity) {
		if (ENTITY_FIELD == null) {
			ENTITY_FIELD = Accessors.getFieldAccessor(HANDLE_TYPE, MinecraftReflection.getEntityClass(), true);
		}

		ENTITY_FIELD.set(handle, BukkitUnwrapper.getInstance().unwrapItem(entity));
	}

	/**
	 * No longer supported in 1.9 due to the removal of a consistent type <-> ID map.
	 * 
	 * @param clazz
	 * @return Null
	 */
	@Deprecated
	public static Integer getTypeID(Class<?> clazz) {
		return null;
	}

	/**
	 * No longer supported in 1.9 due to the removal of a consistent type <-> ID map.
	 * 
	 * @param typeID
	 * @return Null
	 */
	@Deprecated
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

	@Override
	public String toString() {
		return "WrappedDataWatcher[handle=" + handle + "]";
	}

	// ---- 1.9 classes

	/**
	 * Represents a DataWatcherObject in 1.9.
	 * 
	 * @author dmulloy2
	 */
	public static class WrappedDataWatcherObject extends AbstractWrapper {
		private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherObjectClass();
		private static ConstructorAccessor constructor = null;
		private static MethodAccessor getSerializer = null;

		private final StructureModifier<Object> modifier;

		/**
		 * Creates a new watcher object from a NMS handle
		 * 
		 * @param handle NMS handle
		 */
		public WrappedDataWatcherObject(Object handle) {
			super(HANDLE_TYPE);

			setHandle(handle);
			this.modifier = new StructureModifier<Object>(HANDLE_TYPE).withTarget(handle);
		}

		/**
		 * Creates a new watcher object from an index and serializer
		 * 
		 * @param index Index
		 * @param serializer Serializer, see {@link Registry}
		 */
		public WrappedDataWatcherObject(int index, Serializer serializer) {
			this(newHandle(index, serializer));
		}

		private static Object newHandle(int index, Serializer serializer) {
			if (constructor == null) {
				constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
			}

			Object handle = serializer != null ? serializer.getHandle() : null;
			return constructor.invoke(index, handle);
		}

		/**
		 * Gets this watcher object's index.
		 * 
		 * @return The index
		 */
		public int getIndex() {
			return (int) modifier.read(0);
		}

		/**
		 * Gets this watcher object's serializer. Will return null if the serializer was never specified.
		 * 
		 * @return The serializer, or null
		 */
		public Serializer getSerializer() {
			if (getSerializer == null) {
				getSerializer = Accessors.getMethodAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
						.getMethodByParameters("getSerializer", MinecraftReflection.getDataWatcherSerializerClass(), new Class[0]));
			}

			Object serializer = getSerializer.invoke(handle);
			if (serializer != null) {
				Serializer wrapper = Registry.fromHandle(serializer);
				if (wrapper != null) {
					return wrapper;
				} else {
					return new Serializer(null, serializer, false);
				}
			} else {
				return null;
			}
		}

		@Override
		public String toString() {
			return "DataWatcherObject[index=" + getIndex() + ", serializer=" + getSerializer() + "]";
		}
	}

	/**
	 * Represents a DataWatcherSerializer in 1.9.
	 * 
	 * @author dmulloy2
	 */
	public static class Serializer extends AbstractWrapper {
		private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherSerializerClass();

		private final Class<?> type;
		private final boolean optional;

		/**
		 * Constructs a new Serializer
		 * 
		 * @param type Type it serializes
		 * @param handle NMS handle
		 * @param optional Whether or not it's {@link Optional}
		 */
		public Serializer(Class<?> type, Object handle, boolean optional) {
			super(HANDLE_TYPE);
			this.type = type;
			this.optional = optional;

			setHandle(handle);
		}

		/**
		 * Gets the type this serializer serializes.
		 * 
		 * @return The type
		 */
		public Class<?> getType() {
			return type;
		}

		/**
		 * Whether or not this serializer is optional, that is whether or not the return type is wrapped in a {@link Optional}.
		 * 
		 * @return True if it is, false if not
		 */
		public boolean isOptional() {
			return optional;
		}

		@Override
		public String toString() {
			return "Serializer[type=" + type + ", handle=" + handle + ", optional=" + optional + "]";
		}
	}

	/**
	 * Represents a DataWatcherRegistry containing the supported {@link Serializer}s in 1.9.
	 * 
	 * @author dmulloy2
	 */
	public static class Registry {
		private static boolean INITIALIZED = false;
		private static Map<Class<?>, Serializer> REGISTRY = new HashMap<>();

		/**
		 * Gets the serializer associated with a given class. </br>
		 * <b>Note</b>: If {@link Serializer#isOptional()}, the values must be wrapped in {@link Optional}
		 * 
		 * @param clazz Class to find serializer for
		 * @return The serializer, or null if none exists
		 */
		public static Serializer get(Class<?> clazz) {
			Validate.notNull("Class cannot be null!");
			initialize();

			return REGISTRY.get(clazz);
		}

		/**
		 * Gets the serializer associated with a given NMS handle.
		 * @param handle The NMS handle
		 * @return The serializer, or null if none exists
		 */
		public static Serializer fromHandle(Object handle) {
			Validate.notNull("Handle cannot be null!");
			initialize();

			for (Serializer serializer : REGISTRY.values()) {
				if (serializer.getHandle().equals(handle)) {
					return serializer;
				}
			}

			return null;
		}

		private static void initialize() {
			if (!INITIALIZED) {
				INITIALIZED = true;
			} else {
				return;
			}

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