package com.comphenix.protocol.injector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * An implicitly sorted array list that preserves insertion order and maintains duplicates.
 * @param <T> - type of the elements in the list.
 */
class SortedCopyOnWriteArray<T> implements Iterable<T> {
	// Prevent reordering
	private volatile List<T> list;
	
	public SortedCopyOnWriteArray() {
		this(new ArrayList<T>());
	}
	
	public SortedCopyOnWriteArray(List<T> wrapped) {
		this.list = wrapped;
	}
	
	/**
	 * Inserts the given element in the proper location.
	 * @param value - element to insert.
	 */
    @SuppressWarnings("unchecked")
	public synchronized void add(T value) {
    	
    	// We use NULL as a special marker, so we don't allow it
    	if (value == null)
    		throw new IllegalArgumentException("value cannot be NULL");
    	
    	List<T> copy = new ArrayList<T>();
        Comparable<T> compare = (Comparable<T>) value;
        
        for (T element : list) {
        	// If the value is now greater than the current element, it should be placed right before it
        	if (value != null && compare.compareTo(element) < 0) {
        		copy.add(value);
        		value = null;
        	}
        	copy.add(element);
        }
        
        // Don't forget to add it
        if (value != null)
        	copy.add(value);
        
        list = copy;
    }
    
    /**
     * Removes from the list by making a new list with every element except the one given.
     * <p>
     * Objects will be compared using the given objects equals() method.
     * @param value - value to remove.
     */
    public synchronized void remove(T value) {
    	List<T> copy = new ArrayList<T>();
    	
    	// Copy every element except the one given to us
    	for (T element : list) {
    		if (value != null && !Objects.equal(value, element)) {
    			copy.add(element);
    			value = null;
    		}
    	}

    	list = copy;
    }
    
    /**
     * Removes from the list by making a copy of every element except the one with the given index.
     * @param index - index of the element to remove.
     */
    public synchronized void remove(int index) {
    	
    	List<T> copy = new ArrayList<T>(list);
    	
    	copy.remove(index);
    	list = copy;
    }
    
    /**
     * Retrieves an element by index. 
     * @param index - index of element to retrieve.
     * @return The element at the given location.
     */
    public T get(int index) {
    	return list.get(index);
    }
    
    /**
     * Retrieve the size of the list.
     * @return Size of the list.
     */
    public int size() {
    	return list.size();
    }

    /**
     * Retrieves an iterator over the elements in the given list. 
     * Warning: No not attempt to remove elements using the iterator.
     */
	public Iterator<T> iterator() {
		return Iterables.unmodifiableIterable(list).iterator();
	}
}
