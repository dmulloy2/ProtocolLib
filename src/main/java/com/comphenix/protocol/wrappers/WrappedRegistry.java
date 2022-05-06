package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class WrappedRegistry {
    // map of NMS class to registry instance
    private static final Map<Class<?>, WrappedRegistry> REGISTRY;

    private static final MethodAccessor GET;
    private static final MethodAccessor GET_KEY;

    static {
        Map<Class<?>, WrappedRegistry> regMap = new HashMap<>();

        Class<?> regClass = MinecraftReflection.getIRegistry();
        if (regClass != null) {
            for (Field field : regClass.getFields()) {
                try {
                    // make sure it's actually a registry
                    if (field.getType().isAssignableFrom(regClass)) {
                        Type genType = field.getGenericType();
                        if (genType instanceof ParameterizedType) {
                            ParameterizedType par = (ParameterizedType) genType;
                            Type paramType = par.getActualTypeArguments()[0];
                            if (paramType instanceof Class) {
                                regMap.put((Class<?>) paramType, new WrappedRegistry(field.get(null)));
                            }
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }

        REGISTRY = ImmutableMap.copyOf(regMap);

        FuzzyReflection fuzzy = FuzzyReflection.fromClass(regClass, false);
        GET = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                .newBuilder()
            		.parameterCount(1)
            		.returnDerivedOf(Object.class)
            		.requireModifier(Modifier.ABSTRACT)
            		.parameterExactType(MinecraftReflection.getMinecraftKeyClass())
            		.build()));
        GET_KEY = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
                .newBuilder()
                .parameterCount(1)
                .returnTypeExact(MinecraftReflection.getMinecraftKeyClass())
                .build()));
    }

    private final Object handle;

    private WrappedRegistry(Object handle) {
        this.handle = handle;
    }

    public Object get(MinecraftKey key) {
        return GET.invoke(handle, MinecraftKey.getConverter().getGeneric(key));
    }

    public Object get(String key) {
        return get(new MinecraftKey(key));
    }

    public MinecraftKey getKey(Object generic) {
        return MinecraftKey.getConverter().getSpecific(GET_KEY.invoke(handle, generic));
    }

    // TODO add more methods

    public static WrappedRegistry getAttributeRegistry() {
        return REGISTRY.get(MinecraftReflection.getAttributeBase());
    }

    public static WrappedRegistry getDimensionRegistry() {
        return REGISTRY.get(MinecraftReflection.getDimensionManager());
    }

    public static WrappedRegistry getBlockEntityTypeRegistry() {
        return REGISTRY.get(MinecraftReflection.getBlockEntityTypeClass());
    }
}
