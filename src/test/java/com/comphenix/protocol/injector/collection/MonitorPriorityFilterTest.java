package com.comphenix.protocol.injector.collection;

import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class MonitorPriorityFilterTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testOutboundMonitorListenerIsFilteredBySendingPriority() {
        AtomicInteger monitorCalls = new AtomicInteger();
        AtomicInteger normalCalls = new AtomicInteger();
        OutboundPacketListenerSet listenerSet = new OutboundPacketListenerSet(null, mock(ErrorReporter.class));
        listenerSet.addListener(new PacketAdapter(mock(Plugin.class), ListenerPriority.MONITOR,
                PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                monitorCalls.incrementAndGet();
            }
        });
        listenerSet.addListener(new PacketAdapter(mock(Plugin.class), ListenerPriority.NORMAL,
                PacketType.Play.Server.SYSTEM_CHAT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                normalCalls.incrementAndGet();
            }
        });

        // packets sent without filters only reach MONITOR listeners; an outbound only listener has an empty
        // receiving whitelist, which was used for the priority check before
        PacketEvent event = PacketEvent.fromServer(MonitorPriorityFilterTest.class,
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT), mock(Player.class));
        listenerSet.invoke(event, ListenerPriority.MONITOR);

        assertEquals(1, monitorCalls.get());
        assertEquals(0, normalCalls.get());
    }
}
