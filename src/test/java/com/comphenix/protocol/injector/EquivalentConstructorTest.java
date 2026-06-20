package com.comphenix.protocol.injector;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EquivalentConstructorTest {

    @Test
    void createRejectsTooFewArguments() {
        EquivalentConstructor constructor = new EquivalentConstructor(null)
                .withParam(String.class);

        assertThrows(IllegalArgumentException.class, constructor::create);
    }

    @Test
    void createRejectsTooManyArguments() {
        EquivalentConstructor constructor = new EquivalentConstructor(null)
                .withParam(String.class);

        assertThrows(IllegalArgumentException.class, () -> constructor.create("one", "two"));
    }
}
