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

package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.annotations.Spigot;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.ReadOnlyFieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.collection.ConvertedMap;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

/**
 * Wraps a DataWatcher that is used to transmit arbitrary key-value pairs with a given entity.
 *
 * @author Kristian
 */
public class WrappedDataWatcher extends AbstractWrapper implements Iterable<WrappedWatchableObject> {
	/**
	 * Every custom watchable type in Spigot #1628 and above.
	 * @author Kristian
	 */
	@Spigot(minimumBuild = 1628)
	public enum CustomType {
		BYTE_SHORT("org.spigotmc.ProtocolData$ByteShort", 		0, 	short.class),
		DUAL_BYTE("org.spigotmc.ProtocolData$DualByte", 		0, 	byte.class, byte.class),
		HIDDEN_BYTE("org.spigotmc.ProtocolData$HiddenByte", 	0, 	byte.class),
		INT_BYTE("org.spigotmc.ProtocolData$IntByte", 			2, 	int.class, byte.class),
		DUAL_INT("org.spigotmc.ProtocolData$DualInt", 			2, 	int.class, int.class);

		private Class<?> spigotClass;
		private ConstructorAccessor constructor;
		private FieldAccessor secondaryValue;
		private int typeId;

		private CustomType(String className, int typeId, Class<?>... parameters) {
			try {
				this.spigotClass = Class.forName(className);
				this.constructor = Accessors.getConstructorAccessor(spigotClass, parameters);
				this.secondaryValue = parameters.length > 1 ? Accessors.getFieldAccessor(spigotClass, "value2", true) : null;

			} catch (ClassNotFoundException e) {
				System.out.println("[ProtocolLib] Unable to find " + className);
				this.spigotClass = null;
			}
			this.typeId = typeId;
		}

		/**
		 * Construct a new instance of this Spigot type.
		 * @param value - the value. Cannot be NULL.
		 * @return The instance to construct.
		 */
		Object newInstance(Object value) {
			return newInstance(value, null);
		}

		/**
		 * Construct a new instance of this Spigot type.
		 * <p>
		 * The secondary value may be NULL if this custom type does not contain a secondary value.
		 * @param value - the value.
		 * @param secondary - optional secondary value.
		 * @return
		 */
		Object newInstance(Object value, Object secondary) {
			Preconditions.checkNotNull(value, "value cannot be NULL.");

			if (hasSecondary()) {
				return constructor.invoke(value, secondary);
			} else {
				if (secondary != null) {
					throw new IllegalArgumentException("Cannot construct " + this + " with a secondary value");
				}
				return constructor.invoke(value);
			}
		}

		/**
		 * Set the secondary value of a given type.
		 * @param instance - the instance.
		 * @param secondary - the secondary value.
		 */
		void setSecondary(Object instance, Object secondary)  {
			if (!hasSecondary()) {
				throw new IllegalArgumentException(this + " does not have a secondary value.");
			}
			secondaryValue.set(instance, secondary);
		}

		/**
		 * Get the secondary value of a type.
		 * @param instance - the instance.
		 * @return The secondary value.
		 */
		Object getSecondary(Object instance) {
			if (!hasSecondary()) {
				throw new IllegalArgumentException(this + " does not have a secondary value.");
			}
			return secondaryValue.get(instance);
		}

		/**
		 * Determine if this type has a secondary value.
		 * @return TRUE if it does, FALSE otherwise.
		 */
		public boolean hasSecondary() {
			return secondaryValue != null;
		}

		/**
		 * Underlying Spigot class.
		 * @return The class.
		 */
		public Class<?> getSpigotClass() {
			return spigotClass;
		}

		/**
		 * The equivalent type ID.
		 * @return The equivalent ID.
		 */
		public int getTypeId() {
			return typeId;
		}

		/**
		 * Retrieve the custom Spigot type of a value.
		 * @param value - the value.
		 * @return The Spigot type, or NULL if not found.
		 */
		@Spigot(minimumBuild = 1628)
		public static CustomType fromValue(Object value) {
			for (CustomType type : CustomType.values()) {
				if (type.getSpigotClass().isInstance(value)) {
					return type;
				}
			}
			return null;
		}
	}

