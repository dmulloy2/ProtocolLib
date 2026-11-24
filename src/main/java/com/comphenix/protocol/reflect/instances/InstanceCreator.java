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

    // How every parameter of the chosen constructor or factory method receives its value. Set once the choice is
    // made, after the fields above, and made again when the settings of DefaultInstances.DEFAULT change.
    private volatile ParamPlan paramPlan = null;

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
     * A parameter whose default value is a new object every time, such as a list or a block position. Every instance
     * receives its own value, so that changing the value of one instance does not change another.
     */
    private static final class FreshValue {

        private final Class<?> type;

        private FreshValue(Class<?> type) {
            this.type = type;
        }
    }

    /**
     * The value of every parameter of the chosen constructor or factory method: NULL, a value that every instance
     * receives, or a FreshValue for a value that is created for every instance. It holds as long as the settings of
     * DefaultInstances.DEFAULT stay as they were in the given revision, since the default values depend on them.
     */
    private static final class ParamPlan {

        private final Object[] values;
        private final int revision;

        private ParamPlan(Object[] values, int revision) {
            this.values = values;
            this.revision = revision;
        }
    }

    /**
     * Decide how every instance receives the default value of each parameter of the chosen constructor or factory
     * method, given the values it received when it was chosen.
     * <p>
     * Searching for a default value can take microseconds, mostly when there is none: a new BLOCK_CHANGE packet spent
     * that long on its block state, only to receive NULL. A parameter without a default value therefore keeps
     * receiving NULL, and a parameter whose default value is the same object every time, such as a boxed number, an
     * enum constant or an empty optional, keeps receiving that object. Only the other parameters are created again.
     */
    private Object[] planParams(Class<?>[] paramTypes, Object[] chosenParams) {
        Object[] values = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object again = createInstance(paramTypes[i]);
            if (chosenParams[i] == null && again == null) {
                values[i] = null;
            } else if (chosenParams[i] == again) {
                values[i] = again;
            } else {
                values[i] = new FreshValue(paramTypes[i]);
            }
        }
        return values;
    }

    private Object[] createParams(Object[] paramValues) {
        Object[] params = paramValues.clone();
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof FreshValue) {
                params[i] = createInstance(((FreshValue) params[i]).type);
            }
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
        ParamPlan plan = this.paramPlan;
        if (plan != null) {
            // the constructor or factory method was chosen
            Object[] params;
            int revision = DefaultInstances.DEFAULT.getRevision();
            if (plan.revision == revision) {
                params = createParams(plan.values);
            } else {
                // the settings of DEFAULT changed, and so may have the default values
                params = createParams(paramTypes);
                this.paramPlan = new ParamPlan(planParams(paramTypes, params), revision);
            }
            return constructor != null ? constructor.invoke(params) : factoryMethod.invoke(null, params);
        }

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

        // read before any default value is created, so that a change of the settings during the choice is noticed
        int revision = DefaultInstances.DEFAULT.getRevision();
        Object result = null;
        Object[] chosenParams = null;
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
                chosenParams = testParams;
                minCount = paramTypes.length;
                bestRank = testRank;
                this.constructor = testAccessor;
                this.paramTypes = paramTypes;
            } catch (Exception ignored) {
            }
        }

        if (result != null) {
            this.paramPlan = new ParamPlan(planParams(this.paramTypes, chosenParams), revision);
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
                Object testResult = testAccessor.invoke(null, testParams);
                if (testResult == null) {
                    // this factory cannot create the instance, so it must neither replace one that did nor rule out
                    // one that ranks lower
                    continue;
                }

                result = testResult;
                chosenParams = testParams;
                minCount = paramTypes.length;
                bestRank = testRank;
                this.factoryMethod = testAccessor;
                this.paramTypes = paramTypes;
            } catch (Exception ignored) {
            }
        }

        if (result == null) {
            this.failed = true;
        } else {
            this.paramPlan = new ParamPlan(planParams(this.paramTypes, chosenParams), revision);
        }

        return result;
    }
}
