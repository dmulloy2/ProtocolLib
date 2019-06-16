package com.comphenix.protocol.utility;

import net.sf.cglib.proxy.Enhancer;

/**
 * Represents a shared enchancer factory.
 * @author Kristian
 */
public class EnhancerFactory {
	private static EnhancerFactory INSTANCE = new EnhancerFactory();
	
	// The current class loader
	private ClassLoader loader = EnhancerFactory.class.getClassLoader();
	
	public static EnhancerFactory getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Create a new CGLib enhancer.
	 * @return The new enhancer.
	 */
	public Enhancer createEnhancer() {
		Enhancer enhancer = new Enhancer();
		enhancer.setClassLoader(loader);
		return enhancer;
	}
	
	/**
	 * Set the current class loader to use when constructing enhancers.
	 * @param loader - the class loader
	 */
	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}
	
	/**
	 * Get the current class loader we are using.
	 * @return The current class loader.
	 */
	public ClassLoader getClassLoader() {
		return loader;
	}
}
