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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.comphenix.protocol.injector.SortedPacketListenerList;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Represents a filter manager for asynchronous packets.
 * <p>
 * By using {@link AsyncMarker#incrementProcessingDelay()}, a packet can be delayed without having to block the
 * processing thread.
 * @author Kristian
 */
public class AsyncFilterManager implements AsynchronousManager {

	private SortedPacketListenerList serverTimeoutListeners;
	private SortedPacketListenerList clientTimeoutListeners;
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
	private final BukkitScheduler scheduler;
	
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
	public AsyncFilterManager(ErrorReporter reporter, BukkitScheduler scheduler) {
		// Initialize timeout listeners
		this.serverTimeoutListeners = new SortedPacketListenerList();
		this.clientTimeoutListeners = new SortedPacketListenerList();
		this.timeoutListeners = Sets.newSetFromMap(new ConcurrentHashMap<PacketListener, Boolean>());

		this.playerSendingHandler = new PlayerSendingHandler(reporter, serverTimeoutListeners, clientTimeoutListeners);
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
			serverTimeoutListeners.addListener(listener, sending);
		if (!ListeningWhitelist.isEmpty(receiving))
			serverTimeoutListeners.addListener(listener, receiving);
	}

	@Override
	public Set<PacketListener> getTimeoutHandlers() {
		return ImmutableSet.copyOf(timeoutListeners);
	}
	
	@Override
	public Set<PacketListener> getAsyncHandlers() {
		ImmutableSet.Builder<PacketListener> builder = ImmutableSet.builder();
		
		// Add every asynchronous packet listener
		for (PrioritizedListener<AsyncListenerHandler> handler : 
				Iterables.concat(serverProcessingQueue.values(), clientProcessingQueue.values())) {
			builder.add(handler.getListener().getAsyncListener());
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
			PacketFilterManager.verifyWhitelist(listener, sendingWhitelist);
			serverProcessingQueue.addListener(handler, sendingWhitelist);
		}
		if (hasValidWhitelist(receivingWhitelist)) {
			PacketFilterManager.verifyWhitelist(listener, receivingWhitelist);
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
		
		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();
		
		// Do it in the opposite order
		if (serverTimeoutListeners.removeListener(listener, sending).size() > 0 ||
			clientTimeoutListeners.removeListener(listener, receiving).size() > 0) {
			timeoutListeners.remove(listener);
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
			for (PrioritizedListener<AsyncListenerHandler> element : queue.getListener(type)) {
				if (element.getListener().getAsyncListener() == target) {
					return element.getListener();
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
		for (PrioritizedListener<AsyncListenerHandler> listener : processingQueue.values()) {			
			// Remove the listener
			if (Objects.equal(listener.getListener().getPlugin(), plugin)) {
				unregisterAsyncHandler(listener.getListener());
			}
		}
	}
	
	/**
	 * Enqueue a packet for asynchronous processing.
	 * @param syncPacket - synchronous packet event.
	 * @param asyncMarker - the asynchronous marker to use.
	 */
	public synchronized void enqueueSyncPacket(PacketEvent syncPacket, AsyncMarker asyncMarker) {
		PacketEvent newEvent = PacketEvent.fromSynchronous(syncPacket, asyncMarker);
		
		if (asyncMarker.isQueued() || asyncMarker.isTransmitted())
			throw new IllegalArgumentException("Cannot queue a packet that has already been queued.");
		
		asyncMarker.setQueuedSendingIndex(asyncMarker.getNewSendingIndex());
		
		// Start the process
		getSendingQueue(syncPacket).enqueue(newEvent);
		
		// We know this is occuring on the main thread, so pass TRUE
		getProcessingQueue(syncPacket).enqueue(newEvent, true);
	}
	
	@Override
	public Set<Integer> getSendingFilters() {
		return PacketRegistry.toLegacy(serverProcessingQueue.keySet());
	}
	
	@Override
	public Set<PacketType> getReceivingTypes() {
		return serverProcessingQueue.keySet();
	}
	
	@Override
	public Set<Integer> getReceivingFilters() {
		return PacketRegistry.toLegacy(clientProcessingQueue.keySet());
	}
	
	@Override
	public Set<PacketType> getSendingTypes() {
		return clientProcessingQueue.keySet();
	}
	
	/**
	 * Retrieve the current task scheduler.
	 * @return Current task scheduler.
	 */
	public BukkitScheduler getScheduler() {
		return scheduler;
	}
	
	@Override
	public boolean hasAsynchronousListeners(PacketEvent packet) {
		 Collection<?> list = getProcessingQueue(packet).getListener(packet.getPacketType());
		 return list != null && list.size() > 0;
	}
	
	/**
	 * Construct a asynchronous marker with all the default values.
	 * @return Asynchronous marker.
	 */
	public AsyncMarker createAsyncMarker() {
		return createAsyncMarker(AsyncMarker.DEFAULT_SENDING_DELTA, AsyncMarker.DEFAULT_TIMEOUT_DELTA);
	}
	
	/**
	 * Construct an async marker with the given sending priority delta and timeout delta.
	 * @param sendingDelta - how many packets we're willing to wait.
	 * @param timeoutDelta - how long (in ms) until the packet expire.
	 * @return An async marker.
	 */
	public AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta) {
		return createAsyncMarker(sendingDelta, timeoutDelta, 
								 currentSendingIndex.incrementAndGet(), System.currentTimeMillis());
	}
	
	// Helper method
	private AsyncMarker createAsyncMarker(long sendingDelta, long timeoutDelta, long sendingIndex, long currentTime) {
		return new AsyncMarker(manager, sendingIndex, sendingDelta, System.currentTimeMillis(), timeoutDelta);
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
		
		serverTimeoutListeners = null;
		clientTimeoutListeners = null;
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
	 * Signal that a packet has finished processing.
	 * @param packet - packet to signal.
	 */
	public void signalFreeProcessingSlot(PacketEvent packet) {
		getProcessingQueue(packet).signalProcessingDone();
	}
	
	/**
	 * Send any due packets, or clean up packets that have expired.
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
