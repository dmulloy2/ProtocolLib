package com.comphenix.protocol.utility;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents an abstract class loader that can only retrieve classes by their canonical name.
 *
 * @author Kristian
 */
@FunctionalInterface
public interface ClassSource {

	/**
	 * Construct a class source from the default class loader.
	 *
	 * @return A class source.
	 */
	static ClassSource fromClassLoader() {
		return fromClassLoader(ClassSource.class.getClassLoader());
	}

	/**
	 * Construct a class source from the default class loader and package.
	 *
	 * @param packageName - the package that is prepended to every lookup.
	 * @return A package source.
	 */
	static ClassSource fromPackage(String packageName) {
		return fromClassLoader().usingPackage(packageName);
	}

	/**
	 * Construct a class source from the given class loader.
	 *
	 * @param loader - the class loader.
	 * @return The corresponding class source.
	 */
	static ClassSource fromClassLoader(final ClassLoader loader) {
		return canonicalName -> {
			try {
				return Optional.of(loader.loadClass(canonicalName));
			} catch (ClassNotFoundException ignored) {
				return Optional.empty();
			}
		};
	}

	/**
	 * Construct a class source from a mapping of canonical names and the corresponding classes. If the map is null, it
	 * will be interpreted as an empty map. If the map does not contain a Class with the specified name, or that string
	 * maps to NULL explicitly, an empty optional will be returned
	 *
	 * @param map - map of class names and classes.
	 * @return The class source.
	 */
	static ClassSource fromMap(final Map<String, Class<?>> map) {
		return canonicalName -> Optional.ofNullable(map.get(canonicalName));
	}

	/**
	 * @return A ClassLoader which will never successfully load a class.
	 */
	static ClassSource empty() {
		return fromMap(Collections.emptyMap());
	}

	/**
	 * Append to canonical names together.
	 *
	 * @param a - the name to the left.
	 * @param b - the name to the right.
	 * @return The full canonical name, with a dot seperator.
	 */
	static String append(String a, String b) {
		boolean left = a.endsWith(".");
		boolean right = b.endsWith(".");

		// Only add a dot if necessary
		if (left && right) {
			return a.substring(0, a.length() - 1) + b;
		} else if (left != right) {
			return a + b;
		} else {
			return a + "." + b;
		}
	}

	/**
	 * Retrieve a class source that will retry failed lookups in the given source.
	 *
	 * @param other - the other class source.
	 * @return A new class source.
	 */
	default ClassSource retry(ClassSource other) {
		return canonicalName -> Optionals.or(loadClass(canonicalName), () -> other.loadClass(canonicalName));
	}

	/**
	 * Retrieve a class source that prepends a specific package name to every lookup.
	 *
	 * @param packageName - the package name to prepend.
	 * @return The class source.
	 */
	default ClassSource usingPackage(final String packageName) {
		return canonicalName -> this.loadClass(append(packageName, canonicalName));
	}

	/**
	 * Retrieve a class by its canonical name
	 * @param canonicalName The class's canonical name, i.e. java.lang.Object
	 * @return Optional that may contain a Class
	 */
	Optional<Class<?>> loadClass(String canonicalName);
}
