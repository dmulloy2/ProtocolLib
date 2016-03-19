package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class DefaultMethodAccessor implements MethodAccessor {
	private final Method method;
	
	public DefaultMethodAccessor(Method method) {
		this.method = method;
	}
	
	@Override
	public Object invoke(Object target, Object... args) {
		try {
			return method.invoke(target, args);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot use reflection.", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("An internal error occured.", e.getCause());
		} catch (IllegalArgumentException e) {
			throw e;
		}
	}
	
	@Override
	public Method getMethod() {
		return method;
	}

	@Override
	public int hashCode() {
		return method != null ? method.hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof DefaultMethodAccessor) {
			DefaultMethodAccessor other = (DefaultMethodAccessor) obj;
			return other.method == method;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "DefaultMethodAccessor [method=" + method + "]";
	}
}