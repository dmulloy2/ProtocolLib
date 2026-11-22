package com.comphenix.protocol.injector.netty.channel;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NettyEventLoopProxyTest {

    @Test
    public void testScheduledCallableOfCancelledPacketIsReplaced() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        NettyChannelInjector injector = mock(NettyChannelInjector.class);
        // a listener cancelled the packet sent by the callable
        when(injector.processOutbound(any())).thenReturn(null);

        NettyEventLoopProxy proxy = new NettyEventLoopProxy(channel.eventLoop(), injector);
        boolean[] called = new boolean[1];
        Callable<String> callable = () -> {
            called[0] = true;
            return "sent";
        };

        // used to pass null to netty instead of the no-op callable
        ScheduledFuture<String> future = proxy.schedule(callable, 0, TimeUnit.MILLISECONDS);
        assertNotNull(future);

        channel.runScheduledPendingTasks();
        assertFalse(called[0]);
    }
}
