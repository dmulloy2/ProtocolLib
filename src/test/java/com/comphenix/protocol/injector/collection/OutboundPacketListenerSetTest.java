package com.comphenix.protocol.injector.collection;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class OutboundPacketListenerSetTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testCancelledSubPacketsCancelBundle() {
        OutboundPacketListenerSet listenerSet = createListenerSet(event -> event.setCancelled(true));
        PacketEvent bundleEvent = createBundleEvent();

        listenerSet.invoke(bundleEvent);

        assertTrue(bundleEvent.isCancelled());
    }

    @Test
    public void testSubPacketEventReferencesBundle() {
        AtomicReference<PacketEvent> subPacketEvent = new AtomicReference<>();
        OutboundPacketListenerSet listenerSet = createListenerSet(subPacketEvent::set);
        PacketEvent bundleEvent = createBundleEvent();

        listenerSet.invoke(bundleEvent);

        assertSame(bundleEvent, subPacketEvent.get().getBundle());
    }

    private static OutboundPacketListenerSet createListenerSet(Consumer<PacketEvent> callback) {
        OutboundPacketListenerSet listenerSet = new OutboundPacketListenerSet(null, mock(ErrorReporter.class));
        listenerSet.addListener(new PacketAdapter(mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                callback.accept(event);
            }
        });
        return listenerSet;
    }

    private static PacketEvent createBundleEvent() {
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));
        return PacketEvent.fromServer(OutboundPacketListenerSetTest.class, bundle, mock(Player.class));
    }
}
