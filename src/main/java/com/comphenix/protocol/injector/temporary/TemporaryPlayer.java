package com.comphenix.protocol.injector.temporary;

import java.util.Objects;

import com.comphenix.protocol.injector.netty.Injector;

/**
 * A temporary player created by ProtocolLib when a true player instance does not exist.
 * <p>
 * Also able to store a socket injector
 * </p>
 */
public class TemporaryPlayer {

    protected volatile Injector injector;

    public Injector getInjector() {
        return this.injector;
    }

    void setInjector(Injector injector) {
        Objects.requireNonNull(injector, "injector can't be null");

        if (this.injector != null) {
            throw new IllegalStateException("Can't redefine injector for temporary player");
        }

        this.injector = injector;
    }
}
