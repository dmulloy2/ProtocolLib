/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.async;

import com.comphenix.protocol.events.PacketEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import com.google.common.primitives.Longs;

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
	
	@Override
	public boolean equals(Object other) {
		// Standard equals
		if (other == this)
			return true;
		if (other instanceof PacketEventHolder)
			return sendingIndex == ((PacketEventHolder) other).sendingIndex;
		else
			return false;
	}
	
	@Override
	public int hashCode() {
		return Longs.hashCode(sendingIndex);
	}
}
