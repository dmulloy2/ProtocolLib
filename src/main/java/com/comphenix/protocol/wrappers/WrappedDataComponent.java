package com.comphenix.protocol.wrappers;

import java.lang.reflect.RecordComponent;
import java.util.Optional;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.cloning.AggregateCloner;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a single data component in Minecraft, which is a key-value pair
 * used to store metadata about game objects such as items, mobs, and block entities.
 * <p>
 * This class provides methods to create, access, and manipulate data components,
 * as well as to convert them to and from their underlying NMS handles.
 *
 * @see WrappedDataComponentHolder
 */
public final class WrappedDataComponent {
    private static final Optional<Class<?>> HANDLE_TYPE = MinecraftReflection.getTypedDataComponentClass();

    private static MethodAccessor TYPE_ACCESSOR;
    private static MethodAccessor VALUE_ACCESSOR;
    private static ConstructorAccessor CTOR_ACCESSOR;

    private final Object keyHandle;
    private final String key;

    private Object value;

    static {
        initialize();
    }

    private static void initialize() {
        if (HANDLE_TYPE.isEmpty()) {
            return;
        }

        Class<?> clazz = HANDLE_TYPE.get();
        RecordComponent typeComponent = clazz.getRecordComponents()[0];
        TYPE_ACCESSOR = Accessors.getMethodAccessor(typeComponent.getAccessor());

        RecordComponent valueComponent = clazz.getRecordComponents()[1];
        VALUE_ACCESSOR = Accessors.getMethodAccessor(valueComponent.getAccessor());

        CTOR_ACCESSOR = Accessors.getConstructorAccessor(clazz, typeComponent.getType(), valueComponent.getType());
    }

    WrappedDataComponent(Object keyHandle, String key, Object value) {
        this.keyHandle = keyHandle;
        this.key = key;
        this.value = value;
    }

    /**
     * @return the key of this data component, e.g. "minecraft:custom_data".
     */
    public String getKey() {
        return key;
    }

    /**
     * Updates the in-memory value of this data component.
     * <br>
     * <b>NOTE:</b> this does <b>NOT</b> update the underlying handle
     * @param value new NMS value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return the current in-memory value of this data component
     * @param <T> desired return type
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    /**
     * Creates a new WrappedDataComponent from an existing NMS handle.
     * @param handle NMS handle
     * @return new WrappedDataComponent instance
     */
    public static WrappedDataComponent fromHandle(Object handle) {
        if (TYPE_ACCESSOR == null || VALUE_ACCESSOR == null) {
            throw new IllegalStateException("Unable to access TypedDataComponent");
        }

        Object typeHandle = TYPE_ACCESSOR.invoke(handle);
        Object value = VALUE_ACCESSOR.invoke(handle);

        WrappedRegistry componentTypeRegistry = WrappedRegistry.getDataComponentTypeRegistry();
        String key = componentTypeRegistry.getKey(typeHandle).getFullKey();

        return new WrappedDataComponent(typeHandle, key, value);
    }

    /**
     * Creates a new NMS handle for this data component using the current value.
     * @return new NMS handle
     */
    public Object getHandle() {
        if (CTOR_ACCESSOR == null) {
            throw new IllegalStateException("Unable to access TypedDataComponent constructor");
        }

        return CTOR_ACCESSOR.invoke(keyHandle, value);
    }

    /**
     * Creates a new WrappedDataComponent with the specified key and no value.
     * @param key the key of the data component, e.g. "minecraft:custom_data".
     * @return new WrappedDataComponent instance
     */
    public static WrappedDataComponent create(String key) {
        return create(key, null);
    }

    /**
     * Creates a new WrappedDataComponent with the specified key and value.
     * @param key the key of the data component, e.g. "minecraft:custom_data".
     * @param value the NMS value of the data component, e.g. an ItemStack or Integer.
     * @return new WrappedDataComponent instance
     */
    public static WrappedDataComponent create(String key, Object value) {
        WrappedRegistry componentTypeRegistry = WrappedRegistry.getDataComponentTypeRegistry();
        Object keyHandle = componentTypeRegistry.get(key);

        if (keyHandle == null) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }

        return new WrappedDataComponent(keyHandle, key, value);
    }

    /**
     * Creates a shallow clone of this WrappedDataComponent, which means it copies the key and value references,
     * but does not clone the value itself. This should cover most cases where the value is immutable
     * (e.g. strings, numbers, enums).
     * @return a new WrappedDataComponent instance with the same key and value references
     */
    public WrappedDataComponent shallowClone() {
        return new WrappedDataComponent(keyHandle, key, value);
    }

    /**
     * Creates a deep clone of this WrappedDataComponent, which means it clones the value as well.
     * This is useful when the value is mutable (e.g. books, NBT data) and you want to ensure that
     * changes to the cloned component do not affect the original component.
     * @return a new WrappedDataComponent instance with a cloned value
     */
    public WrappedDataComponent deepClone() {
        Object clonedValue = AggregateCloner.DEFAULT.clone(value);
        return new WrappedDataComponent(keyHandle, key, clonedValue);
    }

    @Override
    public String toString() {
        return "WrappedDataComponent[" + key + "=>" + value + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WrappedDataComponent other)) return false;

        return key.equals(other.key) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
