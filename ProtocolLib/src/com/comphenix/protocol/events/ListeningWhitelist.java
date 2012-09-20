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

package com.comphenix.protocol.events;

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

/**
 * Determines which packets will be observed by a listener, and with what priority.

 * @author Kristian 
 */
public class ListeningWhitelist {

	/**
	 * A whitelist with no packets - indicates that the listener shouldn't observe any packets.
	 */
	public static ListeningWhitelist EMPTY_WHITELIST = new ListeningWhitelist(ListenerPriority.LOW);
	
	private ListenerPriority priority;
	private Set<Integer> whitelist;

	/**
	 * Creates a packet whitelist for a given priority with a set of packet IDs.
	 * @param priority - the listener priority.
	 * @param whitelist - set of IDs to observe/enable. 
	 */
	public ListeningWhitelist(ListenerPriority priority, Set<Integer> whitelist) {
		this.priority = priority;
		this.whitelist = whitelist;
	}
	
	/**
	 * Creates a packet whitelist of a given priority for a list of packets.
	 * @param priority - the listener priority.
	 * @param whitelist - list of packet IDs to observe/enable.
	 */
	public ListeningWhitelist(ListenerPriority priority, Integer... whitelist) {
		this.priority = priority;
		this.whitelist = Sets.newHashSet(whitelist);
	}
	
	/**
	 * Whether or not this whitelist has any enabled packets.
	 * @return TRUE if there are any packets, FALSE otherwise.
	 */
	public boolean isEnabled() {
		return whitelist != null && whitelist.size() > 0;
	}
	
	/**
	 * Retrieve the priority in the execution order of the packet listener. Highest priority will be executed last.
	 * @return Execution order in terms of priority.
	 */
	public ListenerPriority getPriority() {
		return priority;
	}
	
	/**
	 * Retrieves the list of packets that will be observed by the listeners.
	 * @return Packet whitelist.
	 */
	public Set<Integer> getWhitelist() {
		return whitelist;
	}
	
	@Override
	public int hashCode(){
	    return Objects.hashCode(priority, whitelist);
	}

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof ListeningWhitelist){
	        final ListeningWhitelist other = (ListeningWhitelist) obj;
	        return Objects.equal(priority, other.priority)
	            && Objects.equal(whitelist, other.whitelist);
	    } else{
	        return false;
	    }
	}
	
	@Override
	public String toString() {
		if (this == EMPTY_WHITELIST)
			return "EMPTY_WHITELIST";
		else
			return Objects.toStringHelper(this)
				.add("priority", priority)
				.add("packets", whitelist).toString();
	}
	
}
