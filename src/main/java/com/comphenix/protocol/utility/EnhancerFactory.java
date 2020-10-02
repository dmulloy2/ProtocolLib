package com.comphenix.protocol.utility;

/**
 * Represents a shared enchancer factory.
 * @author Kristian
 * @deprecated This class should be removed.
 */
// TODO:P Remove this class. Currently, it's only used to access the shared class loader,
//        but a better alternative for that can be found.
@Deprecated
public class EnhancerFactory {
	private static EnhancerFactory INSTANCE = new EnhancerFactory();
	
	// The current class loader
	private ClassLoader loader = EnhancerFactory.class.getClassLoader();
	
	public static EnhancerFactory getInstance() {
		return INSTANCE;
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
