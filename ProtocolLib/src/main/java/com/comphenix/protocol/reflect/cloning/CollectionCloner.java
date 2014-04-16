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

package com.comphenix.protocol.reflect.cloning;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Attempts to clone collection and array classes.
 * 
 * @author Kristian
 */
public class CollectionCloner implements Cloner {
	private final Cloner defaultCloner;

	/**
	 * Constructs a new collection and array cloner with the given inner element cloner.
	 * @param defaultCloner - default inner element cloner.
	 */
	public CollectionCloner(Cloner defaultCloner) {
		this.defaultCloner = defaultCloner;
	}

	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;
		
		Class<?> clazz = source.getClass();
		return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz) || clazz.isArray();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");
		
		Class<?> clazz = source.getClass();

		if (source instanceof Collection) {
			Collection<Object> copy = cloneConstructor(Collection.class, clazz, source);

			// Next, clone each element in the collection
			try {
				copy.clear();
				
				for (Object element : (Collection<Object>) source) {
					copy.add(getClone(element, source));
				}
			} catch (UnsupportedOperationException e) {
				// Immutable - we can't do much about that
			}
			return copy;
			
		} else if (source instanceof Map) {
			Map<Object, Object> copy = cloneConstructor(Map.class, clazz, source);
			
			// Next, clone each element in the collection
			try {
				copy.clear();
				
				for (Entry<Object, Object> element : ((Map<Object, Object>) source).entrySet()) {
					Object key = getClone(element.getKey(), source);
					Object value = getClone(element.getValue(), source);
					copy.put(key, value);
				}
			} catch (UnsupportedOperationException e) {
				// Immutable - we can't do much about that
			}
			return copy;
			
		} else if (clazz.isArray()) {
			// Get the length
			int lenght = Array.getLength(source);
			Class<?> component = clazz.getComponentType();
			
			// Can we speed things up by making a shallow copy instead?
			if (ImmutableDetector.isImmutable(component)) {
				return clonePrimitive(component, source);
			}
			
			// Create a new copy
			Object copy = Array.newInstance(clazz.getComponentType(), lenght);
			
			// Set each element
			for (int i = 0; i < lenght; i++) {
				Object element = Array.get(source, i);
				
				if (defaultCloner.canClone(element))
					Array.set(copy, i, defaultCloner.clone(element));
				else
					throw new IllegalArgumentException("Cannot clone " + element + " in array " + source);
			}
			
			// And we're done
			return copy;
		}
		
		throw new IllegalArgumentException(source + " is not an array nor a Collection.");
	}
	
	/**
	 * Clone an element using the default cloner.
	 * @param element - the element to clone.
	 * @param container - where the element is stored.
	 * @return The cloned element.
	 */
	private Object getClone(Object element, Object container) {
		if (defaultCloner.canClone(element))
			return defaultCloner.clone(element);
		else
			throw new IllegalArgumentException("Cannot clone " + element + " in container " + container);
	}
	
	/**
	 * Clone a primitive or immutable array by calling its clone method.
	 * @param component - the component type of the array.
	 * @param source - the array itself.
	 * @return The cloned array.
	 */
	private Object clonePrimitive(Class<?> component, Object source) {
		// Cast and call the correct version
		if (byte.class.equals(component))
			return ((byte[]) source).clone();
		else if (short.class.equals(component))
			return ((short[]) source).clone();
		else if (int.class.equals(component))
			return ((int[]) source).clone();
		else if (long.class.equals(component))
			return ((long[]) source).clone();
		else if (float.class.equals(component))
			return ((float[]) source).clone();
		else if (double.class.equals(component))
			return ((double[]) source).clone();
		else if (char.class.equals(component))
			return ((char[]) source).clone();
		else if (boolean.class.equals(component))
			return ((boolean[]) source).clone();
		else
			return ((Object[]) source).clone();
	}
	
	/**
	 * Clone an object by calling its clone constructor, or alternatively, a "clone" method.
	 * @param superclass - the superclass we expect in the clone constructor.
	 * @param clazz - the class of the object.
	 * @param source - the object itself.
	 * @return A cloned object.
	 */
	@SuppressWarnings("unchecked")
	private <T> T cloneConstructor(Class<?> superclass, Class<?> clazz, Object source) {
		// Not all collections or maps implement "clone", but most *do* implement the "copy constructor" pattern
		try {
			Constructor<?> constructCopy = clazz.getConstructor(Collection.class);
			return (T) constructCopy.newInstance(source);
		} catch (NoSuchMethodException e) {
			if (source instanceof Serializable)
				return (T) new SerializableCloner().clone(source);
			// Delegate to serializable if possible
			return (T) cloneObject(clazz, source);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct collection.", e);
		}
	}
	
	/**
	 * Clone an object by calling "clone" using reflection.
	 * @param clazz - the class type.
	 * @param obj - the object to clone.
	 * @return The cloned object.
	 */
	private Object cloneObject(Class<?> clazz, Object source) {
		// Try to clone it instead
		try {
			return clazz.getMethod("clone").invoke(source);
		} catch (Exception e1) {
			throw new RuntimeException("Cannot copy " + source + " (" + clazz + ")", e1);
		}
	}
	
	/**
	 * Retrieve the default cloner used to clone the content of each element in the collection.
	 * @return Cloner used to clone elements.
	 */
	public Cloner getDefaultCloner() {
		return defaultCloner;
	}
}