	/**
	 * Used to assign integer IDs to given types.
	 */
	private static Map<Class<?>, Integer> TYPE_MAP;

	/**
	 * Custom types in the bountiful update.
	 */
	private static Map<Class<?>, Integer> CUSTOM_MAP;

	// Accessors
	private static FieldAccessor TYPE_MAP_ACCESSOR;
	private static FieldAccessor VALUE_MAP_ACCESSOR;

	// Fields
	private static Field READ_WRITE_LOCK_FIELD;
	private static Field ENTITY_FIELD;

	// Methods
	private static Method CREATE_KEY_VALUE_METHOD;
	private static Method UPDATE_KEY_VALUE_METHOD;
	private static Method GET_KEY_VALUE_METHOD;

	// Constructors
	private static Constructor<?> CREATE_DATA_WATCHER_CONSTRUCTOR;

	// Entity methods
	private volatile static Field ENTITY_DATA_FIELD;

	/**
	 * Whether or not this class has already been initialized.
	 */
	private static boolean HAS_INITIALIZED;

	// Lock
	private ReadWriteLock readWriteLock;

	// Map of watchable objects
	private Map<Integer, Object> watchableObjects;

	// A map view of all the watchable objects
	private Map<Integer, WrappedWatchableObject> mapView;

	/**
	 * Initialize a new data watcher.
	 * @throws FieldAccessException If we're unable to wrap a DataWatcher.
	 */
	public WrappedDataWatcher() {
		super(MinecraftReflection.getDataWatcherClass());

		// Just create a new watcher
		try {
			if (MinecraftReflection.isUsingNetty()) {
				setHandle(newEntityHandle(null));
			} else {
				setHandle(getHandleType().newInstance());
			}
			initialize();

		} catch (Exception e) {
			throw new RuntimeException("Unable to construct DataWatcher.", e);
		}
	}

	/**
	 * Create a wrapper for a given data watcher.
	 * @param handle - the data watcher to wrap.
	 * @throws FieldAccessException If we're unable to wrap a DataWatcher.
	 */
	public WrappedDataWatcher(Object handle) {
		super(MinecraftReflection.getDataWatcherClass());
		setHandle(handle);
		initialize();
	}

	/**
	 * Construct a new data watcher with the given entity.
	 * <p>
	 * In 1.6.4 and ealier, this will fall back to using {@link #WrappedDataWatcher()}.
	 * @param entity - the entity.
	 * @return The wrapped data watcher.
	 */
	public static WrappedDataWatcher newWithEntity(Entity entity) {
		// Use the old constructor
		if (!MinecraftReflection.isUsingNetty())
			return new WrappedDataWatcher();
		return new WrappedDataWatcher(newEntityHandle(entity));
	}

	/**
	 * Construct a new native DataWatcher with the given entity.
	 * <p>
	 * Warning: This is only supported in 1.7.2 and above.
	 * @param entity - the entity, or NULL.
	 * @return The data watcher.
	 */
	private static Object newEntityHandle(Entity entity) {
		Class<?> dataWatcher = MinecraftReflection.getDataWatcherClass();

		try {
			if (CREATE_DATA_WATCHER_CONSTRUCTOR == null)
				CREATE_DATA_WATCHER_CONSTRUCTOR = dataWatcher.getConstructor(MinecraftReflection.getEntityClass());

			return CREATE_DATA_WATCHER_CONSTRUCTOR.newInstance(
					BukkitUnwrapper.getInstance().unwrapItem(entity)
			);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct data watcher.", e);
		}
	}

