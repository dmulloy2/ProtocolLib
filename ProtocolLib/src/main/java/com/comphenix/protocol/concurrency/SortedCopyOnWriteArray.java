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

package com.comphenix.protocol.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

/**
 * An implicitly sorted array list that preserves insertion order and maintains duplicates.
 * @param <T> - type of the elements in the list.
 */
public class SortedCopyOnWriteArray<T extends Comparable<T>> implements Collection<T> {
	// Prevent reordering
	private volatile List<T> list;
	
	/**
	 * Construct an empty sorted array.
	 */
	public SortedCopyOnWriteArray() {
		list = new ArrayList<T>();
	}
	
	/**
	 * Create a sorted array from the given list. The elements will be automatically sorted.
	 * @param wrapped - the collection whose elements are to be placed into the list.
	 */
	public SortedCopyOnWriteArray(Collection<T> wrapped) {
		this.list = new ArrayList<T>(wrapped);
	}
	
	/**
	 * Create a sorted array from the given list. 
	 * @param wrapped - the collection whose elements are to be placed into the list.
	 * @param sort - TRUE to automatically sort the collection, FALSE if it is already sorted. 
	 */
	public SortedCopyOnWriteArray(Collection<T> wrapped, boolean sort) {
		this.list = new ArrayList<T>(wrapped);
		
		if (sort) {
			Collections.sort(list);
		}
	}
	
	/**
	 * Inserts the given element in the proper location.
	 * @param value - element to insert.
	 */
	@Override
	public synchronized boolean add(T value) {
    	// We use NULL as a special marker, so we don't allow it
    	if (value == null)
    		throw new IllegalArgumentException("value cannot be NULL");
    	
    	List<T> copy = new ArrayList<T>();

        for (T element : list) {
        	// If the value is now greater than the current element, it should be placed right before it
        	if (value != null && value.compareTo(element) < 0) {
        		copy.add(value);
        		value = null;
        	}
        	copy.add(element);
        }
        
        // Don't forget to add it
        if (value != null)
        	copy.add(value);
        
        list = copy;
        return true;
    }
    
	@Override
    public synchronized boolean addAll(Collection<? extends T> values) {
		if (values == null)
			throw new IllegalArgumentException("values cannot be NULL");
		if (values.size() == 0)
			return false;
		
    	List<T> copy = new ArrayList<T>();
    	
    	// Insert the new content and sort it
    	copy.addAll(list);
    	copy.addAll(values);
    	Collections.sort(copy);
    	
    	list = copy;
    	return true;
    }
    
    /**
     * Removes from the list by making a new list with every element except the one given.
     * <p>
     * Objects will be compared using the given objects equals() method.
     * @param value - value to remove.
     */
	@Override
    public synchronized boolean remove(Object value) {
    	List<T> copy = new ArrayList<T>();
    	boolean result = false;
    	
    	// Note that there's not much to be gained from using BinarySearch, as we
    	// have to copy (and thus read) the entire list regardless.
    	
    	// Copy every element except the one given to us. 
    	for (T element : list) {
    		if (!Objects.equal(value, element)) {
    			copy.add(element);    			
    		} else {
    			result = true;
    		}
    	}

    	list = copy;
    	return result;
    }
    
	@Override
	public boolean removeAll(Collection<?> values) {
		// Special cases
		if (values == null)
			throw new IllegalArgumentException("values cannot be NULL");
		if (values.size() == 0)
			return false;
		
		List<T> copy = new ArrayList<T>();
		
		copy.addAll(list);
		copy.removeAll(values);
		
		list = copy;
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> values) {
		// Special cases
		if (values == null)
			throw new IllegalArgumentException("values cannot be NULL");
		if (values.size() == 0)
			return false;
		
		List<T> copy = new ArrayList<T>();
		
		copy.addAll(list);
		copy.removeAll(values);
		
		list = copy;
		return true;
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
	
	@Override
	public void clear() {
		list = new ArrayList<T>();
	}

	@Override
	public boolean contains(Object value) {
		return list.contains(value);
	}

	@Override
	public boolean containsAll(Collection<?> values) {
		return list.containsAll(values);
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public Object[] toArray() {
		return list.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}
	
	@Override
	public String toString() {
		return list.toString();
	}
}
