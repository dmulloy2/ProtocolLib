package com.comphenix.protocol.reflect.definer;

/**
 * Utility class to define dynamically generated classes.
 */
public interface ClassDefiner {

	/**
	 * Get if this class definer can be used in the current jvm.
	 *
	 * @return true if this class definer can be used in the current jvm, false otherwise.
	 */
	boolean isAvailable();

	/**
	 * Defines the given bytecode as a nest member of the given host class, if possible.
	 *
	 * @param hostClass the parent class of the given class represented as bytecode.
	 * @param byteCode  the bytecode of the class to define.
	 * @return the defined class instance, null if defining was not possible.
	 */
	Class<?> define(Class<?> hostClass, byte[] byteCode);
}
