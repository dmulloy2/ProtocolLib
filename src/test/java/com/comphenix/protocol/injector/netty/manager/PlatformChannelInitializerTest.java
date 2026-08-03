package com.comphenix.protocol.injector.netty.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.internal.PlatformProvider;
import com.comphenix.protocol.paper.PaperPlatformProvider;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import io.papermc.paper.network.ChannelInitializeListenerHolder;

class PlatformChannelInitializerTest {

    @AfterEach
    void resetHolder() {
        ChannelInitializeListenerHolder.removeListener(PaperPlatformProvider.CHANNEL_INITIALIZER_KEY);
    }

    @Test
    void registersInvokesAndRemovesListener() {
        AtomicReference<Channel> initializedChannel = new AtomicReference<>();

        PlatformProvider provider = PlatformProvider.get();
        Runnable cleanup = provider.registerChannelInitializer(initializedChannel::set);
        assertTrue(provider.hasEarlyChannelInitialization());
        assertTrue(ChannelInitializeListenerHolder.hasListener(PaperPlatformProvider.CHANNEL_INITIALIZER_KEY));

        Channel channel = new EmbeddedChannel();
        ChannelInitializeListenerHolder.callListeners(channel);
        assertSame(channel, initializedChannel.get());

        cleanup.run();
        assertFalse(ChannelInitializeListenerHolder.hasListener(PaperPlatformProvider.CHANNEL_INITIALIZER_KEY));
    }
}
