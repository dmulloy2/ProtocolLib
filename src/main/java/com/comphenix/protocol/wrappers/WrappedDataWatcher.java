/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2018 dmulloy2
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

import java.lang.reflect.*;
import java.util.*;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a DataWatcher
 * @author dmulloy2
 */
public class WrappedDataWatcher extends AbstractWrapper implements Iterable<WrappedWatchableObject>, ClonableWrapper {
	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherClass();

	private static MethodAccessor GETTER = null;
	private static MethodAccessor SETTER = null;
	private static MethodAccessor REGISTER = null;

	private static FieldAccessor ENTITY_DATA_FIELD = null;
	private static FieldAccessor ENTITY_FIELD = null;
	private static FieldAccessor MAP_FIELD = null;

	private static ConstructorAccessor constructor = null;
	private static ConstructorAccessor eggConstructor = null;

	private static Object fakeEntity = null;

	// ---- Construction

	/**
	 * Constructs a new DataWatcher wrapper around a NMS handle. The resulting
	 * DataWatcher will likely have existing values that can be removed with
	 * {@link #clear()}.
	 * 
	 * @param handle DataWatcher handle
	 */
	public WrappedDataWatcher(Object handle) {
		super(HANDLE_TYPE);
		setHandle(handle);
	}

	/**
	 * Constructs a new DataWatcher using a fake egg entity. The
	 * resulting DataWatcher will not have any keys or values and new ones will
	 * have to be added using watcher objects.
	 */
	public WrappedDataWatcher() {
		this(newHandle(fakeEntity()));
	}

	/**
	 * Constructs a new DataWatcher using a real entity. The resulting
	 * DataWatcher will not have any keys or values and new ones will have to
	 * be added using watcher objects.
	 * 
	 * @param entity The entity
	 */
	public WrappedDataWatcher(Entity entity) {
		this(newHandle(BukkitUnwrapper.getInstance().unwrapItem(entity)));
	}

	/**
	 * Constructs a new DataWatcher using a fake egg entity and a given
	 * list of watchable objects.
	 * 
	 * @param objects The list of objects
	 */
	public WrappedDataWatcher(List<WrappedWatchableObject> objects) {
		this();

		if (MinecraftReflection.watcherObjectExists()) {
			for (WrappedWatchableObject object : objects) {
				setObject(object.getWatcherObject(), object);
			}
		} else {
			for (WrappedWatchableObject object : objects) {
				setObject(object.getIndex(), object);
			}
		}
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

		// We can create a fake egg without it affecting anything
		// Mojang added difficulty to lightning strikes, so this'll have to do
		if (eggConstructor == null) {
			eggConstructor = Accessors.getConstructorAccessor(
					MinecraftReflection.getMinecraftClass("world.entity.projectile.EntityEgg", "world.entity.projectile.ThrownEgg", "EntityEgg"),
					MinecraftReflection.getNmsWorldClass(), double.class, double.class, double.class
			);
		}

		Object world = BukkitUnwrapper.getInstance().unwrapItem(Bukkit.getWorlds().get(0));
		return fakeEntity = eggConstructor.invoke(world, 0, 0, 0);
	}

	// ---- Collection Methods

