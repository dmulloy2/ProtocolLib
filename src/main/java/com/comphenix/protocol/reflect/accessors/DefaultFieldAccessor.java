package com.comphenix.protocol.reflect.accessors;

import com.comphenix.protocol.ProtocolLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.reflect.Field;

final class DefaultFieldAccessor implements FieldAccessor {

	private final Field field;

	private MethodHandle setter;

	public DefaultFieldAccessor(Field field) {
		this.field = field;
		// try to get a getter and setter handle for the field
		if (UnsafeFieldAccess.hasTrustedLookup()) {
			try {
				setter = UnsafeFieldAccess.findSetter(field);
			} catch (ReflectiveOperationException exception) {
				ProtocolLogger.debug("Unable to get setter for field " + field, exception);
			}
		}
	}

	@Override
	public Object get(Object instance) {
		try {
			return field.get(instance);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Cannot read  " + field, e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot use reflection.", e);
		}
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			if (setter == null) {
				field.set(instance, value);
			} else {
				setter.invoke(instance, value);
			}
		} catch (IllegalArgumentException | ClassCastException e) {
			throw new RuntimeException("Cannot set field " + field + " to value " + value, e);
		} catch (IllegalAccessException | WrongMethodTypeException e) {
			throw new IllegalStateException("Cannot use reflection.", e);
		} catch (Throwable ignored) {
			// cannot happen - this might only occur when the handle targets a method
			throw new RuntimeException("Cannot happen");
		}
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public int hashCode() {
		return field != null ? field.hashCode() : 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof DefaultFieldAccessor) {
			DefaultFieldAccessor other = (DefaultFieldAccessor) obj;
			return other.field == field;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DefaultFieldAccessor [field=" + field + "]";
	}
}
