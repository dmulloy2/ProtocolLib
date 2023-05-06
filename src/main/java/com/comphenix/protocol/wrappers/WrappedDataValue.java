package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.google.common.base.Preconditions;

import java.util.Optional;

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
		super(Preconditions.checkNotNull(HANDLE_TYPE, "Cannot find handle type for WrappedDataValue. WrappedDataValue is only supported for Minecraft 1.19.3 or later."));
		this.setHandle(handle);
		this.modifier = new StructureModifier<>(this.handleType).withTarget(handle);
	}

	/**
	 * Constructs a new WrappedDataValue
	 * If a ProtocolLib Wrapper is provided for the param 'value', it will be unwrapped automatically
	 * @param index index of the metadata
	 * @param serializer serializer corresponding to the data type of this value
	 * @param value the NMS handle.
	 */
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

	/**
	 * Retrieves the value associated to this WrappedDataValue. Supported NMS objects will be implicitly wrapped to ProtocolLib wrappers.
	 * For example, if a IChatBaseComponent is stored in this DataValue, this will automatically wrap it to a WrappedChatComponent.
	 * @return Associated value, possible wrapped
	 */
	public Object getValue() {
		return WrappedWatchableObject.getWrapped(getRawValue());
	}

	/**
	 * Updates the value of this DataValue. If the provided value is a ProtocolLib wrapper, it will be unwrapped
	 * implicitly.
	 */
	public void setValue(Object value) {
		setRawValue(WrappedWatchableObject.getUnwrapped(value));
	}

	/**
	 * Returns the raw value associated to this DataValue.
	 * This will not return any ProtocolLib wrappers, e.g., it returns a IChatBaseComponent instead of a WrappedChatComponent.
	 * @return raw object value
	 */
	public Object getRawValue() {
		return this.modifier.readSafely(2);
	}

	/**
	 * Updates the raw value associated to this DataValue.
	 * @param value raw value
	 */
	public void setRawValue(Object value) {
		this.modifier.writeSafely(2, value);
	}
}
