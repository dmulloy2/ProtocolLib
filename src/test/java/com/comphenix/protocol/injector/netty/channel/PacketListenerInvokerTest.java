package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PacketListenerInvokerTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testInitialization() {
        assertDoesNotThrow(() -> PacketListenerInvoker.ensureStaticInitializedWithoutError());
    }
}
