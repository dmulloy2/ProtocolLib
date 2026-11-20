package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Field;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.ListenerManager;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.protocol.Packet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OutboundFastPathTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testPacketWithoutListenerSkipsProtocolLookup() throws Exception {
        ListenerManager listenerManager = mock(ListenerManager.class);
        NettyChannelInjector injector = createInjector(listenerManager);
        SendAction action = new SendAction(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT).getHandle());

        assertSame(action, injector.processOutbound(action));

        verify(injector, never()).getCurrentProtocol(any());
        verify(listenerManager, never()).invokeOutboundPacketListeners(any());
    }

    @Test
    public void testPacketWithListenerIsProcessed() throws Exception {
        ListenerManager listenerManager = mock(ListenerManager.class);
        when(listenerManager.mayHaveOutboundListener(any())).thenReturn(true);
        when(listenerManager.hasOutboundListener(PacketType.Play.Server.SYSTEM_CHAT)).thenReturn(true);
        NettyChannelInjector injector = createInjector(listenerManager);
        SendAction action = new SendAction(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT).getHandle());

        assertSame(action, injector.processOutbound(action));

        verify(listenerManager).invokeOutboundPacketListeners(any());
    }

    @Test
    public void testBundleWithoutListenersIsSkipped() throws Exception {
        ListenerManager listenerManager = mock(ListenerManager.class);
        NettyChannelInjector injector = createInjector(listenerManager);
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));
        SendAction action = new SendAction(bundle.getHandle());

        assertSame(action, injector.processOutbound(action));

        // neither the bundle nor its packets have listeners: no events, no protocol lookup
        verify(injector, never()).getCurrentProtocol(any());
        verify(listenerManager, never()).invokeOutboundPacketListeners(any());
    }

    @Test
    public void testBundleWithListenedSubPacketIsProcessed() throws Exception {
        ListenerManager listenerManager = mock(ListenerManager.class);
        Class<?> chatClass = PacketType.Play.Server.SYSTEM_CHAT.getPacketClass();
        when(listenerManager.mayHaveOutboundListener(chatClass)).thenReturn(true);
        when(listenerManager.hasOutboundListener(PacketType.Play.Server.SYSTEM_CHAT)).thenReturn(true);
        NettyChannelInjector injector = createInjector(listenerManager);
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));

        injector.processOutbound(new SendAction(bundle.getHandle()));

        verify(listenerManager).invokeOutboundPacketListeners(any());
    }

    private static NettyChannelInjector createInjector(ListenerManager listenerManager) throws Exception {
        // the real Connection can't be loaded in tests, so the injector gets a stand-in and a mocked invoker
        NettyChannelInjector injector;
        try (MockedConstruction<PacketListenerInvoker> ignored = mockConstruction(PacketListenerInvoker.class)) {
            injector = spy(new NettyChannelInjector(null, new FakeNetworkManager(), new EmbeddedChannel(),
                    listenerManager, mock(InjectionFactory.class), mock(ErrorReporter.class)));
        }
        doReturn(Protocol.PLAY).when(injector).getCurrentProtocol(PacketType.Sender.SERVER);
        Field injected = NettyChannelInjector.class.getDeclaredField("injected");
        injected.setAccessible(true);
        injected.setBoolean(injector, true);
        return injector;
    }

    private static final class FakeNetworkManager {

        private Channel channel;
    }

    private static final class SendAction implements Runnable {

        private final Packet<?> packet;

        private SendAction(Object packet) {
            this.packet = (Packet<?>) packet;
        }

        @Override
        public void run() {
        }
    }
}