	@SuppressWarnings("unchecked")
	private Map<Integer, Object> getMap() {
		if (MAP_FIELD == null) {
			try {
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
				MAP_FIELD = Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract
						.newBuilder()
						.banModifier(Modifier.STATIC)
						.typeDerivedOf(Map.class)
						.build()));
			} catch (IllegalArgumentException ex) {
				throw new FieldAccessException("Failed to find watchable object map");
			}
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
	 * @deprecated Renamed to {@link #remove(int)}
	 */
	@Deprecated
	public WrappedWatchableObject removeObject(int index) {
		return remove(index);
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

	/**
	 * Returns a set containing all the registered indexes
	 * @return The set
	 */
	public Set<Integer> indexSet() {
		return getMap().keySet();
	}

	/**
	 * Clears the contents of this DataWatcher. The watcher will be empty after
	 * this operation is called.
	 */
	public void clear() {
		getMap().clear();
	}

	// ---- Object Getters

	/**
	 * Get a watched byte.
	 * 
	 * @param index - index of the watched byte.
	 * @return The watched byte, or NULL if this value doesn't exist.
	 */
	public Byte getByte(int index) {
		return (Byte) getObject(index);
	}

	/**
	 * Get a watched short.
	 * 
	 * @param index - index of the watched short.
	 * @return The watched short, or NULL if this value doesn't exist.
	 */
	public Short getShort(int index) {
		return (Short) getObject(index);
	}

	/**
	 * Get a watched integer.
	 * 
	 * @param index - index of the watched integer.
	 * @return The watched integer, or NULL if this value doesn't exist.
	 */
	public Integer getInteger(int index) {
		return (Integer) getObject(index);
	}

	/**
	 * Get a watched float.
	 * 
	 * @param index - index of the watched float.
	 * @return The watched float, or NULL if this value doesn't exist.
	 */
	public Float getFloat(int index) {
		return (Float) getObject(index);
	}

	/**
	 * Get a watched string.
	 * 
	 * @param index - index of the watched string.
	 * @return The watched string, or NULL if this value doesn't exist.
	 */
	public String getString(int index) {
		return (String) getObject(index);
	}

	/**
	 * Get a watched string.
	 * 
	 * @param index - index of the watched string.
	 * @return The watched string, or NULL if this value doesn't exist.
	 */
	public ItemStack getItemStack(int index) {
		return (ItemStack) getObject(index);
	}

	/**
	 * Retrieve a watchable object by index.
	 * 
	 * @param index Index of the object to retrieve.
	 * @return The watched object or null if it doesn't exist.
	 */
	public Object getObject(int index) {
		return getObject(WrappedDataWatcherObject.fromIndex(index));
	}

	/**
	 * Retrieve a watchable object by watcher object.
	 * 
	 * @param object The watcher object
	 * @return The watched object or null if it doesn't exist.
	 */
	public Object getObject(WrappedDataWatcherObject object) {
		Validate.notNull(object, "Watcher object cannot be null!");

		if (GETTER == null) {
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);

			if (MinecraftReflection.watcherObjectExists()) {			
				GETTER = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
					.parameterExactType(object.getHandleType())
					.returnTypeExact(Object.class)
					.build(), "get"));
			} else {
				GETTER = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
					.parameterExactType(int.class)
					.returnTypeExact(MinecraftReflection.getDataWatcherItemClass())
					.build()));
			}
		}

		try {
			Object value = GETTER.invoke(handle, object.getHandle());
			return WrappedWatchableObject.getWrapped(value);
		} catch (Exception ex) {
			// Nothing exists at this index
			return null;
		}
	}

	// ---- Object Setters

	/**
	 * Sets the DataWatcher Item at a given index to a new value. In 1.9 and up,
	 * you cannot register objects without a watcher object.
	 * 
	 * @param index Index of the object to set
	 * @param value New value
	 * @param update Whether or not to inform the client
	 * 
	 * @see WrappedDataWatcher#setObject(WrappedDataWatcherObject, Object, boolean)
	 * @throws IllegalArgumentException in 1.9 and up if there isn't already an
	 * 		object at this index
	 */
	public void setObject(int index, Object value, boolean update) {
		if (MinecraftReflection.watcherObjectExists() && !hasIndex(index)) {
			throw new IllegalArgumentException("You cannot register objects without a watcher object!");
		}

		setObject(WrappedDataWatcherObject.fromIndex(index), value, update);
	}

	/**
	 * Shortcut for {@link #setObject(int, Object, boolean)}
	 */
	public void setObject(int index, Object value) {
		setObject(index, value, false);
	}

	/**
	 * Sets the DataWatcher Item at a given index to a new value.
	 * 
	 * @param index Index of the object to set
	 * @param serializer Serializer from {@link Registry#get(Class)}
	 * @param value New value
	 * @param update Whether or not to inform the client
	 * 
	 * @see WrappedDataWatcher#setObject(WrappedDataWatcherObject, Object)
	 */
	public void setObject(int index, Serializer serializer, Object value, boolean update) {
		setObject(new WrappedDataWatcherObject(index, serializer), value, update);
	}

	/**
	 * Alias for {@link #setObject(int, Serializer, Object, boolean)}
	 */
	public void setObject(int index, Serializer serializer, Object value) {
		setObject(new WrappedDataWatcherObject(index, serializer), value, false);
	}

	/**
	 * Sets the DataWatcher Item at a given index to a new value.
	 * 
	 * @param index Index of the object to set
	 * @param value New value
	 * @param update Whether or not to inform the client
	 * 
	 * @see WrappedDataWatcher#setObject(int, Object, boolean)
	 */
	public void setObject(int index, WrappedWatchableObject value, boolean update) {
		setObject(index, value.getRawValue(), update);
	}

	/**
	 * Alias for {@link #setObject(int, WrappedWatchableObject, boolean)}
	 */
	public void setObject(int index, WrappedWatchableObject value) {
		setObject(index, value.getRawValue(), false);
	}

	/**
	 * Sets the DataWatcher Item associated with a given watcher object to a new value.
	 * 
	 * @param object Associated watcher object
	 * @param value Wrapped value
	 * @param update Whether or not to inform the client
	 * 
	 * @see #setObject(WrappedDataWatcherObject, Object)
	 */
	public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value, boolean update) {
		setObject(object, value.getRawValue(), update);
	}

	/**
	 * Shortcut for {@link #setObject(WrappedDataWatcherObject, WrappedWatchableObject, boolean)}
	 */
	public void setObject(WrappedDataWatcherObject object, WrappedWatchableObject value) {
		setObject(object, value.getRawValue(), false);
	}

	/**
	 * Sets the DataWatcher Item associated with a given watcher object to a
	 * new value. If there is not already an object at this index, the
	 * specified watcher object must have a serializer.
	 * 
	 * @param object Associated watcher object
	 * @param value New value
	 * 
	 * @throws IllegalArgumentException If the watcher object is null or must
	 * 			have a serializer and does not have one.
	 */
	public void setObject(WrappedDataWatcherObject object, Object value, boolean update) {
		Validate.notNull(object, "Watcher object cannot be null!");

		if (SETTER == null && REGISTER == null) {
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(handleType, true);
			FuzzyMethodContract contract = FuzzyMethodContract.newBuilder()
					.banModifier(Modifier.STATIC)
					.requireModifier(Modifier.PUBLIC)
					.parameterExactArray(object.getHandleType(), Object.class)
					.build();
			List<Method> methods = fuzzy.getMethodList(contract);
			for (Method method : methods) {
				if (method.getName().equals("set") || method.getName().equals("watch") || method.getName().equals("b")) {
					SETTER = Accessors.getMethodAccessor(method);
				} else {
					REGISTER = Accessors.getMethodAccessor(method);
				}
			}
		}

		// Unwrap the object
		value = WrappedWatchableObject.getUnwrapped(value);

		if (hasIndex(object.getIndex())) {
			SETTER.invoke(handle, object.getHandle(), value);
		} else {
			object.checkSerializer();
			REGISTER.invoke(handle, object.getHandle(), value);
		}

		if (update) {
			getWatchableObject(object.getIndex()).setDirtyState(update);
		}
	}

	/**
	 * Shortcut for {@link #setObject(WrappedDataWatcherObject, Object, boolean)}
	 */
	public void setObject(WrappedDataWatcherObject object, Object value) {
		setObject(object, value, false);
	}

	// ---- Utility Methods

	/**
	 * Clone the content of the current DataWatcher.
	 * 
	 * @return A cloned data watcher.
	 */
	public WrappedDataWatcher deepClone() {
		WrappedDataWatcher clone = new WrappedDataWatcher(getEntity());

		if (MinecraftReflection.watcherObjectExists()) {
			for (WrappedWatchableObject wrapper : this) {
				clone.setObject(wrapper.getWatcherObject(), wrapper);
			}
		} else {
			for (WrappedWatchableObject wrapper : this) {
				clone.setObject(wrapper.getIndex(), wrapper);
			}
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

		Object entity = ENTITY_FIELD.get(handle);
		if (entity == null) {
			throw new NullPointerException(handle + "." + ENTITY_FIELD);
		}

		return (Entity) MinecraftReflection.getBukkitEntity(entity);
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

	private static final ImmutableBiMap<Class<?>, Integer> CLASS_TO_ID = new ImmutableBiMap.Builder<Class<?>, Integer>()
			.put(Byte.class, 0)
			.put(Short.class, 1)
			.put(Integer.class, 2)
			.put(Float.class, 3)
			.put(String.class, 4)
			.put(MinecraftReflection.getItemStackClass(), 5)
			.put(MinecraftReflection.getBlockPositionClass(), 6)
			.put(Vector3F.getMinecraftClass(), 7)
			.build();

	/**
	 * Retrieves the type ID associated with a given class. No longer supported
	 * in 1.9 and up due to the removal of type IDs.
	 * 
	 * @param clazz Class to find ID for
	 * @return The ID, or null if not found
	 */
	public static Integer getTypeID(Class<?> clazz) {
		return CLASS_TO_ID.get(clazz);
	}

	/**
	 * Retrieves the class associated with a given type ID. No longer
	 * supported in 1.9 and up due to the removal of type IDs.
	 * 
	 * @param typeID ID to find Class for
	 * @return The Class, or null if not found
	 */
	public static Class<?> getTypeClass(int typeID) {
		return CLASS_TO_ID.inverse().get(typeID);
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
	 * Represents a DataWatcherObject in 1.9. In order to register an object,
	 * the serializer must be specified.
	 * 
	 * @author dmulloy2
	 */
	public static class WrappedDataWatcherObject {
		private static final Class<?> HANDLE_TYPE = MinecraftReflection.getDataWatcherObjectClass();
		private static ConstructorAccessor constructor = null;
		private static MethodAccessor getSerializer = null;

		private StructureModifier<Object> modifier;
		private Object handle;

		protected WrappedDataWatcherObject() {
		}

		/**
		 * Creates a new watcher object from a NMS handle.
		 * 
		 * @param handle The handle
		 */
		public WrappedDataWatcherObject(Object handle) {
			this.handle = handle;
			this.modifier = new StructureModifier<>(HANDLE_TYPE).withTarget(handle);
		}

		/**
		 * Creates a new watcher object from an index and serializer.
		 * 
		 * @param index Index
		 * @param serializer Serializer, see {@link Registry}
		 */
		public WrappedDataWatcherObject(int index, Serializer serializer) {
			this(newHandle(index, serializer));
		}

		static WrappedDataWatcherObject fromIndex(int index) {
			if (MinecraftReflection.watcherObjectExists()) {
				return new WrappedDataWatcherObject(newHandle(index));
			} else {
				return new DummyWatcherObject(index);
			}
		}

		private static Object newHandle(int index) {
			Validate.isTrue(index >= 0, "index cannot be negative!");

			if (constructor == null) {
				constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
			}

			return constructor.invoke(index, null);
		}

		private static Object newHandle(int index, Serializer serializer) {
			Validate.isTrue(index >= 0, "index cannot be negative!");
			Validate.notNull(serializer, "serializer cannot be null!");

			if (constructor == null) {
				constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
			}

			Object handle = serializer.getHandle();
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
		 * Gets this watcher object's serializer. Will return null if the
		 * serializer was never specified.
		 * 
		 * @return The serializer, or null
		 */
		public Serializer getSerializer() {
			if (getSerializer == null) {
				getSerializer = Accessors.getMethodAccessor(FuzzyReflection.fromClass(HANDLE_TYPE, true)
						.getMethodByReturnTypeAndParameters("getSerializer", MinecraftReflection.getDataWatcherSerializerClass(), new Class[0]));
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

		public void checkSerializer() {
			Validate.notNull(getSerializer(), "You must specify a serializer to register an object!");
		}

		public Object getHandle() {
			return handle;
		}

		public Class<?> getHandleType() {
			return HANDLE_TYPE;
		}

		@Override
		public String toString() {
			return "DataWatcherObject[index=" + getIndex() + ", serializer=" + getSerializer() + "]";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;

			if (obj instanceof WrappedDataWatcherObject) {
				WrappedDataWatcherObject other = (WrappedDataWatcherObject) obj;
				return handle.equals(other.handle);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return handle.hashCode();
		}
	}

	private static class DummyWatcherObject extends WrappedDataWatcherObject {
		private final int index;

		public DummyWatcherObject(int index) {
			this.index = index;
		}

		@Override
		public int getIndex() {
			return index;
		}

		@Override
		public Serializer getSerializer() {
			return null;
		}

		@Override
		public Object getHandle() {
			return getIndex();
		}

		@Override
		public Class<?> getHandleType() {
			return int.class;
		}

		@Override
		public void checkSerializer() {
			// Do nothing
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null) return false;

			if (obj instanceof DummyWatcherObject) {
				DummyWatcherObject that = (DummyWatcherObject) obj;
				return this.index == that.index;
			}

			return false;
		}
	}

	/**
	 * Represents a DataWatcherSerializer in 1.9. If a Serializer is optional,
	 * values must be wrapped in a {@link Optional}.
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
		 * Whether or not this serializer is optional, that is whether or not
		 * the return type is wrapped in a {@link Optional}.
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
	 * Represents a DataWatcherRegistry containing the supported
	 * {@link Serializer}s in 1.9.
	 *
	 * <ul>
	 *   <li>Byte</li>
	 *   <li>Integer</li>
	 *   <li>Float</li>
	 *   <li>String</li>
	 *   <li>IChatBaseComponent</li>
	 *   <li>ItemStack</li>
	 *   <li>Optional&lt;IBlockData&gt;</li>
	 *   <li>Boolean</li>
	 *   <li>Vector3f</li>
	 *   <li>BlockPosition</li>
	 *   <li>Optional&lt;BlockPosition&gt;</li>
	 *   <li>EnumDirection</li>
	 *   <li>Optional&lt;UUID&gt;</li>
	 *   <li>NBTTagCompound</li>
	 * </ul>
	 *
	 * @author dmulloy2
	 */
	public static class Registry {
		private static boolean INITIALIZED = false;
		private static List<Serializer> REGISTRY = new ArrayList<>();

		/**
		 * Gets the first serializer associated with a given class.
		 *
		 * <p><b>Note</b>: If {@link Serializer#isOptional() the serializer is optional},
		 *   values <i>must</i> be wrapped in an {@link Optional}.</p>
		 *
		 * <p>If there are multiple serializers for a given class (i.e. BlockPosition),
		 *   you should use {@link #get(Class, boolean)} for more precision.</p>
		 *
		 * @param clazz Class to find serializer for
		 * @return The serializer, or null if none exists
		 */
		public static Serializer get(Class<?> clazz) {
			Validate.notNull(clazz,"Class cannot be null!");
			initialize();

			for (Serializer serializer : REGISTRY) {
				if (serializer.getType().equals(clazz)) {
					return serializer;
				}
			}

			throw new IllegalArgumentException("No serializer found for " + clazz);
		}

		/**
		 * Gets the first serializer associated with a given class and optional state.
		 * <br/>
		 * <b>Note</b>: If the serializer is optional, values <i>must</i> be
		 * wrapped in an {@link Optional}
		 *
		 * @param clazz Class to find serializer for
		 * @param optional Optional state
		 * @return The serializer, or null if none exists
		 */
		public static Serializer get(Class<?> clazz, boolean optional) {
			Validate.notNull(clazz, "Class cannot be null!");
			initialize();

			Validate.notEmpty(REGISTRY, "Registry has no elements!");

			for (Serializer serializer : REGISTRY) {
				if (serializer.getType().equals(clazz)
					&& serializer.isOptional() == optional) {
					return serializer;
				}
			}

			throw new IllegalArgumentException("No serializer found for " + (optional ? "Optional<" + clazz + ">" : clazz));
		}

		/**
		 * Gets the serializer associated with a given NMS handle.
		 * @param handle The handle
		 * @return The serializer, or null if none exists
		 */
		public static Serializer fromHandle(Object handle) {
			Validate.notNull(handle, "handle cannot be null!");
			initialize();

			for (Serializer serializer : REGISTRY) {
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

			List<Field> candidates = FuzzyReflection.fromClass(MinecraftReflection.getDataWatcherRegistryClass(), true)
					.getFieldListByType(MinecraftReflection.getDataWatcherSerializerClass());
			for (Field candidate : candidates) {
				Type generic = candidate.getGenericType();
				if (generic instanceof ParameterizedType) {
					ParameterizedType type = (ParameterizedType) generic;
					Type[] args = type.getActualTypeArguments();
					Type arg = args[0];

					Class<?> innerClass;
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

					if (serializer == null) {
						throw new RuntimeException("Failed to read serializer: " + candidate.getName());
					}

					REGISTRY.add(new Serializer(innerClass, serializer, optional));
				}
			}
		}

		// ---- Helper methods

		/**
		 * Gets the serializer for IChatBaseComponents
		 * @return The serializer
		 */
		public static Serializer getChatComponentSerializer() {
			return getChatComponentSerializer(false);
		}

		/**
		 * Gets the serializer for IChatBaseComponents
		 * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
		 * @return The serializer
		 */
		public static Serializer getChatComponentSerializer(boolean optional) {
			return get(MinecraftReflection.getIChatBaseComponentClass(), optional);
		}

		/**
		 * Gets the serializer for ItemStacks
		 * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
		 * @return The serializer
		 */
		public static Serializer getItemStackSerializer(boolean optional) {
			return get(MinecraftReflection.getItemStackClass(), optional);
		}

		/**
		 * Gets the serializer for BlockData
		 * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
		 * @return The serializer
		 */
		public static Serializer getBlockDataSerializer(boolean optional) {
			return get(MinecraftReflection.getIBlockDataClass(), optional);
		}

		/**
		 * Gets the serializer for Vector3Fs
		 * @return The serializer
		 */
		public static Serializer getVectorSerializer() {
			return get(Vector3F.getMinecraftClass());
		}

		/**
		 * Gets the serializer for BlockPositions
		 * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
		 * @return The serializer
		 */
		public static Serializer getBlockPositionSerializer(boolean optional) {
			return get(MinecraftReflection.getBlockPositionClass(), optional);
		}

		/**
		 * Gets the serializer for Directions
		 * @return The serializer
		 */
		public static Serializer getDirectionSerializer() {
			return get(EnumWrappers.getDirectionClass());
		}

		/**
		 * Gets the serializer for UUIDs
		 * @param optional If true, objects <b>must</b> be wrapped in an {@link Optional}
		 * @return The serializer
		 */
		public static Serializer getUUIDSerializer(boolean optional) {
			return get(UUID.class, optional);
		}

		/**
		 * Gets the serializer for NBT Compound tags
		 * @return The serializer
		 */
		public static Serializer getNBTCompoundSerializer() {
			return get(MinecraftReflection.getNBTCompoundClass(), false);
		}
	}
}
