package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;

/**
 * Represents an interface for accessing a field.
 * @author Kristian
 */
public interface FieldAccessor {
	/**
	 * Retrieve the value of a field for a particular instance.
	 * @param instance - the instance, or NULL for a static field.
	 * @return The value of the field.
	 * @throws IllegalStateException If the current security context prohibits reflection.
	 */
	public Object get(Object instance);
	
	/**
	 * Set the value of a field for a particular instance.
	 * @param instance - the instance, or NULL for a static field.
	 * @param value - the new value of the field.
	 */
	public void set(Object instance, Object value);
	
	/**
	 * Retrieve the underlying field.
	 * @return The field.
	 */
	public Field getField();
}