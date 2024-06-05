package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;

public final class PacketCreator implements Supplier<Object> {
    private ConstructorAccessor constructor = null;
    private MethodAccessor factoryMethod = null;
    private Object[] params = null;
    private boolean failed = false;

    private final Class<?> type;

    private PacketCreator(Class<?> type) {
        this.type = type;
    }

    public static PacketCreator forPacket(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }

        if (!MinecraftReflection.getPacketClass().isAssignableFrom(type)) {
            throw new IllegalArgumentException("Type must be a subclass of Packet.");
        }

        return new PacketCreator(type);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return DefaultInstances.DEFAULT.create(clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public Object get() {
        if (constructor != null) {
            return constructor.invoke(params);
        }

        if (factoryMethod != null) {
            return factoryMethod.invoke(null, params);
        }

        if (failed) {
            return null;
        }

        Object result = null;
        int minCount = Integer.MAX_VALUE;

        for (Constructor<?> testCtor : type.getConstructors()) {
            Class<?>[] paramTypes = testCtor.getParameterTypes();
            if (paramTypes.length > minCount) {
                continue;
            }

            Object[] testParams = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                testParams[i] = createInstance(paramTypes[i]);
            }

            try {
                result = testCtor.newInstance(testParams);
                minCount = paramTypes.length;
                this.constructor = Accessors.getConstructorAccessor(testCtor);
                this.params = testParams;
            } catch (Exception ignored) {
            }
        }

        if (result != null) {
            return result;
        }

        minCount = Integer.MAX_VALUE;

        for (Method testMethod : type.getDeclaredMethods()) {
            int modifiers = testMethod.getModifiers();
            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                continue;
            }

            if (testMethod.getReturnType() != type) {
                continue;
            }

            Class<?>[] paramTypes = testMethod.getParameterTypes();
            if (paramTypes.length > minCount) {
                continue;
            }

            Object[] testParams = new Object[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                testParams[i] = createInstance(paramTypes[i]);
            }

            try {
                result = testMethod.invoke(null, testParams);
                minCount = paramTypes.length;
                this.factoryMethod = Accessors.getMethodAccessor(testMethod);
                this.params = testParams;
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            this.failed = true;
        }

        return result;
    }
}
