package com.comphenix.protocol.reflect.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.google.common.base.Preconditions;

final class DefaultFieldAccessor implements FieldAccessor {

	private final Field field;
	private final boolean staticField;

	private final MethodHandle setter;
	private final MethodHandle getter;

	public DefaultFieldAccessor(Field field, MethodHandle setter, MethodHandle getter, boolean staticField) {
		this.field = Preconditions.checkNotNull(field, "field");
		this.setter = Preconditions.checkNotNull(setter, "setter");
		this.getter = Preconditions.checkNotNull(getter, "getter");
		this.staticField = staticField;
	}

	@Override
	public Object get(Object instance) {
		try {
			// we need this check to as the handle will treat "null" as an instance too
			return this.staticField ? this.getter.invokeExact() : this.getter.invokeExact(instance);
		} catch (Throwable throwable) {
			throw new IllegalStateException("Unable to read field value of " + this.field, throwable);
		}
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			// we need this check to as the handle will treat "null" as an instance too
			if (this.staticField) {
				this.setter.invokeExact(value);
			} else {
				this.setter.invokeExact(instance, value);
			}
		} catch (Throwable throwable) {
			throw new IllegalStateException("Unable to set value of field " + this.field, throwable);
		}
	}

	@Override
	public Field getField() {
		return this.field;
	}
}
