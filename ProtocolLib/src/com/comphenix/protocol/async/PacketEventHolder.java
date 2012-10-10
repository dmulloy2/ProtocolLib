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
	private long sendingIndex = 0;
	
	/**
	 * A wrapper that ensures the packet event is ordered by sending index.
	 * @param event - packet event to wrap.
	 */
	public PacketEventHolder(PacketEvent event) {
		this.event = Preconditions.checkNotNull(event, "Event must be non-null");
		
		if (event.getAsyncMarker() != null)
			this.sendingIndex = event.getAsyncMarker().getNewSendingIndex();
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
		return ComparisonChain.start().
			   compare(sendingIndex, other.sendingIndex).
			   result();
	}
}
