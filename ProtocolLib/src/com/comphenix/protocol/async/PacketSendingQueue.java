package com.comphenix.protocol.async;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.ComparisonChain;

/**
 * Represents packets ready to be transmitted to a client.
 * @author Kristian
 */
class PacketSendingQueue {

	private static final int INITIAL_CAPACITY = 64;
	
	private PriorityBlockingQueue<PacketEvent> sendingQueue;
	
	// Whether or not packet transmission can only occur on the main thread
	private final boolean synchronizeMain;
	
	public PacketSendingQueue(boolean synchronizeMain) {
		this.synchronizeMain = synchronizeMain;
		this.sendingQueue = new PriorityBlockingQueue<PacketEvent>(INITIAL_CAPACITY, new Comparator<PacketEvent>() {
			// Compare using the async marker
			@Override
			public int compare(PacketEvent o1, PacketEvent o2) {
				return ComparisonChain.start().
					   compare(o1.getAsyncMarker(), o2.getAsyncMarker()).
					   result();
			}
		});
	}
	
	/**
	 * Enqueue a packet for sending. 
	 * @param packet
	 */
	public void enqueue(PacketEvent packet) {
		sendingQueue.add(packet);
	}
	
	/**
	 * Invoked when one of the packets have finished processing.
	 */
	public synchronized void signalPacketUpdate(PacketEvent packetUpdated, boolean onMainThread) {
		// Mark this packet as finished
		packetUpdated.getAsyncMarker().setProcessed(true);
		trySendPackets(onMainThread);
	}

	public synchronized void signalPacketUpdate(List<Integer> packetsRemoved, boolean onMainThread) {
		
		Set<Integer> lookup = new HashSet<Integer>(packetsRemoved);
		
		// Note that this is O(n), so it might be expensive
		for (PacketEvent event : sendingQueue) {
			if (lookup.contains(event.getPacketID())) {
				event.getAsyncMarker().setProcessed(true);
			}
		}
		
		// This is likely to have changed the situation a bit
		trySendPackets(onMainThread);
	}
	
	/**
	 * Attempt to send any remaining packets.
	 */
	public void trySendPackets(boolean onMainThread) {
		
		// Abort if we're not on the main thread
		if (synchronizeMain && !onMainThread)
			return;
		
		// Transmit as many packets as we can
		while (true) {
			PacketEvent current = sendingQueue.peek();
			
			if (current != null) {
				AsyncMarker marker = current.getAsyncMarker();
				
				if (marker.isProcessed() || marker.hasExpired()) {
					if (marker.isProcessed() && !current.isCancelled())
						sendPacket(current);
					
					sendingQueue.poll();
					continue;
				}
			}
			
			// Only repeat when packets are removed
			break;
		}
	}
	
	/**
	 * Send every packet, regardless of the processing state.
	 */
	private void forceSend() {
		while (true) {
			PacketEvent current = sendingQueue.poll();
			
			if (current != null) {
				sendPacket(current);
			} else {
				break;
			}
		}
	}
	
	/**
	 * Whether or not the packet transmission must synchronize with the main thread.
	 * @return TRUE if it must, FALSE otherwise.
	 */
	public boolean isSynchronizeMain() {
		return synchronizeMain;
	}

	/**
	 * Transmit a packet, if it hasn't already.
	 * @param event - the packet to transmit.
	 */
	private void sendPacket(PacketEvent event) {
		
		AsyncMarker marker = event.getAsyncMarker();
		
		try {
			// Don't send a packet twice
			if (marker != null && !marker.isTransmitted()) {
				marker.sendPacket(event);
			}
			
		} catch (IOException e) {
			// Just print the error
			e.printStackTrace();
		}
	}

	/**
	 * Automatically transmits every delayed packet.
	 */
	public void cleanupAll() {
		// Note that the cleanup itself will always occur on the main thread
		forceSend();
	}
}
