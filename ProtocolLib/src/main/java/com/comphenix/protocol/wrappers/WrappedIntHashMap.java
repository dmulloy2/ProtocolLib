package com.comphenix.protocol.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Preconditions;

/**
 * Represents a wrapper for the internal IntHashMap in Minecraft.
 * @author Kristian
 */
public class WrappedIntHashMap {
	private static final Class<?> INT_HASH_MAP = MinecraftReflection.getIntHashMapClass();
	
	private static Method PUT_METHOD;
	private static Method GET_METHOD;
	
	private Object handle;
	
	/**
	 * Construct an IntHashMap wrapper around an instance.
	 * @param handle - the NMS instance.
	 */
	private WrappedIntHashMap(Object handle) {		
		this.handle = handle;
	}
	
	/**
	 * Construct a new IntHashMap.
	 * @return A new IntHashMap.
	 */
	public static WrappedIntHashMap newMap() {
		try {
			return new WrappedIntHashMap(INT_HASH_MAP.newInstance());
		} catch (Exception e) {
			throw new RuntimeException("Unable to construct IntHashMap.", e);
		}
	}
	
	/**
	 * Construct a wrapper around a given NMS IntHashMap.
	 * @param handle - the NMS IntHashMap.
	 * @return The created wrapped.
	 * @throws IllegalArgumentException If the handle is not an IntHasMap.
	 */
	public static WrappedIntHashMap fromHandle(@Nonnull Object handle) {
		Preconditions.checkNotNull(handle, "handle cannot be NULL");
		Preconditions.checkState(MinecraftReflection.isIntHashMap(handle), 
				"handle is a " + handle.getClass() + ", not an IntHashMap.");

		return new WrappedIntHashMap(handle);
	}
	
	/**
	 * Associates a specified key with the given value in the integer map. 
	 * <p>
	 * If the key has already been associated with a value, then it will be replaced by the new value.
	 * @param key - the key to insert.
	 * @param value - the value to insert. Cannot be NULL.
	 * @throws RuntimeException If the reflection machinery failed.
	 */
	public void put(int key, Object value) {
		Preconditions.checkNotNull(value, "value cannot be NULL.");
		
		initializePutMethod();
		putInternal(key, value);
	}
	
	/**
	 * Invoked when a value must be inserted into the underlying map, regardless of preconditions.
	 * @param key - the key.
	 * @param value - the value to insert.
	 */
	private void putInternal(int key, Object value) {
		try {
			PUT_METHOD.invoke(handle, key, value);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal argument.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access method.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception occured in " + PUT_METHOD + " for" + handle, e);
		}
	}
	
	/**
	 * Retrieve the value associated with a specific key, or NULL if not found.
	 * @param key - the integer key.
	 * @return The associated value, or NULL.
	 */
	public Object get(int key) {
		initializeGetMethod();
		
		try {
			return GET_METHOD.invoke(handle, key);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal argument.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access method.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to invoke " + GET_METHOD + " on " + handle, e);
		}
	}
	
	/**
	 * Remove a mapping of a key to a value if it is present.
	 * @param key - the key of the mapping to remove.
	 * @return TRUE if a key-value pair was removed, FALSE otherwise.
	 */
	public boolean remove(int key) {
		Object prev = get(key);
		
		if (prev != null) {
			putInternal(key, null);
			return true;
		}
		return false;
	}
	
	private void initializePutMethod() {
		if (PUT_METHOD == null) {
			// Fairly straight forward
			PUT_METHOD = FuzzyReflection.fromClass(INT_HASH_MAP).getMethod(
				FuzzyMethodContract.newBuilder().
					banModifier(Modifier.STATIC).
					parameterCount(2).
					parameterExactType(int.class).
					parameterExactType(Object.class).
					build());
		}
	}
	
	private void initializeGetMethod() {
		if (GET_METHOD == null) {
			WrappedIntHashMap temp = WrappedIntHashMap.newMap();
			String expected = "hello";
		
			// Initialize a value
			temp.put(1, expected);
			
			// Determine which method to trust
			for (Method method : FuzzyReflection.fromClass(INT_HASH_MAP).
					getMethodListByParameters(Object.class, new Class<?>[] { int.class })) {
				
				// Skip static methods
				if (Modifier.isStatic(method.getModifiers()))
					continue;
				
				try {
					// See if we found the method we are looking for
					if (expected.equals(method.invoke(temp.getHandle(), 1))) {
						GET_METHOD = method;
						return;
					}
				} catch (Exception e) {
					// Suppress
				}
			}
			throw new IllegalStateException("Unable to find appropriate GET_METHOD for IntHashMap.");
		}
	}
	
	/**
	 * Retrieve the underlying IntHashMap object.
	 * @return The underlying object.
	 */
	public Object getHandle() {
		return handle;
	}
}
