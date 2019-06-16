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

package com.comphenix.protocol.injector;

import com.comphenix.protocol.events.ListenerPriority;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

/**
 * Represents a listener with a priority.
 * 
 * @author Kristian
 */
public class PrioritizedListener<TListener> implements Comparable<PrioritizedListener<TListener>> {

	private TListener listener;
	private ListenerPriority priority;
	
	public PrioritizedListener(TListener listener, ListenerPriority priority) {
		this.listener = listener;
		this.priority = priority;
	}

	@Override
	public int compareTo(PrioritizedListener<TListener> other) {
		// This ensures that lower priority listeners are executed first
		return Ints.compare(
			this.getPriority().getSlot(),
			other.getPriority().getSlot());
	}
	
	// Note that this equals() method is NOT consistent with compareTo(). 
	// But, it's a private class so who cares.
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		// We only care about the listener - priority itself should not make a difference
	    if(obj instanceof PrioritizedListener){
	        final PrioritizedListener<TListener> other = (PrioritizedListener<TListener>) obj;
	        return Objects.equal(listener, other.listener);
	    } else {
	        return false;
	    }
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(listener);
	}
	
	/**
	 * Retrieve the underlying listener.
	 * @return Underlying listener.
	 */
	public TListener getListener() {
		return listener;
	}

	/**
	 * Retrieve the priority of this listener.
	 * @return Listener priority.
	 */
	public ListenerPriority getPriority() {
		return priority;
	}
}
