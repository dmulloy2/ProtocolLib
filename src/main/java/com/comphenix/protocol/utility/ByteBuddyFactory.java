package com.comphenix.protocol.utility;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;

/**
 * Represents a shared ByteBuddy factory.
 *
 * @author Kristian
 */
public final class ByteBuddyFactory {

	private static final ByteBuddyFactory INSTANCE = new ByteBuddyFactory();

	// The current class loader
	private ClassLoader loader = ByteBuddyFactory.class.getClassLoader();

	public static ByteBuddyFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * Get the current class loader we are using.
	 *
	 * @return The current class loader.
	 */
	public ClassLoader getClassLoader() {
		return this.loader;
	}

	/**
	 * Set the current class loader to use when constructing enhancers.
	 *
	 * @param loader - the class loader
	 */
	public void setClassLoader(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * Creates a type builder for a subclass of a given {@link Class}.
	 *
	 * @param clz The class for which to create a subclass.
	 * @return A type builder for creating a new class extending the provided clz and implementing {@link
	 * ByteBuddyGenerated}.
	 */
	public <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<T> createSubclass(Class<T> clz) {
		return new ByteBuddy()
				.subclass(clz)
				.implement(ByteBuddyGenerated.class);
	}

	/**
	 * Creates a type builder for a subclass of a given {@link Class}.
	 *
	 * @param clz                 The class for which to create a subclass.
	 * @param constructorStrategy The constructor strategy to use.
	 * @return A type builder for creating a new class extending the provided clz and implementing {@link
	 * ByteBuddyGenerated}.
	 */
	public <T> DynamicType.Builder.MethodDefinition.ImplementationDefinition.Optional<T> createSubclass(
			Class<T> clz,
			ConstructorStrategy.Default constructorStrategy
	) {
		return new ByteBuddy()
				.subclass(clz, constructorStrategy)
				.implement(ByteBuddyGenerated.class);
	}
}
