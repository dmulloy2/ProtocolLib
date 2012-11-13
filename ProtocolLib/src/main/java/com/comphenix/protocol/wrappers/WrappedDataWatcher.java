package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.base.Objects;

import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.DataWatcher;
import net.minecraft.server.WatchableObject;

/**
 * Wraps a DataWatcher that is used to transmit arbitrary key-value pairs with a given entity.
 * 
 * @author Kristian
 */
public class WrappedDataWatcher {

	/**
	 * Used to assign integer IDs to given types.
	 */
	private static Map<Class<?>, Integer> typeMap;

	// Fields
	private static Field valueMapField;
	private static Field readWriteLockField;
	
	// Methods
	private static Method createKeyValueMethod;
	private static Method updateKeyValueMethod;
	private static Method getKeyValueMethod;
	
	// Entity methods
	private static Field entityDataField;
	
	/**
	 * Whether or not this class has already been initialized.
	 */
	private static boolean hasInitialized;
	
	// The underlying DataWatcher we're modifying
	protected DataWatcher handle;
	
	// Lock
	private ReadWriteLock readWriteLock;
	
	// Map of watchable objects
	private Map<Integer, Object> watchableObjects;
	
	/**
	 * Initialize a new data watcher.
	 * @throws FieldAccessException If we're unable to wrap a DataWatcher.
	 */
	public WrappedDataWatcher() {
		// Just create a new watcher
		this(new DataWatcher());
	}
	
	/**
	 * Create a wrapper for a given data watcher.
	 * @param dataWatcher - the data watcher to wrap.
	 * @throws FieldAccessException If we're unable to wrap a DataWatcher.
	 */
	public WrappedDataWatcher(DataWatcher handle) {
		this.handle = handle;
		
		try {
			initialize();
		} catch (FieldAccessException e) {
			throw new RuntimeException("Cannot initialize wrapper.", e);
		}
	}
	
	/**
	 * Create a new data watcher from a list of watchable objects.
	 * @param watchableObjects - list of watchable objects that will be copied.
	 * @throws FieldAccessException Unable to read watchable objects.
	 */
	public WrappedDataWatcher(List<WrappedWatchableObject> watchableObjects) throws FieldAccessException {
		this();
		
		// Fill the underlying map
		for (WrappedWatchableObject watched : watchableObjects) {
			setObject(watched.getIndex(), watched.getValue());
		}
	}
	
	/**
	 * Retrieves the underlying data watcher.
	 * @return The underlying data watcher.
	 */
	public DataWatcher getHandle() {
		return handle;
	}
	
	/**
	 * Retrieve the ID of a given type, if it's allowed to be watched.
	 * @return The ID, or NULL if it cannot be watched.
	 * @throws FieldAccessException If we cannot initialize the reflection machinery.
	 */
	public static Integer getTypeID(Class<?> clazz) throws FieldAccessException {
		initialize();
		
		return typeMap.get(clazz);
	}
	
