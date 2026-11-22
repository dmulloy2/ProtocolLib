package com.comphenix.protocol.async;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
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
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Asynchronous processing with real worker threads: listeners that are slow or delay packets and finish them from
 * another thread, with and without handlers being cancelled in the middle of the traffic.
 */
class AsyncProcessingStressTest {

    private static final int PACKETS = 3000;
    private static final int PLAYERS = 4;

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @RepeatedTest(10)
    void testEveryPacketIsSentOnceWhileHandlersAreCancelled() throws Exception {
        runTraffic(true, true, PLAYERS);
    }

    @RepeatedTest(10)
    void testEveryPacketIsSentOnce() throws Exception {
        runTraffic(false, true, PLAYERS);
    }

    private void runTraffic(boolean cancelHandlers, boolean delaySome, int playerCount) throws Exception {
        ExecutorService workers = Executors.newCachedThreadPool();
        ScheduledExecutorService delayed = Executors.newScheduledThreadPool(2);
        try {
            ProtocolScheduler scheduler = mock(ProtocolScheduler.class);
            when(scheduler.runTaskAsync(any())).thenAnswer(invocation -> {
                workers.execute(invocation.getArgument(0));
                return mock(Task.class);
            });
            AsyncFilterManager manager = new AsyncFilterManager(mock(ErrorReporter.class), scheduler);
            manager.setManager(mock(ProtocolManager.class));

            AtomicInteger processed = new AtomicInteger();
            Plugin slowPlugin = mock(Plugin.class);
            Plugin delayingPlugin = mock(Plugin.class);
            Plugin lastPlugin = mock(Plugin.class);

            // slow: occasionally busy, so packets are in flight while it is cancelled
            AsyncListenerHandler slow = manager.registerAsyncHandler(new PacketAdapter(slowPlugin, PacketType.Play.Server.SYSTEM_CHAT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    processed.incrementAndGet();
                    if (ThreadLocalRandom.current().nextInt(20) == 0) {
                        sleep(1);
                    }
                }
            }, false);
            // delays some packets and finishes them later from another thread, like a plugin waiting for a database
            AsyncListenerHandler delaying = manager.registerAsyncHandler(new PacketAdapter(delayingPlugin, PacketType.Play.Server.SYSTEM_CHAT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    processed.incrementAndGet();
                    if (delaySome && ThreadLocalRandom.current().nextInt(5) == 0) {
                        event.getAsyncMarker().incrementProcessingDelay();
                        delayed.schedule(() -> manager.signalPacketTransmission(event),
                                ThreadLocalRandom.current().nextInt(3), TimeUnit.MILLISECONDS);
                    }
                }
            }, false);
            AsyncListenerHandler last = manager.registerAsyncHandler(new PacketAdapter(lastPlugin, PacketType.Play.Server.SYSTEM_CHAT) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    processed.incrementAndGet();
                }
            }, false);
            slow.start(2);
            delaying.start(2);
            last.start(2);

            // every transmitted packet, counted per packet instance (packets with the same content are equal)
            Map<Object, AtomicInteger> sent = Collections.synchronizedMap(new IdentityHashMap<>());
            Injector injector = mock(Injector.class);
            doAnswer(invocation -> {
                synchronized (sent) {
                    sent.computeIfAbsent(invocation.getArgument(0), key -> new AtomicInteger()).incrementAndGet();
                }
                return null;
            }).when(injector).sendClientboundPacket(any(), any(), anyBoolean());

            List<Player> players = new ArrayList<>();
            for (int i = 0; i < playerCount; i++) {
                Player player = mock(Player.class);
                when(player.getAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 30000 + i));
                when(player.isOnline()).thenReturn(true);
                players.add(player);
            }

            int slots = availableSlots(manager);
            List<AsyncMarker> markers = new ArrayList<>();
            int tick = 0;
            for (int i = 0; i < PACKETS; i++) {
                // this test thread is the main thread of the manager
                AsyncMarker marker = manager.createAsyncMarker(injector);
                markers.add(marker);
                manager.enqueueSyncPacket(PacketEvent.fromServer(this,
                        new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT), players.get(i % playerCount)), marker);

                // plugins are disabled in the middle of the traffic
                if (cancelHandlers && i == PACKETS / 3) {
                    manager.unregisterAsyncHandlers(slowPlugin);
                } else if (cancelHandlers && i == 2 * PACKETS / 3) {
                    manager.unregisterAsyncHandlers(delayingPlugin);
                }
                if (i % 50 == 0) {
                    manager.sendProcessedPackets(tick++, true);
                }
            }

            // main thread heartbeats until everything is sent
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
            while (sent.size() < PACKETS && System.nanoTime() < deadline) {
                manager.sendProcessedPackets(tick++, true);
                sleep(2);
            }

            if (sent.size() < PACKETS) {
                Reference.reachabilityFence(players);
                fail(describeStall(manager, markers, sent.size(), slots, slow, delaying, last));
            }
            synchronized (sent) {
                for (Map.Entry<Object, AtomicInteger> entry : sent.entrySet()) {
                    if (entry.getValue().get() != 1) {
                        fail("a packet was sent " + entry.getValue().get() + " times");
                    }
                }
            }
            for (AsyncMarker marker : markers) {
                assertEquals(0, marker.getProcessingDelay());
                assertTrue(marker.isTransmitted());
            }
            assertEquals(slots, availableSlots(manager), "processing slots were not released");
            assertTrue(processed.get() >= PACKETS, "every packet reaches at least one listener");

            manager.cleanupAll();
            // packet events and the sending queues only keep weak references to players; on a server the online
            // players are referenced by the server, here this test has to keep them reachable
            Reference.reachabilityFence(players);
        } finally {
            workers.shutdownNow();
            delayed.shutdownNow();
        }
    }

    private static String describeStall(AsyncFilterManager manager, List<AsyncMarker> markers, int sent, int slots,
            AsyncListenerHandler... handlers) throws ReflectiveOperationException {
        int delayed = 0, processedNotSent = 0, notProcessed = 0;
        Map<Integer, Integer> delays = new TreeMap<>();
        for (AsyncMarker marker : markers) {
            if (marker.isTransmitted()) {
                continue;
            }
            delays.merge(marker.getProcessingDelay(), 1, Integer::sum);
            if (marker.getProcessingDelay() > 0) {
                delayed++;
            }
            if (marker.isProcessed()) {
                processedNotSent++;
            } else {
                notProcessed++;
            }
        }
        StringBuilder queues = new StringBuilder();
        for (AsyncListenerHandler handler : handlers) {
            queues.append(" handler(cancelled=").append(handler.isCancelled()).append(", queued=")
                    .append(queueSize(handler)).append(", workers=").append(handler.getWorkers()).append(')');
        }
        // where are the packets that were not sent?
        Set<AsyncMarker> stuck = Collections.newSetFromMap(new IdentityHashMap<>());
        for (AsyncMarker marker : markers) {
            if (!marker.isTransmitted()) stuck.add(marker);
        }
        Field handlerField = AsyncFilterManager.class.getDeclaredField("playerSendingHandler");
        handlerField.setAccessible(true);
        Object sendingHandler = handlerField.get(manager);
        Field queuesField = PlayerSendingHandler.class.getDeclaredField("playerSendingQueues");
        queuesField.setAccessible(true);
        Field queueField = PacketSendingQueue.class.getDeclaredField("sendingQueue");
        queueField.setAccessible(true);
        StringBuilder sending = new StringBuilder();
        int inSendingQueues = 0;
        for (Object container : ((Map<?, ?>) queuesField.get(sendingHandler)).values()) {
            Field outbound = container.getClass().getDeclaredField("outboundQueue");
            outbound.setAccessible(true);
            Collection<?> holders = (Collection<?>) queueField.get(outbound.get(container));
            int stuckHere = 0;
            for (Object holder : holders) {
                PacketEvent event = ((PacketEventHolder) holder).getEvent();
                if (stuck.contains(event.getAsyncMarker())) stuckHere++;
            }
            inSendingQueues += stuckHere;
            sending.append(" player(queued=").append(holders.size()).append(", stuck=").append(stuckHere).append(')');
        }
        return "stalled: sent " + sent + "/" + PACKETS + "; stuck in sending queues " + inSendingQueues + ";" + sending + ";"
                + " sending queues " + ((Map<?, ?>) queuesField.get(sendingHandler)).size() + "; not sent: delay>0=" + delayed + ", processed=" + processedNotSent
                + ", not processed=" + notProcessed + "; slots " + availableSlots(manager) + "/" + slots
                + "; processing queue " + processingQueue(manager).size() + "; delays " + delays + ";" + queues;
    }

    private static PacketProcessingQueue processingQueue(AsyncFilterManager manager) throws ReflectiveOperationException {
        Field queueField = AsyncFilterManager.class.getDeclaredField("serverProcessingQueue");
        queueField.setAccessible(true);
        return (PacketProcessingQueue) queueField.get(manager);
    }

    private static int availableSlots(AsyncFilterManager manager) throws ReflectiveOperationException {
        Field slotsField = PacketProcessingQueue.class.getDeclaredField("concurrentProcessing");
        slotsField.setAccessible(true);
        return ((Semaphore) slotsField.get(processingQueue(manager))).availablePermits();
    }

    private static int queueSize(AsyncListenerHandler handler) throws ReflectiveOperationException {
        Field queueField = AsyncListenerHandler.class.getDeclaredField("queuedPackets");
        queueField.setAccessible(true);
        return ((Collection<?>) queueField.get(handler)).size();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
