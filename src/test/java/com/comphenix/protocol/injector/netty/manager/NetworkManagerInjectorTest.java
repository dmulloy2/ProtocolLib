package com.comphenix.protocol.injector.netty.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;

class NetworkManagerInjectorTest {

    @Test
    void addsInjectionHandlerBeforeNetworkManager() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("before", new ChannelInboundHandlerAdapter());
        channel.pipeline().addLast("network_manager", new TestNetworkManager());
        channel.pipeline().addLast("after", new ChannelInboundHandlerAdapter());

        NetworkManagerInjector.addInjectionHandler(
                channel,
                TestNetworkManager.class,
                new ChannelInboundHandlerAdapter());

        assertEquals(
                channel.pipeline().names().indexOf("network_manager") - 1,
                channel.pipeline().names().indexOf("protocol_lib_inbound_inject"));
    }

    @Test
    void injectsAfterNetworkManagerFinishesChannelActivation() {
        AtomicBoolean networkManagerInitialized = new AtomicBoolean();
        AtomicBoolean observedInitializedNetworkManager = new AtomicBoolean();
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("network_manager", new TestNetworkManager() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) {
                ctx.fireChannelActive();
                networkManagerInitialized.set(true);
            }
        });

        NetworkManagerInjector.addInjectionHandler(
                channel,
                TestNetworkManager.class,
                new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) {
                        ctx.fireChannelActive();
                        observedInitializedNetworkManager.set(networkManagerInitialized.get());
                    }
                });
        channel.pipeline().fireChannelActive();

        assertTrue(observedInitializedNetworkManager.get());
    }

    @Test
    void addsInjectionHandlerLastWhenNetworkManagerIsMissing() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("existing", new ChannelInboundHandlerAdapter());

        NetworkManagerInjector.addInjectionHandler(
                channel,
                TestNetworkManager.class,
                new ChannelInboundHandlerAdapter());

        assertEquals(
                channel.pipeline().names().indexOf("existing") + 1,
                channel.pipeline().names().indexOf("protocol_lib_inbound_inject"));
    }

    private static class TestNetworkManager extends ChannelInboundHandlerAdapter {
    }
}