	/**
	 * Retrieve the type of a given ID, if it's allowed to be watched.
	 * @return The type using a given ID, or NULL if it cannot be watched.
	 * @throws FieldAccessException If we cannot initialize the reflection machinery.
	 */
	public static Class<?> getTypeClass(int id) throws FieldAccessException {
		initialize();
		
		for (Map.Entry<Class<?>, Integer> entry : typeMap.entrySet()) {
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
    	return (Byte) getObjectRaw(index);
    }
    
    /**
     * Get a watched short.
     * @param index - index of the watched short.
     * @return The watched short, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Short getShort(int index) throws FieldAccessException {
    	return (Short) getObjectRaw(index);
    }
    
    /**
     * Get a watched integer.
     * @param index - index of the watched integer.
     * @return The watched integer, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Integer getInteger(int index) throws FieldAccessException {
    	return (Integer) getObjectRaw(index);
    }
    
    /**
     * Get a watched float.
     * @param index - index of the watched float.
     * @return The watched float, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public Float getFloat(int index) throws FieldAccessException {
    	return (Float) getObjectRaw(index);
    }
    
    /**
     * Get a watched string.
     * @param index - index of the watched string.
     * @return The watched string, or NULL if this value doesn't exist.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public String getString(int index) throws FieldAccessException {
    	return (String) getObjectRaw(index);
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
    	Object result = getObjectRaw(index);
    	
    	// Handle the special cases too
    	if (result instanceof net.minecraft.server.ItemStack) {
    		return BukkitConverters.getItemStackConverter().getSpecific(result);
    	} else if (result instanceof ChunkCoordinates) {
    		return new WrappedChunkCoordinate((ChunkCoordinates) result);
    	} else {
    		return result;
    	}
    }
    
    /**
     * Retrieve a watchable object by index.
     * @param index - index of the object to retrieve.
     * @return The watched object.
     * @throws FieldAccessException Cannot read underlying field.
     */
    private Object getObjectRaw(int index) throws FieldAccessException {
    	// The get method will take care of concurrency
    	WatchableObject watchable = getWatchedObject(index);

    	if (watchable != null) {
    		return new WrappedWatchableObject(watchable).getValue();
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
    	try {
    		getReadWriteLock().readLock().lock();
    		
    		List<WrappedWatchableObject> result = new ArrayList<WrappedWatchableObject>();
    		
    		// Add each watchable object to the list
    		for (Object watchable : getWatchableObjectMap().values()) {
    			if (watchable != null) {
    				result.add(new WrappedWatchableObject((WatchableObject) watchable));
    			} else {
    				result.add(null);
    			}
    		}
    		return result;
    		
    	} finally {
    		getReadWriteLock().readLock().unlock();
    	}
    }

    /**
     * Retrieve a copy of every index associated with a watched object.
     * @return Every watched object index.
     * @throws FieldAccessException If we're unable to read the underlying object.
     */
    public Set<Integer> indexSet() throws FieldAccessException {
    	try {
    		getReadWriteLock().readLock().lock();
    		return new HashSet<Integer>(getWatchableObjectMap().keySet());
    		
    	} finally {
    		getReadWriteLock().readLock().unlock();
    	}
    }
    
    /**
     * Retrieve the number of watched objects.
     * @return Watched object count.
     * @throws FieldAccessException If we're unable to read the underlying object.
     */
    public int size() throws FieldAccessException {
    	try {
    		getReadWriteLock().readLock().lock();
    		return getWatchableObjectMap().size();
    		
    	} finally {
    		getReadWriteLock().readLock().unlock();
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
     * @param update - whether or not to refresh every listening clients.
     * @throws FieldAccessException Cannot read underlying field.
     */
    public void setObject(int index, Object newValue, boolean update) throws FieldAccessException {
    	// Convert special cases
    	if (newValue instanceof WrappedChunkCoordinate)
    		newValue = ((WrappedChunkCoordinate) newValue).getHandle();
    	if (newValue instanceof ItemStack)
    		newValue = BukkitConverters.getItemStackConverter().getGeneric(
    				net.minecraft.server.ItemStack.class, (ItemStack) newValue);
    	
    	// Next, set the object
    	setObjectRaw(index, newValue, update);
    }
    
    /**
     * Set a watchable object by index.
     * @param index - index of the object to retrieve.
     * @param newValue - the new watched value.
     * @param update - whether or not to refresh every listening clients.
     * @return The watched object.
     * @throws FieldAccessException Cannot read underlying field.
     */
    private void setObjectRaw(int index, Object newValue, boolean update) throws FieldAccessException {
    	WatchableObject watchable;
    	
    	try {
    		// Aquire write lock
    		getReadWriteLock().writeLock().lock();
    		watchable = getWatchedObject(index);
	    	
	    	if (watchable != null) {
	    		new WrappedWatchableObject(watchable).setValue(newValue, update);
	    	}
    	} finally {
    		getReadWriteLock().writeLock().unlock();
    	}
    }
        
    private WatchableObject getWatchedObject(int index) throws FieldAccessException {
    	// We use the get-method first and foremost
    	if (getKeyValueMethod != null) {
			try {
				return (WatchableObject) getKeyValueMethod.invoke(handle, index);
			} catch (Exception e) {
				throw new FieldAccessException("Cannot invoke get key method for index " + index, e);
			}
    	} else {
    		try {
    			getReadWriteLock().readLock().lock();
    			return (WatchableObject) getWatchableObjectMap().get(index);
    			
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
	    	else if (readWriteLockField != null)
	    		return readWriteLock = (ReadWriteLock) FieldUtils.readField(readWriteLockField, handle, true);
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
		if (watchableObjects == null) {
			try {
				watchableObjects = (Map<Integer, Object>) FieldUtils.readField(valueMapField, handle, true);
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot read watchable object field.", e);
			}
		}
		return watchableObjects;
	}
	
	/**
	 * Retrieve the data watcher associated with an entity.
	 * @param entity - the entity to read from.
	 * @return Associated data watcher.
	 * @throws FieldAccessException Reflection failed.
	 */
	public static WrappedDataWatcher getEntityWatcher(Entity entity) throws FieldAccessException {
		if (entityDataField == null)
			entityDataField = FuzzyReflection.fromClass(net.minecraft.server.Entity.class, true).
				getFieldByType("datawatcher", DataWatcher.class);

		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		
		try {
			Object nsmWatcher = FieldUtils.readField(entityDataField, unwrapper.unwrapItem(entity), true);
			
			if (nsmWatcher != null) 
				return new WrappedDataWatcher((DataWatcher) nsmWatcher);
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
		if (!hasInitialized)
			hasInitialized = true;
		else
			return;
		
		FuzzyReflection fuzzy = FuzzyReflection.fromClass(DataWatcher.class, true);
		
		for (Field lookup : fuzzy.getFieldListByType(Map.class)) {
			if (Modifier.isStatic(lookup.getModifiers())) {
				// This must be the type map
				try {
					typeMap = (Map<Class<?>, Integer>) FieldUtils.readStaticField(lookup, true);
				} catch (IllegalAccessException e) {
					throw new FieldAccessException("Cannot access type map field.", e);
				}
				
			} else {
				// If not, then we're probably dealing with the value map
				valueMapField = lookup;
			}
		}
		
		try {
			readWriteLockField = fuzzy.getFieldByType("readWriteLock", ReadWriteLock.class);
		} catch (IllegalArgumentException e) {
			// It's not a big deal
		}
		
		initializeMethods(fuzzy);
	}
	
	private static void initializeMethods(FuzzyReflection fuzzy) {
		List<Method> candidates = fuzzy.getMethodListByParameters(Void.TYPE, 
				  					new Class<?>[] { int.class, Object.class});
		
		for (Method method : candidates) {
			
			if (!method.getName().startsWith("watch")) {
				createKeyValueMethod = method;
			} else {
				updateKeyValueMethod = method;
			}
		}
		
		// Did we succeed?
		if (updateKeyValueMethod == null || createKeyValueMethod == null) {
			// Go by index instead
			if (candidates.size() > 1) {
				createKeyValueMethod = candidates.get(0);
				updateKeyValueMethod = candidates.get(1);
			} else {
				throw new IllegalStateException("Unable to find create and update watchable object. Update ProtocolLib.");
			}
		}
		
		// Load the get-method
		try {
			getKeyValueMethod = fuzzy.getMethodByParameters(
					"getWatchableObject", ".*WatchableObject", new String[] { int.class.getName() });
			getKeyValueMethod.setAccessible(true);
		} catch (IllegalArgumentException e) {
			// Use fallback method
		}
	}
}
