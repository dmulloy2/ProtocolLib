package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;

/**
 * Represents a DataValue in 1.19.3+.
 * Use {@link WrappedWatchableObject} before 1.19.3.
 */
public class WrappedDataValue extends AbstractWrapper {

    // TODO: need a better solution
    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getNullableNMS(
        "network.syncher.SynchedEntityData$DataValue",
        "network.syncher.DataWatcher$b",
        "network.syncher.DataWatcher$c"
    );

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

    /**
     * Creates a new WrappedDataValue from a NMS value.
     * ProtocolLib wrappers are not supported as arguments.
     * If implicit unwrapping of wrappers is required, use {@link WrappedDataValue#fromWrappedValue(int, Serializer, Object)}.
     * @param index the index of the metadata value
     * @param serializer the serializer corresponding for serializing. Can be null.
     * @param value The raw value for the DataValue. Can be null.
     */
    public WrappedDataValue(int index, Serializer serializer, Object value) {
        this(newHandle(index, serializer, value));
    }

    /**
     * Creates a new WrappedDataValue from a possibly wrapped value and implicitly unwrap value if possible.
     * @param index the index of the metadata value
     * @param serializer the serializer corresponding for serializing. Can be null.
     * @param value The value for the DataValue. Can be null.
     */
    public static WrappedDataValue fromWrappedValue(int index, Serializer serializer, Object value) {
        return new WrappedDataValue(index, serializer, value == null ? null : WrappedWatchableObject.getUnwrapped(value));
    }

    private static Object newHandle(int index, Serializer serializer, Object value) {
        if (constructor == null) {
            constructor = Accessors.getConstructorAccessor(HANDLE_TYPE.getConstructors()[0]);
        }

        return constructor.invoke(index, serializer.getHandle(), value);
    }

    /**
     * Returns the entity-type specific index of this DataValue
     * @return index of the DataValue
     */
    public int getIndex() {
        return this.modifier.<Integer>withType(int.class).read(0);
    }

    /**
     * Sets the entity-type specific index of this DataValue
     * @param index New index of the DataValue
     */
    public void setIndex(int index) {
        this.modifier.withType(int.class).write(0, index);
    }

    /**
     * Returns the current serializer for this DataValue.
     * @return serializer
     */
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

    /**
     * Changes the serializer for this DataValue
     * @param serializer serializer
     */
    public void setSerializer(Serializer serializer) {
        this.modifier.writeSafely(1, serializer == null ? null : serializer.getHandle());
    }

    /**
     * Returns the current value associated and implicitly wraps it to corresponding ProtocolLib wrappers if possible.
     * @return Current value
     */
    public Object getValue() {
        return WrappedWatchableObject.getWrapped(getRawValue());
    }

    /**
     * Sets the current value associated and implicitly unwraps it to NMS types if a ProtocolLib wrapper is provided.
     * @param value New value for this DataValue
     */
    public void setValue(Object value) {
        setRawValue(WrappedWatchableObject.getUnwrapped(value));
    }

    /**
     * Returns the current, raw value.
     * @return Raw value (not wrapped)
     */
    public Object getRawValue() {
        return this.modifier.readSafely(2);
    }

    /**
     * Updates the raw value for this DataValue. No unwrapping will be applied.
     * @param value NMS value
     */
    public void setRawValue(Object value) {
        this.modifier.writeSafely(2, value);
    }
}
