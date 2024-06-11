package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;

public final class InstanceCreator implements Supplier<Object> {
    private ConstructorAccessor constructor = null;
    private MethodAccessor factoryMethod = null;
    private Class<?>[] paramTypes = null;
    private boolean failed = false;

    private final Class<?> type;

    private InstanceCreator(Class<?> type) {
        this.type = type;
    }

    public static InstanceCreator forClass(Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }

        return new InstanceCreator(type);
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return DefaultInstances.DEFAULT.create(clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Object[] createParams(Class<?>[] paramTypes) {
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            params[i] = createInstance(paramTypes[i]);
        }
        return params;
    }

    @Override
    public Object get() {
        Object[] params = paramTypes != null ? createParams(paramTypes) : null;

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

            Object[] testParams = createParams(paramTypes);

            try {
                result = testCtor.newInstance(testParams);
                minCount = paramTypes.length;
                this.constructor = Accessors.getConstructorAccessor(testCtor);
                this.paramTypes = paramTypes;
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

            Object[] testParams = createParams(paramTypes);

            try {
                result = testMethod.invoke(null, testParams);
                minCount = paramTypes.length;
                this.factoryMethod = Accessors.getMethodAccessor(testMethod);
                this.paramTypes = paramTypes;
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            this.failed = true;
        }

        return result;
    }
}
