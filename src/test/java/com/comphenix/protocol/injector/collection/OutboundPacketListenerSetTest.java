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
import com.comphenix.protocol.wrappers.Converters;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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

    @Test
    public void testUnchangedBundleIsNotCopied() {
        OutboundPacketListenerSet listenerSet = createListenerSet(event -> { });
        PacketEvent bundleEvent = createBundleEvent();
        PacketContainer bundle = bundleEvent.getPacket();
        Object packets = getRawPackets(bundle);

        listenerSet.invoke(bundleEvent);

        assertSame(bundle, bundleEvent.getPacket());
        assertSame(packets, getRawPackets(bundleEvent.getPacket()));
    }

    @Test
    public void testCancelledSubPacketDoesNotModifySharedBundle() {
        PacketContainer cancelled = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer retained = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer sharedBundle = createBundle(cancelled, retained);
        PacketEvent modifiedEvent = createBundleEvent(sharedBundle);
        PacketEvent sharedEvent = createBundleEvent(PacketContainer.fromPacket(sharedBundle.getHandle()));
        Object sharedPackets = getRawPackets(sharedBundle);

        OutboundPacketListenerSet listenerSet = createListenerSet(event -> {
            if (event.getPacket().getHandle() == cancelled.getHandle()) {
                event.setCancelled(true);
            }
        });
        listenerSet.invoke(modifiedEvent);

        assertNotSame(sharedBundle.getHandle(), modifiedEvent.getPacket().getHandle());
        assertSame(sharedBundle.getHandle(), sharedEvent.getPacket().getHandle());
        assertSame(sharedPackets, getRawPackets(sharedEvent.getPacket()));
        assertPacketHandles(modifiedEvent.getPacket(), retained);
        assertPacketHandles(sharedEvent.getPacket(), cancelled, retained);
    }

    @Test
    public void testReplacedSubPacketDoesNotModifySharedBundle() {
        PacketContainer replaced = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer retained = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer replacement = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        PacketContainer sharedBundle = createBundle(replaced, retained);
        PacketEvent modifiedEvent = createBundleEvent(sharedBundle);
        PacketEvent sharedEvent = createBundleEvent(PacketContainer.fromPacket(sharedBundle.getHandle()));
        Object sharedPackets = getRawPackets(sharedBundle);

        OutboundPacketListenerSet listenerSet = createListenerSet(event -> {
            if (event.getPacket().getHandle() == replaced.getHandle()) {
                event.setPacket(replacement);
            }
        });
        listenerSet.invoke(modifiedEvent);

        assertNotSame(sharedBundle.getHandle(), modifiedEvent.getPacket().getHandle());
        assertSame(sharedBundle.getHandle(), sharedEvent.getPacket().getHandle());
        assertSame(sharedPackets, getRawPackets(sharedEvent.getPacket()));
        assertPacketHandles(modifiedEvent.getPacket(), replacement, retained);
        assertPacketHandles(sharedEvent.getPacket(), replaced, retained);
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
        return createBundleEvent(createBundle(new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));
    }

    private static PacketEvent createBundleEvent(PacketContainer bundle) {
        return PacketEvent.fromServer(OutboundPacketListenerSetTest.class, bundle, mock(Player.class));
    }

    private static PacketContainer createBundle(PacketContainer... packets) {
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(packets));
        return bundle;
    }

    private static Object getRawPackets(PacketContainer bundle) {
        return bundle.getModifier().withType(Iterable.class).read(0);
    }

    private static void assertPacketHandles(PacketContainer bundle, PacketContainer... expected) {
        List<PacketContainer> packets = Converters.toList(bundle.getPacketBundles().read(0));
        assertEquals(expected.length, packets.size());
        for (int index = 0; index < expected.length; index++) {
            assertSame(expected[index].getHandle(), packets.get(index).getHandle());
        }
    }
}
