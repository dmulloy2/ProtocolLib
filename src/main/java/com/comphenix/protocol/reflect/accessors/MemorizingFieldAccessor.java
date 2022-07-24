package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Field;

final class MemorizingFieldAccessor implements FieldAccessor {

	// a marker object which indicates the value of the field wasn't yet read
	private static final Object NIL = new Object();

	private final FieldAccessor inner;
	private volatile Object fieldValue = NIL;

	public MemorizingFieldAccessor(FieldAccessor inner) {
		this.inner = inner;
	}

	@Override
	public Object get(Object instance) {
		if (this.fieldValue == NIL) {
			this.fieldValue = this.inner.get(instance);
		}

		return this.fieldValue;
	}

	@Override
	public void set(Object instance, Object value) {
		this.inner.set(instance, value);
		this.fieldValue = value;
	}

	@Override
	public Field getField() {
		return this.inner.getField();
	}
}
