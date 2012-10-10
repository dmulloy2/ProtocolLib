package com.comphenix.protocol.reflect.instances;

import javax.annotation.Nullable;

/**
 * Represents a type generator for specific types.
 * 
 * @author Kristian
 */
public interface InstanceProvider {

	/**
	 * Create an instance given a type, if possible.
	 * @param type - type to create.
	 * @return The instance, or NULL if the type cannot be created.
	 */
	public abstract Object create(@Nullable Class<?> type);
}