package com.comphenix.protocol.injector.netty.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import com.comphenix.protocol.internal.PlatformProvider;
class PlatformChannelInitializerTest {

    @Test
    void reportsPaperInitializerUnavailable() {
        PlatformProvider provider = PlatformProvider.get();
        assertInstanceOf(com.comphenix.protocol.spigot.SpigotPlatformProvider.class, provider);
        assertFalse(provider.hasEarlyChannelInitialization());
    }
}
