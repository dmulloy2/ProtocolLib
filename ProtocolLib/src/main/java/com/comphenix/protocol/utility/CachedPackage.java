package com.comphenix.protocol.utility;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Represents a dynamic package and an arbitrary number of cached classes.
 * 
 * @author Kristian
 */
class CachedPackage {
	private Map<String, Class<?>> cache;
	private String packageName;
	
	public CachedPackage(String packageName) {
		this.packageName = packageName;
		this.cache = Maps.newConcurrentMap();
	}
	
	/**
	 * Retrieve the class object of a specific class in the current package.
	 * @param className - the specific class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find the given class.
	 */
	@SuppressWarnings("rawtypes")
	public Class getPackageClass(String className) {
		try {
			Class result = cache.get(className);
			
			// Concurrency is not a problem - we don't care if we look up a class twice
			if (result == null) {
				// Look up the class dynamically
				result = CachedPackage.class.getClassLoader().
							loadClass(packageName + "." + className);
				cache.put(className, result);
			}

			return result;
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find class " + className, e);
		}
	}
}
