package com.comphenix.protocol.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class DefaultMethodAccessor implements MethodAccessor {

	private final Method method;
	private final boolean staticMethod;

	private final MethodHandle methodHandle;

	public DefaultMethodAccessor(Method method, MethodHandle methodHandle, boolean staticMethod) {
		this.method = method;
		this.methodHandle = methodHandle;
		this.staticMethod = staticMethod;
	}

	@Override
	public Object invoke(Object target, Object... args) {
		try {
			return this.methodHandle.invoke(target, args);
		} catch (Throwable throwable) {
			throw new IllegalStateException("Unable to invoke method " + this.method, throwable);
		}
	}

	@Override
	public Method getMethod() {
		return this.method;
	}
}
