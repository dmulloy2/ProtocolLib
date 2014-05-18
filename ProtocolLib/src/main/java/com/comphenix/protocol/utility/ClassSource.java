package com.comphenix.protocol.utility;

import java.util.Collections;
import java.util.Map;

/**
 * Represents an abstract class loader that can only retrieve classes by their canonical name.
 * @author Kristian
 */
public abstract class ClassSource {
	/**
	 * Construct a class source from the default class loader.
	 * @return A class source.
	 */
	public static ClassSource fromClassLoader() {
		return fromClassLoader(ClassSource.class.getClassLoader());
	}

	/**
	 * Construct a class source from the default class loader and package.
	 * @param packageName - the package that is prepended to every lookup.
	 * @return A package source.
	 */
	public static ClassSource fromPackage(String packageName) {
		return fromClassLoader().usingPackage(packageName);
	}

	/**
	 * Construct a class source from the given class loader.
	 * @param loader - the class loader.
	 * @return The corresponding class source.
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
	 * If the map is null, it will be interpreted as an empty map. If the map does not contain a Class with the specified name, or that string maps to NULL explicitly, a {@link ClassNotFoundException} will be thrown.
	 * @param map - map of class names and classes.
	 * @return The class source.
	 */
	public static ClassSource fromMap(final Map<String, Class<?>> map) {
		return new ClassSource() {
			@Override
			public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
				Class<?> loaded = map == null ? null : map.get(canonicalName);
				if(loaded == null){
					// Throw the appropriate exception if we can't load the class
					throw new ClassNotFoundException("The specified class could not be found by this ClassLoader.");
				}
				return loaded;
			}
		};
	}
	
	/**
	 * @return A ClassLoader which will never successfully load a class.
	 */
	public static ClassSource empty(){
		return fromMap(Collections.<String, Class<?>>emptyMap());
	}

	/**
	 * Retrieve a class source that will attempt lookups in each of the given sources in the order they are in the array, and return the first value that is found.
	 * If the sources array is null or composed of any null elements, an exception will be thrown.
	 * @param sources - the class sources.
	 * @return A new class source.
	 */
	public static ClassSource attemptLoadFrom(final ClassSource... sources) {
		if(sources.length == 0){ // Throws NPE if sources is null, which is what we want
			return ClassSource.empty();
		}
		
		ClassSource source = null;
		for(int i = 0; i < sources.length; i++){
			if(sources[i] == null){
				throw new IllegalArgumentException("Null values are not permitted as ClassSources.");
			}
			
			source = source == null ? sources[i] : source.retry(sources[i]);
		}
		return source;
	}

	/**
	 * Retrieve a class source that will retry failed lookups in the given source.
	 * @param other - the other class source.
	 * @return A new class source.
	 */
	public ClassSource retry(final ClassSource other) {
		return new ClassSource() {
			@Override
			public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
				try {
					return ClassSource.this.loadClass(canonicalName);
				} catch (ClassNotFoundException e) {
					return other.loadClass(canonicalName);
				}
			}
		};
	}

	/**
	 * Retrieve a class source that prepends a specific package name to every lookup.
	 * @param packageName - the package name to prepend.
	 * @return The class source.
	 */
	public ClassSource usingPackage(final String packageName) {
		return new ClassSource() {
			@Override
			public Class<?> loadClass(String canonicalName) throws ClassNotFoundException {
				return ClassSource.this.loadClass(append(packageName, canonicalName));
			}
		};
	}

	/**
	 * Append to canonical names together.
	 * @param a - the name to the left.
	 * @param b - the name to the right.
	 * @return The full canonical name, with a dot seperator.
	 */ 
	protected static String append(String a, String b) {
		boolean left = a.endsWith(".");
		boolean right = b.endsWith(".");

		// Only add a dot if necessary
		if (left && right)
			return a.substring(0, a.length() - 1) + b;
		else if (left != right)
			return a + b;
		else
			return a + "." + b;
	}

	/**
	 * Retrieve a class by name.
	 * @param canonicalName - the full canonical name of the class.
	 * @return The corresponding class. If the class is not found, NULL should <b>not</b> be returned, instead a {@code ClassNotFoundException} exception should be thrown.
	 * @throws ClassNotFoundException If the class could not be found.
	 */
	public abstract Class<?> loadClass(String canonicalName) throws ClassNotFoundException;
}
