package com.comphenix.protocol.reflect.instances;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Rank the default values that a constructor or factory method received against those of the others with as many
     * parameters. One that received a value for every parameter ranks above one that received null, which can leave the
     * instance incomplete, such as the content of a new SYSTEM_CHAT packet. Then one that received more empty arrays,
     * collections, maps and optionals ranks higher, so that a new instance does not hold a made-up element, such as an
     * entry of a new PLAYER_INFO packet.
     */
    private static int rank(Object[] params) {
        boolean complete = true;
        int empty = 0;
        for (Object param : params) {
            if (param == null) {
                complete = false;
            } else if (isEmpty(param)) {
                empty++;
            }
        }
        // there are at most params.length empty values, so this is above every rank of values that contain null
        return complete ? params.length + 1 + empty : empty;
    }

    private static boolean isEmpty(Object value) {
        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }
        if (value instanceof Optional) {
            return !((Optional<?>) value).isPresent();
        }
        return value.getClass().isArray() && Array.getLength(value) == 0;
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
        int bestRank = -1;

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
            // fewest parameters, keep the one whose default values rank best.
            int testRank = rank(testParams);
            if (paramTypes.length == minCount && testRank < bestRank) {
                continue;
            }

            try {
                ConstructorAccessor testAccessor = Accessors.getConstructorAccessor(testCtor);
                result = testAccessor.invoke(testParams);
                minCount = paramTypes.length;
                bestRank = testRank;
                this.constructor = testAccessor;
                this.paramTypes = paramTypes;
            } catch (Exception ignored) {
            }
        }

        if (result != null) {
            return result;
        }

        minCount = Integer.MAX_VALUE;
        bestRank = -1;

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
            int testRank = rank(testParams);
            if (paramTypes.length == minCount && testRank < bestRank) {
                continue;
            }

            try {
                MethodAccessor testAccessor = Accessors.getMethodAccessor(testMethod);
                result = testAccessor.invoke(null, testParams);
                minCount = paramTypes.length;
                bestRank = testRank;
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
