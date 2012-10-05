package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

/**
 * Provides a comparable to a packet event.
 * 
 * @author Kristian
 */
class PacketEventHolder implements Comparable<PacketEventHolder> {

	private PacketEvent event;

	/**
	 * A wrapper that ensures the packet event is ordered by sending index.
	 * @param event - packet event to wrap.
	 */
	public PacketEventHolder(PacketEvent event) {
		this.event = Preconditions.checkNotNull(event, "Event must be non-null");
	}

	/**
	 * Retrieve the stored event.
	 * @return The stored event.
	 */
	public PacketEvent getEvent() {
		return event;
	}
	
	@Override
	public int compareTo(PacketEventHolder other) {
		AsyncMarker marker = other != null ? other.getEvent().getAsyncMarker() : null;
		
		return ComparisonChain.start().
			   compare(event.getAsyncMarker(), marker).
			   result();
	}
}
