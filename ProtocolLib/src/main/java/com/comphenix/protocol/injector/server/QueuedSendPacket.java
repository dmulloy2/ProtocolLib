package com.comphenix.protocol.injector.server;

/**
 * Represents a single send packet command.
 * @author Kristian
 */
class QueuedSendPacket {
	private final Object packet;
	private final boolean filtered;
	
	public QueuedSendPacket(Object packet, boolean filtered) {
		this.packet = packet;
		this.filtered = filtered;
	}

	/**
	 * Retrieve the underlying packet that will be sent.
	 * @return The underlying packet.
	 */
	public Object getPacket() {
		return packet;
	}

	/**
	 * Determine if the packet should be intercepted by packet listeners.
	 * @return TRUE if it should, FALSE otherwise.
	 */
	public boolean isFiltered() {
		return filtered;
	}
}