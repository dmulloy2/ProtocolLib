package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.IRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WrappedRegistry {
    private static final Map<Class<?>, Object> REGISTRY;

    static {
        Map<Class<?>, Object> regMap = new HashMap<>();
        for (Field field : IRegistry.class.getFields()) {
            try {
                if (field.getType().isAssignableFrom(IRegistry.class)) {
                    Type genType = field.getGenericType();
                    if (genType instanceof ParameterizedType) {
                        ParameterizedType par = (ParameterizedType) genType;
                        Type paramType = par.getActualTypeArguments()[0];
                        if (paramType instanceof Class) {
                            regMap.put((Class<?>) paramType, field.get(null));
                        }
                    }
                }
            } catch (ReflectiveOperationException ignored) {}
        }

        REGISTRY = ImmutableMap.copyOf(regMap);
    }

    public static Object getAttributeRegistry() {
        return REGISTRY.get(MinecraftReflection.getAttributeBase());
    }
}
