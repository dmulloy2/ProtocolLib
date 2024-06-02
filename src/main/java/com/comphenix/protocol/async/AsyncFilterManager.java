/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.async;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.collection.InboundPacketListenerSet;
import com.comphenix.protocol.injector.collection.OutboundPacketListenerSet;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Represents a filter manager for asynchronous packets.
 * <p>
 * By using {@link AsyncMarker#incrementProcessingDelay()}, a packet can be delayed without having to block the
 * processing thread.
 * @author Kristian
 */
public class AsyncFilterManager implements AsynchronousManager {

    private OutboundPacketListenerSet outboundTimeoutListeners;
    private InboundPacketListenerSet inboundTimeoutListeners;
    private Set<PacketListener> timeoutListeners;
    
    private PacketProcessingQueue serverProcessingQueue;
    private PacketProcessingQueue clientProcessingQueue;

    // Sending queues
    private final PlayerSendingHandler playerSendingHandler;
    
    // Report exceptions
    private final ErrorReporter reporter;
    
    // The likely main thread
    private final Thread mainThread;
    
    // Default scheduler
    private final ProtocolScheduler scheduler;
    
    // Current packet index
    private final AtomicInteger currentSendingIndex = new AtomicInteger();
    
    // Our protocol manager
    private ProtocolManager manager;
    
    /**
     * Initialize a asynchronous filter manager.
     * <p>
     * <b>Internal method</b>. Retrieve the global asynchronous manager from the protocol manager instead.
     * @param reporter - desired error reporter.
     * @param scheduler - task scheduler.
     */
    public AsyncFilterManager(ErrorReporter reporter, ProtocolScheduler scheduler) {
        // Initialize timeout listeners
        this.outboundTimeoutListeners = new OutboundPacketListenerSet(null, reporter);
        this.inboundTimeoutListeners = new InboundPacketListenerSet(null, reporter);
        this.timeoutListeners = ConcurrentHashMap.newKeySet();

        this.playerSendingHandler = new PlayerSendingHandler(outboundTimeoutListeners, inboundTimeoutListeners);
        this.serverProcessingQueue = new PacketProcessingQueue(playerSendingHandler);
        this.clientProcessingQueue = new PacketProcessingQueue(playerSendingHandler);
        this.playerSendingHandler.initializeScheduler();
        
        this.scheduler = scheduler;
        this.reporter = reporter;
        this.mainThread = Thread.currentThread();
    }
    
    /**
     * Retrieve the protocol manager.
     * @return The protocol manager.
     */
    public ProtocolManager getManager() {
        return manager;
    }
    
    /**
     * Set the associated protocol manager.
     * @param manager - the new manager.
     */
    public void setManager(ProtocolManager manager) {
        this.manager = manager;
    }
    
    @Override
    public AsyncListenerHandler registerAsyncHandler(PacketListener listener) {
        return registerAsyncHandler(listener, true);
    }
    
    @Override
    public void registerTimeoutHandler(PacketListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener cannot be NULL.");
        if (!timeoutListeners.add(listener))
            return;
        
        ListeningWhitelist sending = listener.getSendingWhitelist();
        ListeningWhitelist receiving = listener.getReceivingWhitelist();
        
        if (!ListeningWhitelist.isEmpty(sending))
            outboundTimeoutListeners.addListener(listener);
        if (!ListeningWhitelist.isEmpty(receiving))
            inboundTimeoutListeners.addListener(listener);
    }

    @Override
    public Set<PacketListener> getTimeoutHandlers() {
        return ImmutableSet.copyOf(timeoutListeners);
    }
    
    @Override
    public Set<PacketListener> getAsyncHandlers() {
        ImmutableSet.Builder<PacketListener> builder = ImmutableSet.builder();
        
        // Add every asynchronous packet listener
        for (AsyncListenerHandler handler : Iterables.concat(serverProcessingQueue.values(), clientProcessingQueue.values())) {
            builder.add(handler.getAsyncListener());
        }
        return builder.build();
    }
    
    /**
     * Registers an asynchronous packet handler.
     * <p>
     * Use {@link AsyncMarker#incrementProcessingDelay()} to delay a packet until its ready to be transmitted.
     * <p>
     * To start listening asynchronously, pass the getListenerLoop() runnable to a different thread.
     * <p>
     * Asynchronous events will only be executed if a synchronous listener with the same packets is registered.
     * If you already have a synchronous event, call this method with autoInject set to FALSE.
     * 
     * @param listener - the packet listener that will receive these asynchronous events.
     * @param autoInject - whether or not to automatically create the corresponding synchronous listener,
     * @return An asynchronous handler.
     */
    public AsyncListenerHandler registerAsyncHandler(PacketListener listener, boolean autoInject) {
        AsyncListenerHandler handler = new AsyncListenerHandler(mainThread, this, listener);
        
        ListeningWhitelist sendingWhitelist = listener.getSendingWhitelist();
        ListeningWhitelist receivingWhitelist = listener.getReceivingWhitelist();
        
        if (!hasValidWhitelist(sendingWhitelist) && !hasValidWhitelist(receivingWhitelist)) {
            throw new IllegalArgumentException("Listener has an empty sending and receiving whitelist.");
        }
        
        // Add listener to either or both processing queue
        if (hasValidWhitelist(sendingWhitelist)) {
            manager.verifyWhitelist(listener, sendingWhitelist);
            serverProcessingQueue.addListener(handler, sendingWhitelist);
        }
        if (hasValidWhitelist(receivingWhitelist)) {
            manager.verifyWhitelist(listener, receivingWhitelist);
            clientProcessingQueue.addListener(handler, receivingWhitelist);
        }
        
        // We need a synchronized listener to get the ball rolling
        if (autoInject) {
            handler.setNullPacketListener(new NullPacketListener(listener));
            manager.addPacketListener(handler.getNullPacketListener());
        }
        return handler;
    }
    
    private boolean hasValidWhitelist(ListeningWhitelist whitelist) {
        return whitelist != null && whitelist.getTypes().size() > 0;
    }
    
    @Override
    public void unregisterTimeoutHandler(PacketListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener cannot be NULL.");
        
        if (timeoutListeners.remove(listener)) {
            outboundTimeoutListeners.removeListener(listener);
            inboundTimeoutListeners.removeListener(listener);
        }
    }
    
    @Override
    public void unregisterAsyncHandler(PacketListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("listener cannot be NULL.");
        
        AsyncListenerHandler handler =
                      findHandler(serverProcessingQueue, listener.getSendingWhitelist(), listener);
        
        if (handler == null) {
            handler = findHandler(clientProcessingQueue, listener.getReceivingWhitelist(), listener);
        }
        unregisterAsyncHandler(handler);
    }
    
    // Search for the first correct handler
    private AsyncListenerHandler findHandler(PacketProcessingQueue queue, ListeningWhitelist search, PacketListener target) {
        if (ListeningWhitelist.isEmpty(search))
            return null;
        
        for (PacketType type : search.getTypes()) {
            for (AsyncListenerHandler element : queue.get(type)) {
                if (element.getAsyncListener() == target) {
                    return element;
                }
            }
        }
        return null;
    }
    
    @Override
    public void unregisterAsyncHandler(AsyncListenerHandler handler) {
        if (handler == null)
            throw new IllegalArgumentException("listenerToken cannot be NULL");
        
        handler.cancel();
    }
    
    // Called by AsyncListenerHandler
    void unregisterAsyncHandlerInternal(AsyncListenerHandler handler) {
        
        PacketListener listener = handler.getAsyncListener();
        boolean synchronusOK = onMainThread();
        
        // Unregister null packet listeners
        if (handler.getNullPacketListener() != null) {
            manager.removePacketListener(handler.getNullPacketListener());
        }
        
        // Just remove it from the queue(s)
        if (hasValidWhitelist(listener.getSendingWhitelist())) {
            List<PacketType> removed = serverProcessingQueue.removeListener(handler, listener.getSendingWhitelist());
            
            // We're already taking care of this, so don't do anything
            playerSendingHandler.sendServerPackets(removed, synchronusOK);
        }
        
        if (hasValidWhitelist(listener.getReceivingWhitelist())) {
            List<PacketType> removed = clientProcessingQueue.removeListener(handler, listener.getReceivingWhitelist());
            playerSendingHandler.sendClientPackets(removed, synchronusOK);
        }
    }
    
    /**
     * Determine if we're running on the main thread.
     * @return TRUE if we are, FALSE otherwise.
     */
    private boolean onMainThread() {
        return Thread.currentThread().getId() == mainThread.getId();
    }
    
    @Override
    public void unregisterAsyncHandlers(Plugin plugin) {
        unregisterAsyncHandlers(serverProcessingQueue, plugin);
        unregisterAsyncHandlers(clientProcessingQueue, plugin);
    }
    
    private void unregisterAsyncHandlers(PacketProcessingQueue processingQueue, Plugin plugin) {
        
        // Iterate through every packet listener
        for (AsyncListenerHandler listener : processingQueue.values()) {
            // Remove the listener
            if (Objects.equal(listener.getPlugin(), plugin)) {
                unregisterAsyncHandler(listener);
            }
        }
    }
    
    /**
     * Enqueue a packet for asynchronous processing.
     * 
     * @param syncPacket - synchronous packet event.
     * @param asyncMarker - the asynchronous marker to use.
     */
    public synchronized void enqueueSyncPacket(PacketEvent syncPacket, AsyncMarker asyncMarker) {
        PacketEvent newEvent = PacketEvent.fromSynchronous(syncPacket, asyncMarker);

        if (asyncMarker.isQueued() || asyncMarker.isTransmitted())
            throw new IllegalArgumentException("Cannot queue a packet that has already been queued.");

        asyncMarker.setQueuedSendingIndex(asyncMarker.getNewSendingIndex());

        // The player is only be null when they're logged out,
        // so this should be a pretty safe check
        Player player = syncPacket.getPlayer();
        if (player != null) {
            // Start the process
            getSendingQueue(syncPacket).enqueue(newEvent);

            // We know this is occurring on the main thread, so pass TRUE
            getProcessingQueue(syncPacket).enqueue(newEvent, true);
        }
    }

    @Override
    public Set<PacketType> getReceivingTypes() {
        return serverProcessingQueue.keySet();
    }

    @Override
    public Set<PacketType> getSendingTypes() {
        return clientProcessingQueue.keySet();
    }
    
    /**
     * Retrieve the current task scheduler.
     * @return Current task scheduler.
     */
    public ProtocolScheduler getScheduler() {
        return scheduler;
    }
    
    @Override
    public boolean hasAsynchronousListeners(PacketEvent packet) {
         return getProcessingQueue(packet).contains(packet.getPacketType());
    }
    
    /**
     * Construct a asynchronous marker with all the default values.
     * @return Asynchronous marker.
     */
    public AsyncMarker createAsyncMarker() {
        return createAsyncMarker(AsyncMarker.DEFAULT_TIMEOUT_DELTA);
    }
    
    /**
     * Construct an async marker with the given sending priority delta and timeout delta.
     * @param timeoutDelta - how long (in ms) until the packet expire.
     * @return An async marker.
     */
    public AsyncMarker createAsyncMarker(long timeoutDelta) {
        return createAsyncMarker(timeoutDelta, currentSendingIndex.incrementAndGet());
    }
    
    // Helper method
    private AsyncMarker createAsyncMarker(long timeoutDelta, long sendingIndex) {
        return new AsyncMarker(manager, sendingIndex, System.currentTimeMillis(), timeoutDelta);
    }
    
    @Override
    public PacketStream getPacketStream() {
        return manager;
    }

    @Override
    public ErrorReporter getErrorReporter() {
        return reporter;
    }
    
    @Override
    public void cleanupAll() {
        serverProcessingQueue.cleanupAll();
        playerSendingHandler.cleanupAll();
        timeoutListeners.clear();
        
        outboundTimeoutListeners = null;
        inboundTimeoutListeners = null;
    }
    
    @Override
    public void signalPacketTransmission(PacketEvent packet) {
        signalPacketTransmission(packet, onMainThread());
    }

    /**
     * Signal that a packet is ready to be transmitted.
     * @param packet - packet to signal.
     * @param onMainThread - whether or not this method was run by the main thread.
     */
    private void signalPacketTransmission(PacketEvent packet, boolean onMainThread) {
        AsyncMarker marker = packet.getAsyncMarker();
        if (marker == null)
            throw new IllegalArgumentException(
                    "A sync packet cannot be transmitted by the asynchronous manager.");
        if (!marker.isQueued())
            throw new IllegalArgumentException(
                    "A packet must have been queued before it can be transmitted.");
        
        // Only send if the packet is ready
        if (marker.decrementProcessingDelay() == 0) {

            // Now, get the next non-cancelled listener
            if (!marker.hasExpired()) {
                for (; marker.getListenerTraversal().hasNext(); ) {
                    AsyncListenerHandler handler = marker.getListenerTraversal().next();
                    
                    if (!handler.isCancelled()) {
                    	marker.incrementProcessingDelay();
                    	handler.enqueuePacket(packet);
                        return;
                    }
                }
            }
            
            // There are no more listeners - queue the packet for transmission
            signalFreeProcessingSlot(packet, onMainThread);

            PacketSendingQueue queue = getSendingQueue(packet, false);
            
            // No need to create a new queue if the player has logged out
            if (queue != null)
                queue.signalPacketUpdate(packet, onMainThread);
        }
    }
    
    /**
     * Retrieve the sending queue this packet belongs to.
     * @param packet - the packet.
     * @return The server or client sending queue the packet belongs to.
     */
    public PacketSendingQueue getSendingQueue(PacketEvent packet) {
        return playerSendingHandler.getSendingQueue(packet);
    }
    
    /**
     * Retrieve the sending queue this packet belongs to.
     * @param packet - the packet.
     * @param createNew - if TRUE, create a new queue if it hasn't already been created.
     * @return The server or client sending queue the packet belongs to.
     */
    public PacketSendingQueue getSendingQueue(PacketEvent packet, boolean createNew) {
        return playerSendingHandler.getSendingQueue(packet, createNew);
    }
    
    /**
     * Retrieve the processing queue this packet belongs to.
     * @param packet - the packet.
     * @return The server or client sending processing the packet belongs to.
     */
    public PacketProcessingQueue getProcessingQueue(PacketEvent packet) {
        return packet.isServerPacket() ? serverProcessingQueue : clientProcessingQueue;
    }
    
    /**
     * Signal that a packet has finished processing. Tries to process further packets
     * if a processing slot is still free.
     * @param packet - packet to signal.
     * @param onMainThread whether or not this method was run by the main thread.
     */
    public void signalFreeProcessingSlot(PacketEvent packet, boolean onMainThread) {
    	PacketProcessingQueue queue = getProcessingQueue(packet);
    	// mark slot as done
    	queue.signalProcessingDone();
    	
    	// start processing next slot if possible
    	queue.signalBeginProcessing(onMainThread);
    }
    
    /**
     * Send any due packets, or clean up packets that have expired.
     * @param tickCounter Tick counter
     * @param onMainThread Whether or not to execute on the main thread
     */
    public void sendProcessedPackets(int tickCounter, boolean onMainThread) {
        // The server queue is unlikely to need checking that often
        if (tickCounter % 10 == 0) {
            playerSendingHandler.trySendServerPackets(onMainThread);
        }
        
        playerSendingHandler.trySendClientPackets(onMainThread);
    }

    /**
     * Clean up after a given player has logged out.
     * @param player - the player that has just logged out.
     */
    public void removePlayer(Player player) {
        playerSendingHandler.removePlayer(player);
    }
}
