package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.Field;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketPostListener;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.scheduler.ProtocolScheduler;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NettyChannelInjectorTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testBundleNeedsMainThreadForSubPacketListener() {
        ListenerManager listenerManager = createListenerManager(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer bundle = createBundle(
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE),
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT));

        assertTrue(NettyChannelInjector.hasMainThreadListener(listenerManager, Protocol.PLAY, bundle));
    }

    @Test
    public void testBundleNeedsMainThreadForBundleListener() {
        ListenerManager listenerManager = createListenerManager(PacketType.Play.Server.BUNDLE);
        PacketContainer bundle = createBundle(new PacketContainer(PacketType.Play.Server.KEEP_ALIVE));

        assertTrue(NettyChannelInjector.hasMainThreadListener(listenerManager, Protocol.PLAY, bundle));
    }

    @Test
    public void testBundleWithoutListenedSubPacketsStaysOffMainThread() {
        ListenerManager listenerManager = createListenerManager(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer bundle = createBundle(
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE),
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE));

        assertFalse(NettyChannelInjector.hasMainThreadListener(listenerManager, Protocol.PLAY, bundle));
    }

    @Test
    public void testSinglePacketOnlyChecksItsOwnType() {
        ListenerManager listenerManager = createListenerManager(PacketType.Play.Server.SYSTEM_CHAT);

        assertTrue(NettyChannelInjector.hasMainThreadListener(listenerManager, Protocol.PLAY,
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));
        assertFalse(NettyChannelInjector.hasMainThreadListener(listenerManager, Protocol.PLAY,
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE)));
    }

    @Test
    public void testRescheduledBundleKeepsNetworkMarker() throws Exception {
        ListenerManager listenerManager = createListenerManager(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer bundle = createBundle(
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE),
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT));
        SendAction action = new SendAction((Packet<?>) bundle.getHandle());

        NetworkMarker marker = new NetworkMarker(ConnectionSide.SERVER_SIDE, PacketType.Play.Server.BUNDLE);
        PacketPostListener postListener = mock(PacketPostListener.class);
        marker.addPostListener(postListener);

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

        ProtocolScheduler scheduler = mock(ProtocolScheduler.class);
        try (MockedStatic<ProtocolLibrary> protocolLibrary = mockStatic(ProtocolLibrary.class, CALLS_REAL_METHODS)) {
            protocolLibrary.when(ProtocolLibrary::getScheduler).thenReturn(scheduler);

            // a plugin sends the bundle with a marker off the main thread
            when(Bukkit.getServer().isPrimaryThread()).thenReturn(false);
            injector.sendClientboundPacket(action.packet, marker, true);
            assertNull(injector.processOutbound(action));

            ArgumentCaptor<Runnable> rescheduled = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTask(rescheduled.capture());
            verify(listenerManager, never()).invokeOutboundPacketListeners(any());

            // the rescheduled send runs on the main thread and is written again
            when(Bukkit.getServer().isPrimaryThread()).thenReturn(true);
            rescheduled.getValue().run();
            Runnable sent = injector.processOutbound(action);
            assertNotNull(sent);
            sent.run();
        } finally {
            when(Bukkit.getServer().isPrimaryThread()).thenReturn(true);
        }

        assertTrue(action.ran);
        verify(postListener).onPostEvent(any(PacketEvent.class));
    }

    private static final class FakeNetworkManager {

        private Channel channel;
    }

    private static final class SendAction implements Runnable {

        private final Packet<?> packet;
        private boolean ran;

        private SendAction(Packet<?> packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            this.ran = true;
        }
    }

    private static ListenerManager createListenerManager(PacketType mainThreadPacketType) {
        ListenerManager listenerManager = mock(ListenerManager.class);
        when(listenerManager.hasMainThreadListener(mainThreadPacketType)).thenReturn(true);
        return listenerManager;
    }

    private static PacketContainer createBundle(PacketContainer... packets) {
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(packets));
        return bundle;
    }
}
