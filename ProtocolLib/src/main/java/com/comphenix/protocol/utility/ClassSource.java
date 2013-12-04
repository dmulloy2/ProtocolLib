package com.comphenix.protocol.utility;

import java.util.Map;

/**
 * Represents an abstract class loader that can only retrieve classes by their canonical name.
 * @author Kristian
 */
abstract class ClassSource {
	/**
	 * Construct a class source from the current class loader.
	 * @return A package source.
	 */
	public static ClassSource fromClassLoader() {
		return fromClassLoader(ClassSource.class.getClassLoader());
	}
	
	/**
	 * Construct a class source from the given class loader.
	 * @param loader - the class loader.
	 * @return The corresponding package source.
	 */
	public static ClassSource fromClassLoader(final ClassLoader loader) {
		return new ClassSource() {
			@Override
			public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
				return loader.loadClass(canonicalName);
			}
		};
	}

	/**
	 * Construct a class source from a mapping of canonical names and the corresponding classes.
	 * @param map - map of class names and classes.
	 * @return The class source.
	 */
	public static ClassSource fromMap(final Map<String, Class<?>> map) {
		return new ClassSource() {
			@Override
			public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
				return map.get(canonicalName);
			}
		};
	}
	
	/**
	 * Retrieve a class by name.
	 * @param canonicalName - the full canonical name of the class.
	 * @return The corresponding class 
	 * @throws ClassNotFoundException If the class could not be found.
	 */
	public abstract Class<?> loadClass(String canonicalName) throws ClassNotFoundException;
}
