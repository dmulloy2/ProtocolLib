package com.comphenix.protocol.injector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.collection.OutboundPacketListenerSet;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.manager.NetworkManagerInjector;
import com.comphenix.protocol.scheduler.ProtocolScheduler;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UnfilteredBundleMonitorTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testMonitorListenerSeesPacketsInUnfilteredBundle() throws ReflectiveOperationException {
        AtomicInteger monitorCalls = new AtomicInteger();
        PacketFilterManager manager = createManager(monitorCalls, true);

        // packets sent without filters skip the injector's listener processing, so MONITOR listeners are only
        // invoked here
        manager.sendServerPacket(mock(Player.class), createBundle(), null, false);

        assertEquals(1, monitorCalls.get());
    }

    @Test
    public void testOffThreadBundleRunsSubPacketMonitorOnMainThread() throws ReflectiveOperationException {
        AtomicInteger monitorCalls = new AtomicInteger();
        PacketFilterManager manager = createManager(monitorCalls, false);
        ProtocolScheduler scheduler = mock(ProtocolScheduler.class);

        try (MockedStatic<ProtocolLibrary> protocolLibrary = mockStatic(ProtocolLibrary.class, CALLS_REAL_METHODS)) {
            protocolLibrary.when(ProtocolLibrary::getScheduler).thenReturn(scheduler);

            // a plugin sends the bundle off the main thread; the synchronous listener of a packet inside it must not
            // run on the sending thread
            manager.sendServerPacket(mock(Player.class), createBundle(), null, false);
            assertEquals(0, monitorCalls.get());

            ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).runTask(task.capture());
            task.getValue().run();
            assertEquals(1, monitorCalls.get());
        }
    }

    private static PacketFilterManager createManager(AtomicInteger monitorCalls, boolean primaryThread)
            throws ReflectiveOperationException {
        PacketTypeListenerSet mainThreadPacketTypes = new PacketTypeListenerSet();
        OutboundPacketListenerSet outboundListeners = new OutboundPacketListenerSet(mainThreadPacketTypes,
                mock(ErrorReporter.class));

        // a synchronous MONITOR listener for a packet type that is sent inside a bundle, nothing listens to the bundle
        outboundListeners.addListener(new PacketAdapter(mock(Plugin.class), ListenerPriority.MONITOR,
                PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                monitorCalls.incrementAndGet();
            }
        });

        Server server = mock(Server.class);
        when(server.isPrimaryThread()).thenReturn(primaryThread);
        NetworkManagerInjector networkManagerInjector = mock(NetworkManagerInjector.class);
        when(networkManagerInjector.getInjector(any())).thenReturn(mock(Injector.class));

        // only the fields used by sendServerPacket
        PacketFilterManager manager = mock(PacketFilterManager.class, CALLS_REAL_METHODS);
        setField(manager, "mainThreadPacketTypes", mainThreadPacketTypes);
        setField(manager, "outboundListeners", outboundListeners);
        setField(manager, "networkManagerInjector", networkManagerInjector);
        setField(manager, "server", server);
        return manager;
    }

    private static PacketContainer createBundle() {
        PacketContainer bundle = new PacketContainer(PacketType.Play.Server.BUNDLE);
        bundle.getPacketBundles().write(0, List.of(
                new PacketContainer(PacketType.Play.Server.KEEP_ALIVE),
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT)));
        return bundle;
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = PacketFilterManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
