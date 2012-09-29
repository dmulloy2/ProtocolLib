package com.comphenix.protocol.async;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PrioritizedListener;

/**
 * Handles the processing of every packet type.
 * 
 * @author Kristian
 */
class PacketProcessingQueue extends AbstractConcurrentListenerMultimap<ListenerToken> {

	/**
	 * Default maximum number of packets to process concurrently.
	 */
	public static final int DEFAULT_MAXIMUM_CONCURRENCY = 5;
	
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
	private ArrayBlockingQueue<PacketEvent> processingQueue;
	
	// Packets for sending
	private PacketSendingQueue sendingQueue;

	public PacketProcessingQueue(PacketSendingQueue sendingQueue) {
		this(sendingQueue, DEFAULT_QUEUE_LIMIT, DEFAULT_MAXIMUM_CONCURRENCY);
	}
	
	public PacketProcessingQueue(PacketSendingQueue sendingQueue, int queueLimit, int maximumConcurrency) {
		super();
		this.processingQueue = new ArrayBlockingQueue<PacketEvent>(queueLimit);
		this.maximumConcurrency = maximumConcurrency;
		this.concurrentProcessing = new Semaphore(maximumConcurrency);
		this.sendingQueue = sendingQueue;
	}
	
	/**
	 * Enqueue a packet for processing by the asynchronous listeners.
	 * @param packet - packet to process.
	 * @return TRUE if we sucessfully queued the packet, FALSE if the queue ran out if space.
	 */
	public boolean enqueuePacket(PacketEvent packet) {
		try {
			processingQueue.add(packet);
			
			// Begin processing packets
			signalBeginProcessing();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * Called by the current method and each thread to signal that a packet might be ready for processing.
	 */
	public void signalBeginProcessing() {
		while (concurrentProcessing.tryAcquire()) {
			PacketEvent packet = processingQueue.poll();
			
			// Any packet queued?
			if (packet != null) {
				Collection<PrioritizedListener<ListenerToken>> list = getListener(packet.getPacketID());
				AsyncMarker marker = packet.getAsyncMarker();
				
				if (list != null) {
					Iterator<PrioritizedListener<ListenerToken>> iterator = list.iterator();
					
					if (iterator.hasNext()) {
						marker.setListenerTraversal(iterator);
						iterator.next().getListener().enqueuePacket(packet);
						continue;
					}
				}
				
				// The packet has no listeners. Just send it.
				sendingQueue.signalPacketUpdate(packet);
				signalProcessingDone();
				
			} else {
				// No more queued packets. 
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
		for (PrioritizedListener<ListenerToken> token : values()) {
			if (token != null) {
				token.getListener().cancel();
			}
		}
		
		// Remove the rest, just in case
		clearListeners();
	}
}
