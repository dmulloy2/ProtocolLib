package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;

final class DefaultFieldAccessor implements FieldAccessor {
	private final Field field;
	
	public DefaultFieldAccessor(Field field) {
		this.field = field;
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
			field.set(instance, value);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Cannot set field " + field + " to value " + value, e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot use reflection.", e);
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
		if (this == obj)
			return true;
		
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