package com.comphenix.protocol.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;

final class DefaultConstrutorAccessor implements ConstructorAccessor {

	private final Constructor<?> constructor;
	private final MethodHandle constructorAccessor;

	public DefaultConstrutorAccessor(Constructor<?> constructor, MethodHandle constructorAccessor) {
		this.constructor = constructor;
		this.constructorAccessor = constructorAccessor;
	}

	@Override
	public Object invoke(Object... args) {
		try {
			return this.constructorAccessor.invokeExact(args);
		} catch (Throwable throwable) {
			throw new IllegalStateException("Unable to construct new instance using " + this.constructor, throwable);
		}
	}

	@Override
	public Constructor<?> getConstructor() {
		return this.constructor;
	}
}
