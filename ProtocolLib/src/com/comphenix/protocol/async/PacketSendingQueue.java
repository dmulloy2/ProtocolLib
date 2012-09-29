package com.comphenix.protocol.async;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Represents packets ready to be transmitted to a client.
 * @author Kristian
 */
class PacketSendingQueue {

	private PriorityBlockingQueue<AsyncPacket> sendingQueue;
	
	/**
	 * Enqueue a packet for sending. 
	 * @param packet
	 */
	public void enqueue(AsyncPacket packet) {
		sendingQueue.add(packet);
	}
	
	/**
	 * Invoked when one of the packets have finished processing.
	 */
	public synchronized void signalPacketUpdate(AsyncPacket packetUpdated) {
		
		// Mark this packet as finished
		packetUpdated.setProcessed(true);
		
		// Transmit as many packets as we can
		while (true) {
			AsyncPacket current = sendingQueue.peek();
			
			if (current != null && current.isProcessed()) {
				current.sendPacket();
				sendingQueue.poll();
			} else {
				break;
			}
		}
	}
}
