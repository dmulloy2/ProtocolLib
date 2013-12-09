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
public class WrappedIntHashMap extends AbstractWrapper {
	private static Method PUT_METHOD;
	private static Method GET_METHOD;
	private static Method REMOVE_METHOD;
	
	/**
	 * Construct an IntHashMap wrapper around an instance.
	 * @param handle - the NMS instance.
	 */
	private WrappedIntHashMap(Object handle) {		
		super(MinecraftReflection.getIntHashMapClass());
		setHandle(handle);
	}
	
	/**
	 * Construct a new IntHashMap.
	 * @return A new IntHashMap.
	 */
	public static WrappedIntHashMap newMap() {
		try {
			return new WrappedIntHashMap(MinecraftReflection.getIntHashMapClass().newInstance());
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
		invokeMethod(PUT_METHOD, key, value);
	}
	
	/**
	 * Retrieve the value associated with a specific key, or NULL if not found.
	 * @param key - the integer key.
	 * @return The associated value, or NULL.
	 */
	public Object get(int key) {
		initializeGetMethod();
		return invokeMethod(GET_METHOD, key);
	}
		
	/**
	 * Remove a mapping of a key to a value if it is present.
	 * @param key - the key of the mapping to remove.
	 * @return The object that was removed, or NULL if the key is not present.
	 */
	public Object remove(int key) {
		initializeGetMethod();
		
		if (REMOVE_METHOD == null)
			return removeFallback(key);
		return invokeMethod(REMOVE_METHOD, key);
	}
	
	/**
	 * Remove a entry in the IntHashMap using a fallback method.
	 * @param key - the key of the mapping to remove.
	 * @return The removed element.
	 */
	private Object removeFallback(int key) {
		Object old = get(key);
		
		invokeMethod(PUT_METHOD, key, null);
		return old;
	}
	
	/**
	 * Invoke a particular method on the current handle
	 * @param method - the current method.
	 * @param params - parameters.
	 * @return The return value, if any.
	 */
	private Object invokeMethod(Method method, Object... params) {
		try {
			return method.invoke(handle, params);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Illegal argument.", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access method.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Unable to invoke " + method + " on " + handle, e);
		}
	}
	
	private void initializePutMethod() {
		if (PUT_METHOD == null) {
			// Fairly straight forward
			PUT_METHOD = FuzzyReflection.fromClass(MinecraftReflection.getIntHashMapClass()).getMethod(
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
					
			// Determine which method to trust
			for (Method method : FuzzyReflection.fromClass(MinecraftReflection.getIntHashMapClass()).
					getMethodListByParameters(Object.class, new Class<?>[] { int.class })) {
				
				// Initialize a value
				temp.put(1, expected);
				
				// Skip static methods
				if (Modifier.isStatic(method.getModifiers()))
					continue;
				
				try {
					boolean first = expected.equals(method.invoke(temp.getHandle(), 1));
					boolean second = expected.equals(method.invoke(temp.getHandle(), 1));
					
					// See if we found the method we are looking for
					if (first && !second) {
						REMOVE_METHOD = method;
					} else if (first && second) {
						GET_METHOD = method;
					}
				} catch (Exception e) {
					// Suppress
				}
			}
			
			if (GET_METHOD == null)
				throw new IllegalStateException("Unable to find appropriate GET_METHOD for IntHashMap.");
		}
	}
}
