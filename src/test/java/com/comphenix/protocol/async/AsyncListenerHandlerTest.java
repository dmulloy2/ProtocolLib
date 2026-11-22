package com.comphenix.protocol.async;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.comphenix.protocol.scheduler.Task;

import org.bukkit.entity.Player;
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

    @Test
    void testCancelledHandlerRejectsPackets() {
        AsyncFilterManager manager = createManager(mock(ProtocolScheduler.class));
        AsyncListenerHandler handler = manager.registerAsyncHandler(
                new PacketAdapter(mock(Plugin.class), PacketType.Play.Server.SYSTEM_CHAT) { }, false);

        handler.cancel();

        // the workers of a cancelled handler are gone, a packet queued there would never be processed
        assertFalse(handler.offerPacket(createQueuedEvent()));
    }

    @Test
    void testUnregisteredHandlerPassesQueuedPacketsOn() throws Exception {
        AsyncFilterManager manager = createManager(mock(ProtocolScheduler.class));
        Plugin disabled = mock(Plugin.class);
        Plugin remaining = mock(Plugin.class);
        AsyncListenerHandler first = manager.registerAsyncHandler(
                new PacketAdapter(disabled, PacketType.Play.Server.SYSTEM_CHAT) { }, false);
        AsyncListenerHandler second = manager.registerAsyncHandler(
                new PacketAdapter(remaining, PacketType.Play.Server.SYSTEM_CHAT) { }, false);

        Player player = mock(Player.class);
        // the sending queues are keyed by address
        when(player.getAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 25565));
        PacketEvent syncEvent = PacketEvent.fromServer(this,
                new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT), player);
        manager.enqueueSyncPacket(syncEvent, manager.createAsyncMarker(mock(Injector.class)));

        // no worker has been started, so the packet waits in the queue of the first handler and holds a slot
        int slots = availableSlots(manager);
        assertEquals(1, queueSize(first));
        assertEquals(0, queueSize(second));

        // what disabling the plugin of the first handler does
        manager.unregisterAsyncHandlers(disabled);
        assertEquals(1, queueSize(second), "the queued packet was not passed on to the next handler");
        assertEquals(slots, availableSlots(manager));

        manager.unregisterAsyncHandlers(remaining);
        assertEquals(slots + 1, availableSlots(manager), "the processing slot of the packet was not released");
    }

    private static int availableSlots(AsyncFilterManager manager) throws ReflectiveOperationException {
        Field queueField = AsyncFilterManager.class.getDeclaredField("serverProcessingQueue");
        queueField.setAccessible(true);
        Field slotsField = PacketProcessingQueue.class.getDeclaredField("concurrentProcessing");
        slotsField.setAccessible(true);
        return ((Semaphore) slotsField.get(queueField.get(manager))).availablePermits();
    }

    private static int queueSize(AsyncListenerHandler handler) throws ReflectiveOperationException {
        Field queueField = AsyncListenerHandler.class.getDeclaredField("queuedPackets");
        queueField.setAccessible(true);
        return ((Collection<?>) queueField.get(handler)).size();
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
