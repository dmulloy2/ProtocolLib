package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;

/**
 * Represents a DataValue in 1.19.3+.
 */
public class WrappedDataValue extends AbstractWrapper {

	private static final Class<?> HANDLE_TYPE = MinecraftReflection.getNullableNMS("network.syncher.DataWatcher$b", "network.syncher.SynchedEntityData$DataValue");

	private static ConstructorAccessor constructor;

	private final StructureModifier<Object> modifier;

	/**
	 * Construct a new NMS wrapper.
	 *
	 * @param handle the wrapped data value.
	 */
	public WrappedDataValue(Object handle) {
		super(HANDLE_TYPE);
		this.setHandle(handle);
		this.modifier = new StructureModifier<>(this.handleType).withTarget(handle);
	}

	public WrappedDataValue(int index, Serializer serializer, Object value) {
		this(newHandle(index, serializer, value));
	}

	private static Object newHandle(int index, Serializer serializer, Object value) {
		if (constructor == null) {
			constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
		}

		return constructor.invoke(index, serializer.getHandle(), value);
	}

	public int getIndex() {
		return this.modifier.<Integer>withType(int.class).read(0);
	}

	public void setIndex(int index) {
		this.modifier.withType(int.class).write(0, index);
	}

	public Serializer getSerializer() {
		Object serializer = this.modifier.readSafely(1);
		if (serializer != null) {
			Serializer wrapper = Registry.fromHandle(serializer);
			if (wrapper != null) {
				return wrapper;
			} else {
				return new Serializer(null, serializer, false);
			}
		} else {
			return null;
		}
	}

	public void setSerializer(Serializer serializer) {
		this.modifier.writeSafely(1, serializer == null ? null : serializer.getHandle());
	}

	public Object getValue() {
		return WrappedWatchableObject.getWrapped(getRawValue());
	}

	public void setValue(Object value) {
		setRawValue(WrappedWatchableObject.getUnwrapped(value));
	}

	public Object getRawValue() {
		return this.modifier.readSafely(2);
	}

	public void setRawValue(Object value) {
		this.modifier.writeSafely(2, value);
	}
}
