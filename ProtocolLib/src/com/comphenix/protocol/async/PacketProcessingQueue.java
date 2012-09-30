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
class PacketProcessingQueue extends AbstractConcurrentListenerMultimap<AsyncListenerHandler> {

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
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 * @return TRUE if we sucessfully queued the packet, FALSE if the queue ran out if space.
	 */
	public boolean enqueue(PacketEvent packet, boolean onMainThread) {
		try {
			processingQueue.add(packet);
			
			// Begin processing packets
			signalBeginProcessing(onMainThread);
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * Called by the current method and each thread to signal that a packet might be ready for processing.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void signalBeginProcessing(boolean onMainThread) {	
		while (concurrentProcessing.tryAcquire()) {
			PacketEvent packet = processingQueue.poll();
			
			// Any packet queued?
			if (packet != null) {
				Collection<PrioritizedListener<AsyncListenerHandler>> list = getListener(packet.getPacketID());
				AsyncMarker marker = packet.getAsyncMarker();
				
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
				sendingQueue.signalPacketUpdate(packet, onMainThread);
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
	}
}
