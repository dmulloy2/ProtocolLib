package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;

public interface ConstructorAccessor {

    /**
     * NoOp Accessor, does what is says: nothing.
     */
    static final ConstructorAccessor NO_OP_ACCESSOR = new ConstructorAccessor() {
        @Override
        public Object invoke(Object... args) {
            return null;
        }

        @Override
        public Constructor<?> getConstructor() {
            return null;
        }
    };

    /**
     * Invoke the underlying constructor.
     *
     * @param args - the arguments to pass to the method.
     * @return The return value, or NULL for void methods.
     */
    Object invoke(Object... args);

    /**
     * Retrieve the underlying constructor.
     *
     * @return The method.
     */
    Constructor<?> getConstructor();
}
