package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.events.NetworkMarker;

/**
 * Represents a single send packet command.
 * @author Kristian
 */
class QueuedSendPacket {
	private final Object packet;
	private final NetworkMarker marker;
	private final boolean filtered;
	
	public QueuedSendPacket(Object packet, NetworkMarker marker, boolean filtered) {
		this.packet = packet;
		this.marker = marker;
		this.filtered = filtered;
	}

	/**
	 * Retrieve the network marker.
	 * @return Marker.
	 */
	public NetworkMarker getMarker() {
		return marker;
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