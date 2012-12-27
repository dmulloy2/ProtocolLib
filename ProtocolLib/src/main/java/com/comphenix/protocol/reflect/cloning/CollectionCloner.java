package com.comphenix.protocol.reflect.cloning;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.Collection;

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
		return Collection.class.isAssignableFrom(clazz) || clazz.isArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");
		
		Class<?> clazz = source.getClass();
		
		if (source instanceof Collection) {
			Collection<Object> copy = null; 
			
			// Not all collections implement "clone", but most *do* implement the "copy constructor" pattern
			try {
				Constructor<?> constructCopy = clazz.getConstructor(Collection.class);
				copy = (Collection<Object>) constructCopy.newInstance(source);
			} catch (NoSuchMethodException e) {
				copy = (Collection<Object>) cloneObject(clazz, source);
			} catch (Exception e) {
				throw new RuntimeException("Cannot construct collection.", e);
			}
			
			// Next, clone each element in the collection
			copy.clear();
			
			for (Object element : (Collection<Object>) source) {
				if (defaultCloner.canClone(element))
					copy.add(defaultCloner.clone(element));
				else
					throw new IllegalArgumentException("Cannot clone " + element + " in collection " + source);
			}
			
			return copy;
			
			// Second possibility
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
