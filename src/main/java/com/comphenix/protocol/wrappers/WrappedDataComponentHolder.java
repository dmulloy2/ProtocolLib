package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;

import org.bukkit.inventory.ItemStack;

/**
 * Provides access to a DataComponentHolder, which is an object that can hold
 * various (meta)data components
 */
public final class WrappedDataComponentHolder extends AbstractWrapper {
    private static MethodAccessor GET_COMPONENTS;
    private static MethodAccessor GET_COMPONENT;

    private static Optional<Class<?>> DATA_COMPONENT_MAP_CLASS = MinecraftReflection.getDataComponentMapClass();
    private static Optional<Class<?>> DATA_COMPONENT_HOLDER_CLASS = MinecraftReflection.getDataComponentHolderClass();

    static {
        initialize();
    }

    private static void initialize() {
        if (DATA_COMPONENT_MAP_CLASS.isEmpty() || DATA_COMPONENT_HOLDER_CLASS.isEmpty()) {
            return;
        }

        Class<?> mapClass = DATA_COMPONENT_MAP_CLASS.get();
        Class<?> holderClass = DATA_COMPONENT_HOLDER_CLASS.get();
        FuzzyReflection fuzzy = FuzzyReflection.fromClass(holderClass, true);
        GET_COMPONENTS = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
            .parameterCount(0)
            .returnTypeExact(mapClass)
            .build()));
        GET_COMPONENT = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
            .parameterCount(1)
            //.requireModifier(Modifier.PUBLIC)
            .banModifier(Modifier.STATIC)
            .parameterExactType(MinecraftReflection.getMinecraftClass("core.component.DataComponentType"))
            .build()));
    }

    public WrappedDataComponentHolder(Object handle) {
        super(DATA_COMPONENT_HOLDER_CLASS.orElseThrow(() -> new IllegalStateException("DataComponentHolder class is not available in this version")));
        setHandle(handle);
    }

    public Map<String, WrappedDataComponent> getAllComponents() {
        Iterable<?> components = (Iterable<?>) GET_COMPONENTS.invoke(handle);
        Map<String, WrappedDataComponent> wrapped = new HashMap<>();
        for (Object componentHandle : components) {
            WrappedDataComponent component = WrappedDataComponent.fromHandle(componentHandle);
            wrapped.put(component.getKey(), component);
        }

        return wrapped;
    }

    public WrappedDataComponent getComponent(String key) {
        Object keyHandle = WrappedRegistry.getDataComponentTypeRegistry().get(key);
        Object valueHandle = GET_COMPONENT.invoke(handle, keyHandle);
        return new WrappedDataComponent(keyHandle, key, valueHandle);
    }

    public static WrappedDataComponentHolder fromItemStack(ItemStack itemStack) {
        Object nmsStack = MinecraftReflection.getMinecraftItemStack(itemStack);
        return new WrappedDataComponentHolder(nmsStack);
    }
}
