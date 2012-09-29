package com.comphenix.protocol.async;

import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import com.comphenix.protocol.events.PacketEvent;

/**
 * Represents packets ready to be transmitted to a client.
 * @author Kristian
 */
class PacketSendingQueue {

	private PriorityBlockingQueue<PacketEvent> sendingQueue;
	
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
	public synchronized void signalPacketUpdate(PacketEvent packetUpdated) {
		// Mark this packet as finished
		packetUpdated.getAsyncMarker().setProcessed(true);
		signalPacketUpdates();
	}
	
	/**
	 * Send every packet, regardless of the processing state.
	 */
	public synchronized void forceSend() {
		while (true) {
			PacketEvent current = sendingQueue.poll();
			
			if (current != null) {
				// Just print the error
				try {
					current.getAsyncMarker().sendPacket(current);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
	}
	
	/**
	 * Invoked when potentially every packet is finished.
	 */
	private void signalPacketUpdates() {
		// Transmit as many packets as we can
		while (true) {
			PacketEvent current = sendingQueue.peek();
			
			if (current != null && current.getAsyncMarker().isProcessed()) {
				// Just print the error
				try {
					current.getAsyncMarker().sendPacket(current);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				sendingQueue.poll();
				
			} else {
				break;
			}
		}
		
		// And we're done
	}

	/**
	 * Automatically transmits every delayed packet.
	 */
	public void cleanupAll() {
		forceSend();
	}
}
