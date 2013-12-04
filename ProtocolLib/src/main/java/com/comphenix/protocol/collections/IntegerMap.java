package com.comphenix.protocol.collections;

import java.util.Arrays;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Represents a very quick integer-based lookup map, with a fixed key space size.
 * <p>
 * Integers must be non-negative.
 * @author Kristian
 */
public class IntegerMap<T> {
	private T[] array;
	private int size;
	
	/**
	 * Construct a new integer map.
	 * @return A new integer map.
	 */
	public static <T> IntegerMap<T> newMap() {
		return new IntegerMap<T>();
	}
	
	/**
	 * Construct a new integer map with a default capacity.
	 */
	public IntegerMap() {
		this(8);
	}
	
	/**
	 * Construct a new integer map with a given capacity.
	 * @param initialCapacity - the capacity.
	 */
	public IntegerMap(int initialCapacity) {
		@SuppressWarnings("unchecked")
		T[] backingArray = (T[]) new Object[initialCapacity];
		this.array = backingArray;
		this.size = 0;
	}

	/**
	 * Associate an integer key with the given value.
	 * @param key - the integer key. Cannot be negative.
	 * @param value - the value. Cannot be NULL.
	 * @return The previous association, or NULL if not found.
	 */
	public T put(int key, T value) {
		ensureCapacity(key);
		
		T old = array[key];	
		array[key] = Preconditions.checkNotNull(value, "value cannot be NULL");
		
		if (old == null)
			size++;
		return old;
	}
	
	/**
	 * Remove an association from the map.
	 * @param key - the key of the association to remove.
	 * @return The old associated value, or NULL.
	 */
	public T remove(int key) {
		T old = array[key];
		array[key] = null;
		
		if (old != null)
			size--;
		return old;
	}
	
	/**
	 * Resize the backing array to fit the given key.
	 * @param key - the key.
	 */
	protected void ensureCapacity(int key) {
		int newLength = array.length;

		// Don't resize if the key fits
		if (key < 0)
			throw new IllegalArgumentException("Negative key values are not permitted.");
		if (key < newLength)
			return;
		
		while (newLength <= key) {
			int next = newLength * 2;
			// Handle overflow
			newLength = next > newLength ? next : Integer.MAX_VALUE;
		}
		this.array = Arrays.copyOf(array, newLength);
	}
	
	/**
	 * Retrieve the number of mappings in this map.
	 * @return The number of mapping.
	 */
	public int size() {
		return size;
	}

	/**
	 * Retrieve the value associated with a given key.
	 * @param key - the key.
	 * @return The value, or NULL if not found.
	 */
	public T get(int key) {
		if (key >= 0 && key < array.length)
			return array[key];
		return null;
	}
	
	/**
	 * Determine if the given key exists in the map.
	 * @param key - the key to check.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean containsKey(int key) {
		return get(key) != null;
	}
	
	/**
	 * Convert the current map to an Integer map.
	 * @return The Integer map.
	 */
	public Map<Integer, Object> toMap() {
		Map<Integer, Object> map = Maps.newHashMap();
		
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				map.put(i, array[i]);
			}
		}
		return map;
	}
}
