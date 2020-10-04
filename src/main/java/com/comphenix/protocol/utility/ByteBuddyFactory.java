package com.comphenix.protocol.utility;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;

/**
 * Represents a shared ByteBuddy factory.
 * @author Kristian
 */
public class ByteBuddyFactory {
	private static ByteBuddyFactory INSTANCE = new ByteBuddyFactory();
	
	// The current class loader
	private ClassLoader loader = ByteBuddyFactory.class.getClassLoader();
	
	public static ByteBuddyFactory getInstance() {
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

	/**
	 * Creates a type builder for a subclass of a given {@link Class}.
	 * @param clz The class for which to create a subclass.
	 * @return A type builder for creating a new class extending the provided clz and implementing
	 * {@link ByteBuddyGenerated}.
	 */
	public DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<?> createSubclass(Class<?> clz)
	{
		return new ByteBuddy()
				.subclass(clz)
				.implement(ByteBuddyGenerated.class);
	}
}
