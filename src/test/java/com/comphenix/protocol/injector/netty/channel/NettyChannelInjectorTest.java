package com.comphenix.protocol.injector.netty.channel;

import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.ListenerManager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
