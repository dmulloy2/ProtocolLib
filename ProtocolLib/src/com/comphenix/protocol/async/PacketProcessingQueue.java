package com.comphenix.protocol.async;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import com.comphenix.protocol.concurrency.SortedCopyOnWriteArray;

/**
 * Handles the processing of a certain packet type.
 * 
 * @author Kristian
 */
class PacketProcessingQueue {


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
	private ArrayBlockingQueue<AsyncPacket> processingQueue;

	// Packet listeners
	private SortedCopyOnWriteArray<>
	
	public PacketProcessingQueue() {
		this(DEFAULT_QUEUE_LIMIT, DEFAULT_MAXIMUM_CONCURRENCY);
	}
	
	public PacketProcessingQueue(int queueLimit, int maximumConcurrency) {
		this.processingQueue = new ArrayBlockingQueue<AsyncPacket>(queueLimit);
		this.maximumConcurrency = maximumConcurrency;
		this.concurrentProcessing = new Semaphore(maximumConcurrency);
	}
	
	public boolean queuePacket(AsyncPacket packet) {
		try {
			processingQueue.add(packet);
			
			// Begin processing packets
			processPacket();
			return true;
		} catch (IllegalStateException e) {
			return false;
		}
	}
	
	public void processPacket() {
		if (concurrentProcessing.tryAcquire()) {
			AsyncPacket packet = processingQueue.poll();
			
			// Any packet queued?
			if (packet != null) {
				
			}
 		}
	}

	public int getMaximumConcurrency() {
		return maximumConcurrency;
	}
}
