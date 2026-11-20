package com.comphenix.protocol.async;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.comphenix.protocol.scheduler.Task;

import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AsyncListenerHandlerTest {

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testFullQueueRejectsPacketsWithoutThrowing() {
        AsyncFilterManager manager = createManager(mock(ProtocolScheduler.class));
        AsyncListenerHandler handler = manager.registerAsyncHandler(
                new PacketAdapter(mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) { }, false);

        PacketEvent event = mock(PacketEvent.class);
        for (int i = 0; i < 1024; i++) {
            assertTrue(handler.offerPacket(event));
        }

        // a full queue used to throw from inside the processing loop, losing the packet and its processing slot
        assertFalse(handler.offerPacket(event));
        assertThrows(IllegalStateException.class, () -> handler.enqueuePacket(event));
    }

    @Test
    void testSyncStartHonoursTimeBudget() {
        ProtocolScheduler scheduler = mock(ProtocolScheduler.class);
        when(scheduler.scheduleSyncRepeatingTask(any(), anyLong(), anyLong())).thenReturn(mock(Task.class));
        AsyncFilterManager manager = createManager(scheduler);

        AtomicInteger processed = new AtomicInteger();
        AsyncListenerHandler handler = manager.registerAsyncHandler(
                new PacketAdapter(mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        processed.incrementAndGet();
                        sleep(5);
                    }
                }, false);

        assertTrue(handler.syncStart(1, TimeUnit.MILLISECONDS));
        ArgumentCaptor<Runnable> tick = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).scheduleSyncRepeatingTask(tick.capture(), anyLong(), anyLong());

        for (int i = 0; i < 10; i++) {
            assertTrue(handler.offerPacket(createQueuedEvent()));
        }

        // every packet takes 5 ms, so a 1 ms budget allows a single packet per tick; the inverted check used to drain
        // the whole queue on the main thread
        tick.getValue().run();
        assertEquals(1, processed.get());

        tick.getValue().run();
        assertEquals(2, processed.get());
    }

    @Test
    void testCleanupCancelsInboundAndOutboundHandlers() {
        AsyncFilterManager manager = createManager(mock(ProtocolScheduler.class));
        AsyncListenerHandler outbound = manager.registerAsyncHandler(
                new PacketAdapter(mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) { }, false);
        AsyncListenerHandler inbound = manager.registerAsyncHandler(
                new PacketAdapter(mock(Plugin.class), PacketType.Play.Client.CHAT) { }, false);

        manager.cleanupAll();

        assertTrue(outbound.isCancelled());
        assertTrue(inbound.isCancelled());
    }

    private static AsyncFilterManager createManager(ProtocolScheduler scheduler) {
        AsyncFilterManager manager = new AsyncFilterManager(mock(ErrorReporter.class), scheduler);
        manager.setManager(mock(ProtocolManager.class));
        return manager;
    }

    private static PacketEvent createQueuedEvent() {
        AsyncMarker marker = mock(AsyncMarker.class);
        when(marker.getProcessingLock()).thenReturn(new Object());
        when(marker.isQueued()).thenReturn(true);
        // another listener still has to process the packet, so the transmission stops there
        when(marker.decrementProcessingDelay()).thenReturn(1);

        PacketEvent event = mock(PacketEvent.class);
        when(event.getAsyncMarker()).thenReturn(marker);
        when(event.isServerPacket()).thenReturn(true);
        when(event.getPacketType()).thenReturn(PacketType.Play.Server.SYSTEM_CHAT);
        return event;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
