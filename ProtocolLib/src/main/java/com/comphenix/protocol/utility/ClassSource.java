package com.comphenix.protocol.utility;

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
	 * Retrieve a class by name.
	 * @param canonicalName - the full canonical name of the class.
	 * @return The corresponding class 
	 * @throws ClassNotFoundException If the class could not be found.
	 */
	public abstract Class<?> loadClass(String canonicalName) throws ClassNotFoundException;
}
