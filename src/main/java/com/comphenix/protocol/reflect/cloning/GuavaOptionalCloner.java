/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.reflect.cloning;

import com.google.common.base.Optional;

/**
 * A cloner that can clone Guava Optional objects
 * @author dmulloy2
 */

public class GuavaOptionalCloner implements Cloner {
    protected Cloner wrapped;

    public GuavaOptionalCloner(Cloner wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public boolean canClone(Object source) {
        return source instanceof Optional;
    }

    @Override
    public Object clone(Object source) {
        Optional<?> optional = (Optional<?>) source;
        if (!optional.isPresent()) {
            return Optional.absent();
        }

        // Clone the inner value
        return Optional.of(wrapped.clone(optional.get()));
    }

    public Cloner getWrapped() {
        return wrapped;
    }
}
