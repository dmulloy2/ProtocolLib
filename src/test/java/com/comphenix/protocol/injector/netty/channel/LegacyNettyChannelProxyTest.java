package com.comphenix.protocol.injector.netty.channel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LegacyNettyChannelProxyTest {

    // types that only exist since netty 4.1, Minecraft 1.8 - 1.11 ship netty 4.0
    private static final String[] NETTY_41_TYPES = {
            "io/netty/channel/AbstractEventLoop",
            "io/netty/channel/ChannelOutboundInvoker"
    };

    @Test
    public void testClassesLoadedOnEveryServerDoNotReferenceNetty41Types() throws IOException {
        // NettyChannelProxy is excluded on purpose: it is only loaded on netty 4.1+
        for (Class<?> type : new Class<?>[]{NettyEventLoopProxy.class, LegacyNettyChannelProxy.class,
                NettyChannelInjector.class}) {
            String bytecode = readClass(type);
            for (String netty41Type : NETTY_41_TYPES) {
                assertFalse(bytecode.contains(netty41Type),
                        type.getSimpleName() + " references " + netty41Type + " and would fail to load on netty 4.0");
            }
        }
    }

    @Test
    public void testDynamicProxyDelegatesToChannel() {
        EmbeddedChannel channel = new EmbeddedChannel();
        EventLoop eventLoop = mock(EventLoop.class);
        NettyChannelInjector injector = mock(NettyChannelInjector.class);
        when(injector.isPacketEventCallNeeded(any())).thenReturn(false);

        Channel proxy = LegacyNettyChannelProxy.createDynamicProxy(channel, eventLoop, injector);

        assertSame(eventLoop, proxy.eventLoop());
        assertEquals(channel.isActive(), proxy.isActive());
        assertSame(channel.pipeline(), proxy.pipeline());

        // messages that are not packets are written to the channel directly
        proxy.writeAndFlush("not a packet");
        assertEquals("not a packet", channel.readOutbound());
        verify(injector, never()).processPacketOutbound(any(), any());

        // identity semantics, like the compiled proxy
        assertTrue(proxy.equals(proxy));
        assertNotEquals(proxy, channel);
        assertEquals(System.identityHashCode(proxy), proxy.hashCode());

        assertTrue(LegacyNettyChannelProxy.isChannelProxy(proxy));
        assertFalse(LegacyNettyChannelProxy.isChannelProxy(channel));
        assertFalse(LegacyNettyChannelProxy.isChannelProxy(null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDynamicProxyRoutesPacketsThroughInjector() {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyChannelInjector injector = mock(NettyChannelInjector.class);
        Object packet = "packet stand-in";
        when(injector.isPacketEventCallNeeded(packet)).thenReturn(true);
        // the injector decides to send the packet unchanged
        doAnswer(invocation -> {
            ((Consumer<Object>) invocation.getArgument(1)).accept(invocation.getArgument(0));
            return null;
        }).when(injector).processPacketOutbound(eq(packet), any());

        Channel proxy = LegacyNettyChannelProxy.createDynamicProxy(channel, channel.eventLoop(), injector);
        proxy.writeAndFlush(packet);

        verify(injector).processPacketOutbound(eq(packet), any());
        assertEquals(packet, channel.readOutbound());
    }

    @Test
    public void testCompiledProxyIsUsedOnCurrentNetty() {
        EmbeddedChannel channel = new EmbeddedChannel();
        Channel proxy = LegacyNettyChannelProxy.createChannelProxy(channel, channel.eventLoop(),
                mock(NettyChannelInjector.class));

        assertTrue(proxy instanceof NettyChannelProxy);
        assertTrue(LegacyNettyChannelProxy.isChannelProxy(proxy));
    }

    private static String readClass(Class<?> type) throws IOException {
        String resource = type.getName().replace('.', '/') + ".class";
        try (InputStream input = type.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Class file not found: " + resource);
            }
            // constant pool entries are modified UTF-8, ISO-8859-1 keeps every byte for the ASCII search
            return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }
}
