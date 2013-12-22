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
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.google.common.collect.MinMaxPriorityQueue;


/**
 * Handles the processing of every packet type.
 * 
 * @author Kristian
 */
class PacketProcessingQueue extends AbstractConcurrentListenerMultimap<AsyncListenerHandler> {
	public static final ReportType REPORT_GUAVA_CORRUPT_MISSING = 
			new ReportType("Guava is either missing or corrupt. Reverting to PriorityQueue.");

	// Initial number of elements
	public static final int INITIAL_CAPACITY = 64;
	
	/**
	 * Default maximum number of packets to process concurrently.
	 */
	public static final int DEFAULT_MAXIMUM_CONCURRENCY = 32;
	
	/**
	 * Default maximum number of packets to queue for processing.
	 */
	public static final int DEFAULT_QUEUE_LIMIT = 1024 * 60;
	
	/**
	 * Number of packets we're processing concurrently.
	 */
	private final int maximumConcurrency;
	private Semaphore concurrentProcessing;
	
	// Queued packets for being processed
	private Queue<PacketEventHolder> processingQueue;
	
	// Packets for sending
	private PlayerSendingHandler sendingHandler;
	
	public PacketProcessingQueue(PlayerSendingHandler sendingHandler) {
		this(sendingHandler, INITIAL_CAPACITY, DEFAULT_QUEUE_LIMIT, DEFAULT_MAXIMUM_CONCURRENCY);
	}
	
	public PacketProcessingQueue(PlayerSendingHandler sendingHandler, int initialSize, int maximumSize, int maximumConcurrency) {
		super();

		try {
			this.processingQueue = Synchronization.queue(MinMaxPriorityQueue.
					expectedSize(initialSize).
					maximumSize(maximumSize).
					<PacketEventHolder>create(), null);
		} catch (IncompatibleClassChangeError e) {
			// Print in the console
			ProtocolLibrary.getErrorReporter().reportWarning(
				this, Report.newBuilder(REPORT_GUAVA_CORRUPT_MISSING).error(e));
			
			// It's a Beta class after all
			this.processingQueue = Synchronization.queue(
					new PriorityQueue<PacketEventHolder>(), null);
		}
				
		this.maximumConcurrency = maximumConcurrency;
		this.concurrentProcessing = new Semaphore(maximumConcurrency);
		this.sendingHandler = sendingHandler;
	}
	
	/**
	 * Enqueue a packet for processing by the asynchronous listeners.
	 * @param packet - packet to process.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 * @return TRUE if we sucessfully queued the packet, FALSE if the queue ran out if space.
	 */
	public boolean enqueue(PacketEvent packet, boolean onMainThread) {
		try {
			processingQueue.add(new PacketEventHolder(packet));

			// Begin processing packets
			signalBeginProcessing(onMainThread);
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * Number of packet events in the queue.
	 * @return The number of packet events in the queue.
	 */
	public int size() {
		return processingQueue.size();
	}
	
	/**
	 * Called by the current method and each thread to signal that a packet might be ready for processing.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void signalBeginProcessing(boolean onMainThread) {	
		while (concurrentProcessing.tryAcquire()) {
			PacketEventHolder holder = processingQueue.poll();
			
			// Any packet queued?
			if (holder != null) {
				PacketEvent packet = holder.getEvent();
				AsyncMarker marker = packet.getAsyncMarker();
				Collection<PrioritizedListener<AsyncListenerHandler>> list = getListener(packet.getPacketType());
				
				marker.incrementProcessingDelay();
				
				// Yes, removing the marker will cause the chain to stop
				if (list != null) {
					Iterator<PrioritizedListener<AsyncListenerHandler>> iterator = list.iterator();
					
					if (iterator.hasNext()) {
						marker.setListenerTraversal(iterator);
						iterator.next().getListener().enqueuePacket(packet);
						continue;
					}
				}
				
				// The packet has no further listeners. Just send it.
				if (marker.decrementProcessingDelay() == 0) {
					PacketSendingQueue sendingQueue = sendingHandler.getSendingQueue(packet, false);
					
					// In case the player has logged out
					if (sendingQueue != null)
						sendingQueue.signalPacketUpdate(packet, onMainThread);
				}
				signalProcessingDone();
				
			} else {
				// No more queued packets.
				signalProcessingDone();
				return;
			}
 		}
	}
	
	/**
	 * Called when a packet has been processed.
	 */
	public void signalProcessingDone() {
		concurrentProcessing.release();
	}

	/**
	 * Retrieve the maximum number of packets to process at any given time.
	 * @return Number of simultaneous packet to process.
	 */
	public int getMaximumConcurrency() {
		return maximumConcurrency;
	}
	
	public void cleanupAll() {
		// Cancel all the threads and every listener
		for (PrioritizedListener<AsyncListenerHandler> handler : values()) {
			if (handler != null) {
				handler.getListener().cancel();
			}
		}
		
		// Remove the rest, just in case
		clearListeners();
		
		// Remove every packet in the queue
		processingQueue.clear();
	}
}
