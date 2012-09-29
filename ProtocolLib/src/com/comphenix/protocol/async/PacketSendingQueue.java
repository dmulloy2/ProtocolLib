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

		// Transmit as many packets as we can
		while (true) {
			PacketEvent current = sendingQueue.peek();
			
			if (current != null) {
				AsyncMarker marker = current.getAsyncMarker();
				
				if (marker.isProcessed() || marker.hasExpired()) {
					if (marker.isProcessed())
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
	public synchronized void forceSend() {
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
		forceSend();
	}
}