	/**
	 * Create a new data watcher for a list of watchable objects.
	 * <p>
	 * Note that the watchable objects are not cloned, and will be modified in place. Use "deepClone" if
	 * that is not desirable.
	 * <p>
	 * The {@link #removeObject(int)} method will not modify the given list, however.
	 *
	 * @param watchableObjects - list of watchable objects that will be copied.
	 * @throws FieldAccessException Unable to read watchable objects.
	 */
	public WrappedDataWatcher(List<WrappedWatchableObject> watchableObjects) throws FieldAccessException {
		this();

		Lock writeLock = getReadWriteLock().writeLock();
		Map<Integer, Object> map = getWatchableObjectMap();

		writeLock.lock();

		try {
			// Add the watchable objects by reference
			for (WrappedWatchableObject watched : watchableObjects) {
				map.put(watched.getIndex(), watched.handle);
			}
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Retrieve the ID of a given type, if it's allowed to be watched.
	 * @return The ID, or NULL if it cannot be watched.
	 * @throws FieldAccessException If we cannot initialize the reflection machinery.
	 */
	public static Integer getTypeID(Class<?> clazz) throws FieldAccessException {
		initialize();
		Integer result = TYPE_MAP.get(WrappedWatchableObject.getUnwrappedType(clazz));

		if (result == null) {
			result = CUSTOM_MAP.get(clazz);
		}
		return result;
	}

	/**
	 * Retrieve the type of a given ID, if it's allowed to be watched.
	 * @return The type using a given ID, or NULL if it cannot be watched.
	 * @throws FieldAccessException If we cannot initialize the reflection machinery.
	 */
	public static Class<?> getTypeClass(int id) throws FieldAccessException {
		initialize();

		for (Map.Entry<Class<?>, Integer> entry : TYPE_MAP.entrySet()) {
			if (Objects.equal(entry.getValue(), id)) {
				return entry.getKey();
			}
		}

		// Unknown class type
		return null;
	}

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
    	// The get method will take care of concurrency
    	WrappedWatchableObject object = getWrappedObject(index);
    	return object != null ? object.getValue() : null;
    }

    /**
     * Retrieve the wrapped object by index.
     * @param index - the index.
     * @return The corresponding wrapper, or NULL.
     */
    @Spigot(minimumBuild = 1628)
    public WrappedWatchableObject getWrappedObject(int index) {
    	// The get method will take care of concurrency
    	Object watchable = getWatchedObject(index);

    	if (watchable != null) {
    		return new WrappedWatchableObject(watchable);
    	} else {
    		return null;
    	}
    }

    /**
     * Retrieve every watchable object in this watcher.
     * @return Every watchable object.
     * @throws FieldAccessException If reflection failed.
     */
	public List<WrappedWatchableObject> getWatchableObjects() throws FieldAccessException {
		Lock readLock = getReadWriteLock().readLock();
		readLock.lock();

    	try {
    		List<WrappedWatchableObject> result = new ArrayList<WrappedWatchableObject>();

    		// Add each watchable object to the list
    		for (Object watchable : getWatchableObjectMap().values()) {
    			if (watchable != null) {
    				result.add(new WrappedWatchableObject(watchable));
    			} else {
    				result.add(null);
    			}
    		}
    		return result;

    	} finally {
    		readLock.unlock();
    	}
    }

	@Override
	public boolean equals(Object obj) {
		// Quick checks
		if (obj == this)
			return true;
		if (obj == null)
			return false;

		if (obj instanceof WrappedDataWatcher) {
			WrappedDataWatcher other = (WrappedDataWatcher) obj;
			Iterator<WrappedWatchableObject> first = iterator(), second = other.iterator();

			// Make sure they're the same size
			if (size() != other.size())
				return false;

			for (; first.hasNext() && second.hasNext(); ) {
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

    /**
     * Retrieve a copy of every index associated with a watched object.
     * @return Every watched object index.
     * @throws FieldAccessException If we're unable to read the underlying object.
     */
    public Set<Integer> indexSet() throws FieldAccessException {
    	Lock readLock = getReadWriteLock().readLock();
		readLock.lock();

    	try {
    		return new HashSet<Integer>(getWatchableObjectMap().keySet());
    	} finally {
    		readLock.unlock();
    	}
    }

    /**
     * Clone the content of the current DataWatcher.
     * @return A cloned data watcher.
     */
    public WrappedDataWatcher deepClone() {
    	WrappedDataWatcher clone = new WrappedDataWatcher();

    	// Make a new copy instead
    	for (WrappedWatchableObject watchable : this) {
    		clone.setObject(watchable.getIndex(), watchable.getClonedValue());
    	}
    	return clone;
    }

    /**
     * Retrieve the number of watched objects.
     * @return Watched object count.
     * @throws FieldAccessException If we're unable to read the underlying object.
     */
    public int size() throws FieldAccessException {
    	Lock readLock = getReadWriteLock().readLock();
    	readLock.lock();

    	try {
    		return getWatchableObjectMap().size();
    	} finally {
    		readLock.unlock();
    	}
    }

    /**
     * Remove a given object from the underlying DataWatcher.
     * @param index - index of the object to remove.
     * @return The watchable object that was removed, or NULL If none could be found.
     */
    public WrappedWatchableObject removeObject(int index) {
    	Lock writeLock = getReadWriteLock().writeLock();
    	writeLock.lock();

    	try {
    		Object removed = getWatchableObjectMap().remove(index);
    		return removed != null ? new WrappedWatchableObject(removed) : null;
    	} finally {
    		writeLock.unlock();
    	}
    }

    /**
     * Set a watched byte.
     * @param index - index of the watched byte.
     * @param newValue - the new watched value.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public void setObject(int index, Object newValue) throws FieldAccessException {
    	setObject(index, newValue, true);
    }

    /**
     * Set a watched byte.
     * @param index - index of the watched byte.
     * @param newValue - the new watched value.
     * @param update - whether or not to refresh every listening client.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public void setObject(int index, Object newValue, boolean update) throws FieldAccessException {
    	// Aquire write lock
    	Lock writeLock = getReadWriteLock().writeLock();
    	writeLock.lock();

    	try {
    		Object watchable = getWatchedObject(index);

	    	if (watchable != null) {
	    		new WrappedWatchableObject(watchable).setValue(newValue, update);
	    	} else {
	    		CREATE_KEY_VALUE_METHOD.invoke(handle, index, WrappedWatchableObject.getUnwrapped(newValue));
	    	}

	    	// Handle invoking the method
    	} catch (IllegalArgumentException e) {
    		throw new FieldAccessException("Cannot convert arguments.", e);
		} catch (IllegalAccessException e) {
			throw new FieldAccessException("Illegal access.", e);
		} catch (InvocationTargetException e) {
			throw new FieldAccessException("Checked exception in Minecraft.", e);
		} finally {
    		writeLock.unlock();
    	}
    }

    /**
     * Set a watched byte with an optional secondary value.
     * @param index - index of the watched byte.
     * @param newValue - the new watched value.
     * @param secondary - optional secondary value.
     * @param update - whether or not to refresh every listening client.
     * @throws FieldAccessException Cannot read underlying field.
     */
    @Spigot(minimumBuild = 1628)
    public void setObject(int index, Object newValue, Object secondary, boolean update, CustomType type) throws FieldAccessException {
		Object created = type.newInstance(newValue, secondary);

		// Now update the watcher
		setObject(index, created, update);
    }

    private Object getWatchedObject(int index) throws FieldAccessException {
    	// We use the get-method first and foremost
    	if (GET_KEY_VALUE_METHOD != null) {
			try {
				return GET_KEY_VALUE_METHOD.invoke(handle, index);
			} catch (Exception e) {
				throw new FieldAccessException("Cannot invoke get key method for index " + index, e);
			}
    	} else {
    		try {
    			getReadWriteLock().readLock().lock();
    			return getWatchableObjectMap().get(index);

    		} finally {
    			getReadWriteLock().readLock().unlock();
    		}
    	}
    }

    /**
     * Retrieve the current read write lock.
     * @return Current read write lock.
     * @throws FieldAccessException If we're unable to read the underlying field.
     */
    protected ReadWriteLock getReadWriteLock() throws FieldAccessException {
    	try {
	    	// Cache the read write lock
	    	if (readWriteLock != null)
	    		return readWriteLock;
	    	else if (READ_WRITE_LOCK_FIELD != null)
	    		return readWriteLock = (ReadWriteLock) FieldUtils.readField(READ_WRITE_LOCK_FIELD, handle, true);
	    	else
	    		return readWriteLock = new ReentrantReadWriteLock();
    	} catch (IllegalAccessException e) {
    		throw new FieldAccessException("Unable to read lock field.", e);
    	}
    }

    /**
	 * Retrieve the underlying map of key values that stores watchable objects.
	 * @return A map of watchable objects.
	 * @throws FieldAccessException If we don't have permission to perform reflection.
	 */
	@SuppressWarnings("unchecked")
	protected Map<Integer, Object> getWatchableObjectMap() throws FieldAccessException {
		if (watchableObjects == null)
			watchableObjects = (Map<Integer, Object>) VALUE_MAP_ACCESSOR.get(handle);
		return watchableObjects;
	}

	/**
	 * Retrieve the data watcher associated with an entity.
	 * @param entity - the entity to read from.
	 * @return Associated data watcher.
	 * @throws FieldAccessException Reflection failed.
	 */
	public static WrappedDataWatcher getEntityWatcher(Entity entity) throws FieldAccessException {
		if (ENTITY_DATA_FIELD == null)
			ENTITY_DATA_FIELD = FuzzyReflection.fromClass(MinecraftReflection.getEntityClass(), true).
				getFieldByType("datawatcher", MinecraftReflection.getDataWatcherClass());

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
	 * Invoked when a data watcher is first used.
	 */
	@SuppressWarnings("unchecked")
	private static void initialize() throws FieldAccessException {
		// This method should only be run once, even if an exception is thrown
		if (!HAS_INITIALIZED)
			HAS_INITIALIZED = true;
		else
			return;

		FuzzyReflection fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getDataWatcherClass(), true);

		for (Field lookup : fuzzy.getFieldListByType(Map.class)) {
			if (Modifier.isStatic(lookup.getModifiers())) {
				// This must be the type map
				TYPE_MAP_ACCESSOR = Accessors.getFieldAccessor(lookup, true);
			} else {
				// If not, then we're probably dealing with the value map
				VALUE_MAP_ACCESSOR = Accessors.getFieldAccessor(lookup, true);
			}
		}
		// Spigot workaround (not necessary
		initializeSpigot(fuzzy);

		// Any custom types
		CUSTOM_MAP = initializeCustom();

		// Initialize static type type
		TYPE_MAP = (Map<Class<?>, Integer>) TYPE_MAP_ACCESSOR.get(null);

		try {
			READ_WRITE_LOCK_FIELD = fuzzy.getFieldByType("readWriteLock", ReadWriteLock.class);
		} catch (IllegalArgumentException e) {
			// It's not a big deal
		}

		// Check for the entity field as well
		if (MinecraftReflection.isUsingNetty()) {
			ENTITY_FIELD = fuzzy.getFieldByType("entity", MinecraftReflection.getEntityClass());
			ENTITY_FIELD.setAccessible(true);
		}
		initializeMethods(fuzzy);
	}

	// For Spigot's bountiful update patch
	private static Map<Class<?>, Integer> initializeCustom() {
		Map<Class<?>, Integer> map = Maps.newHashMap();

		for (CustomType type : CustomType.values()) {
			if (type.getSpigotClass() != null) {
				map.put(type.getSpigotClass(), type.getTypeId());
			}
		}
		return map;
	}

	// TODO: Remove, as this was fixed in build #1189 of Spigot
	private static void initializeSpigot(FuzzyReflection fuzzy) {
		// See if the workaround is needed
		if (TYPE_MAP_ACCESSOR != null && VALUE_MAP_ACCESSOR != null)
			return;

		for (Field lookup : fuzzy.getFields()) {
			final Class<?> type = lookup.getType();

			if (TroveWrapper.isTroveClass(type)) {
				// Create a wrapper accessor
				final ReadOnlyFieldAccessor accessor = TroveWrapper.wrapMapField(
				  Accessors.getFieldAccessor(lookup, true), new Function<Integer, Integer>() {
					@Override
					public Integer apply(@Nullable Integer value) {
						// Do not use zero for no entry value
						if (value == 0)
							return -1;
						return value;
					}
				});

				if (Modifier.isStatic(lookup.getModifiers())) {
					TYPE_MAP_ACCESSOR = accessor;
				} else {
					VALUE_MAP_ACCESSOR = accessor;
				}
			}
		}

		if (TYPE_MAP_ACCESSOR == null)
			throw new IllegalArgumentException("Unable to find static type map.");
		if (VALUE_MAP_ACCESSOR == null)
			throw new IllegalArgumentException("Unable to find static value map.");
	}

	private static void initializeMethods(FuzzyReflection fuzzy) {
		List<Method> candidates = fuzzy.getMethodListByParameters(Void.TYPE,
				  					new Class<?>[] { int.class, Object.class});

		// Load the get-method
		try {
			GET_KEY_VALUE_METHOD = fuzzy.getMethodByParameters(
					"getWatchableObject", MinecraftReflection.getWatchableObjectClass(), new Class[] { int.class });
			GET_KEY_VALUE_METHOD.setAccessible(true);

		} catch (IllegalArgumentException e) {
			// Use the fallback method
		}

		for (Method method : candidates) {
			if (!method.getName().startsWith("watch")) {
				CREATE_KEY_VALUE_METHOD = method;
			} else {
				UPDATE_KEY_VALUE_METHOD = method;
			}
		}

		// Did we succeed?
		if (UPDATE_KEY_VALUE_METHOD == null || CREATE_KEY_VALUE_METHOD == null) {
			// Go by index instead
			if (candidates.size() > 1) {
				CREATE_KEY_VALUE_METHOD = candidates.get(0);
				UPDATE_KEY_VALUE_METHOD = candidates.get(1);
			} else {
				throw new IllegalStateException("Unable to find create and update watchable object. Update ProtocolLib.");
			}

			// Be a little scientist - see if this in fact IS the right way around
			try {
				WrappedDataWatcher watcher = new WrappedDataWatcher();
				watcher.setObject(0, 0);
				watcher.setObject(0, 1);

				if (watcher.getInteger(0) != 1) {
					throw new IllegalStateException("This cannot be!");
				}
			} catch (Exception e) {
				// Nope
				UPDATE_KEY_VALUE_METHOD = candidates.get(0);
				CREATE_KEY_VALUE_METHOD = candidates.get(1);
			}
		}
	}

	@Override
	public Iterator<WrappedWatchableObject> iterator() {
		// We'll wrap the iterator instead of creating a new list every time
		return Iterators.transform(getWatchableObjectMap().values().iterator(),
				new Function<Object, WrappedWatchableObject>() {

			@Override
			public WrappedWatchableObject apply(@Nullable Object item) {
				if (item != null)
					return new WrappedWatchableObject(item);
				else
					return null;
			}
		});
	}

	/**
	 * Retrieve a view of this DataWatcher as a map.
	 * <p>
	 * Any changes to the map will be reflected in this DataWatcher, and vice versa.
	 * @return A view of the data watcher as a map.
	 */
	public Map<Integer, WrappedWatchableObject> asMap() {
		// Construct corresponding map
		if (mapView == null) {
			mapView = new ConvertedMap<Integer, Object, WrappedWatchableObject>(getWatchableObjectMap()) {
				@Override
				protected Object toInner(WrappedWatchableObject outer) {
					if (outer == null)
						return null;
					return outer.getHandle();
				}

				@Override
				protected WrappedWatchableObject toOuter(Object inner) {
					if (inner == null)
						return null;
					return new WrappedWatchableObject(inner);
				}
			};
		}
		return mapView;
	}

	@Override
	public String toString() {
		return asMap().toString();
	}

	/**
	 * Retrieve the entity associated with this data watcher.
	 * <p>
	 * <b>Warning:</b> This is only supported on 1.7.2 and above.
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
}