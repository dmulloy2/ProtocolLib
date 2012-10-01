package com.comphenix.protocol.async;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Represents packets ready to be transmitted to a client.
 * @author Kristian
 */
class PacketSendingQueue {

	public static final int INITIAL_CAPACITY = 64;
	
	private PriorityBlockingQueue<PacketEventHolder> sendingQueue;
	
	// Whether or not packet transmission can only occur on the main thread
	private final boolean synchronizeMain;
	
	/**
	 * Create a packet sending queue.
	 * @param synchronizeMain - whether or not to synchronize with the main thread.
	 */
	public PacketSendingQueue(boolean synchronizeMain) {
		this.sendingQueue = new PriorityBlockingQueue<PacketEventHolder>(INITIAL_CAPACITY);
		this.synchronizeMain = synchronizeMain;
	}
	
	/**
	 * Enqueue a packet for sending. 
	 * @param packet
	 */
	public void enqueue(PacketEvent packet) {
		sendingQueue.add(new PacketEventHolder(packet));
	}
	
	/**
	 * Invoked when one of the packets have finished processing.
	 * @param packetUpdated - the packet that has now been updated.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public synchronized void signalPacketUpdate(PacketEvent packetUpdated, boolean onMainThread) {
		// Mark this packet as finished
		packetUpdated.getAsyncMarker().setProcessed(true);
		trySendPackets(onMainThread);
	}

	/***
	 * Invoked when a list of packet IDs are no longer associated with any listeners.
	 * @param packetsRemoved - packets that no longer have any listeners.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public synchronized void signalPacketUpdate(List<Integer> packetsRemoved, boolean onMainThread) {
		
		Set<Integer> lookup = new HashSet<Integer>(packetsRemoved);
		
		// Note that this is O(n), so it might be expensive
		for (PacketEventHolder holder : sendingQueue) {
			PacketEvent event = holder.getEvent();
			
			if (lookup.contains(event.getPacketID())) {
				event.getAsyncMarker().setProcessed(true);
			}
		}
		
		// This is likely to have changed the situation a bit
		trySendPackets(onMainThread);
	}
	
	/**
	 * Attempt to send any remaining packets.
	 * @param onMainThread - whether or not this is occuring on the main thread.
	 */
	public void trySendPackets(boolean onMainThread) {
				
		// Transmit as many packets as we can
		while (true) {
			PacketEventHolder holder = sendingQueue.peek();
					
			if (holder != null) {
				PacketEvent current = holder.getEvent();
				AsyncMarker marker = current.getAsyncMarker();
				
				// Abort if we're not on the main thread
				if (synchronizeMain) {
					try {
						boolean wantAsync = marker.isMinecraftAsync(current);
						boolean wantSync = !wantAsync;
						
						// Quit if we haven't fulfilled our promise
						if ((onMainThread && wantAsync) || (!onMainThread && wantSync))
							return;
						
					} catch (FieldAccessException e) {
						e.printStackTrace();
						return;
					}
				}
				
				if (marker.isProcessed() || marker.hasExpired()) {
					if (marker.isProcessed() && !current.isCancelled()) {
						sendPacket(current);
					}
					
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
			PacketEventHolder holder = sendingQueue.poll();
			
			if (holder != null) {
				sendPacket(holder.getEvent());
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
