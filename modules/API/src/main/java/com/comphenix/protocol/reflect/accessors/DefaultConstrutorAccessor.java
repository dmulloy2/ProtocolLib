package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

final class DefaultConstrutorAccessor implements ConstructorAccessor {
	private final Constructor<?> constructor;
	
	public DefaultConstrutorAccessor(Constructor<?> method) {
		this.constructor = method;
	}
	
	@Override
	public Object invoke(Object... args) {
		try {
			return constructor.newInstance(args);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot use reflection.", e);
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (InvocationTargetException e) {
			throw new RuntimeException("An internal error occured.", e.getCause());
		} catch (InstantiationException e) {
			throw new RuntimeException("Cannot instantiate object.", e);
		}
	}
	
	@Override
	public Constructor<?> getConstructor() {
		return constructor;
	}

	@Override
	public int hashCode() {
		return constructor != null ? constructor.hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj instanceof DefaultConstrutorAccessor) {
			DefaultConstrutorAccessor other = (DefaultConstrutorAccessor) obj;
			return other.constructor == constructor;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "DefaultConstrutorAccessor [constructor=" + constructor + "]";
	}
}