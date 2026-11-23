package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;

public final class InstanceCreator implements Supplier<Object> {
    private static Map<Class<?>, Object> BANNED_PARAMETERS = new WeakHashMap<>();

    static {
        try {
            BANNED_PARAMETERS.put(ByteBuffer.class, true);
            BANNED_PARAMETERS.put(FloatBuffer.class, true);
        } catch (Throwable ignored) {
        }
    }

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

    private static boolean containsNull(Object[] params) {
        for (Object param : params) {
            if (param == null) {
                return true;
            }
        }
        return false;
    }

    private boolean containsBannedParameter(Class<?>[] paramTypes) {
        for (Class<?> paramType : paramTypes) {
            if (BANNED_PARAMETERS.containsKey(paramType)) {
                return true;
            }
        }
        return false;
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
        boolean complete = false;

        for (Constructor<?> testCtor : type.getDeclaredConstructors()) {
            Class<?>[] paramTypes = testCtor.getParameterTypes();
            if (paramTypes.length > minCount) {
                continue;
            }

            if (containsBannedParameter(paramTypes)) {
                continue;
            }

            Object[] testParams = createParams(paramTypes);

            // The JVM lists constructors in an unspecified order that can differ between runs. Among those with the
            // fewest parameters, keep one that received a value for every parameter over one that received null,
            // which can leave the instance incomplete, such as the content of a new SYSTEM_CHAT packet.
            boolean testComplete = !containsNull(testParams);
            if (paramTypes.length == minCount && complete && !testComplete) {
                continue;
            }

            try {
                ConstructorAccessor testAccessor = Accessors.getConstructorAccessor(testCtor);
                result = testAccessor.invoke(testParams);
                minCount = paramTypes.length;
                complete = testComplete;
                this.constructor = testAccessor;
                this.paramTypes = paramTypes;
            } catch (Exception ignored) {
            }
        }

        if (result != null) {
            return result;
        }

        minCount = Integer.MAX_VALUE;
        complete = false;

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

            if (containsBannedParameter(paramTypes)) {
                continue;
            }

            Object[] testParams = createParams(paramTypes);

            // same preference as for the constructors
            boolean testComplete = !containsNull(testParams);
            if (paramTypes.length == minCount && complete && !testComplete) {
                continue;
            }

            try {
                MethodAccessor testAccessor = Accessors.getMethodAccessor(testMethod);
                result = testAccessor.invoke(null, testParams);
                minCount = paramTypes.length;
                complete = testComplete;
                this.factoryMethod = testAccessor;
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
