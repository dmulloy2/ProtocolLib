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

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.comphenix.protocol.injector.GamePhase;
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
	public static final ListeningWhitelist EMPTY_WHITELIST = new ListeningWhitelist(ListenerPriority.LOW);
	
	private ListenerPriority priority;
	private Set<Integer> whitelist;
	private GamePhase gamePhase;
	private Set<ListenerOptions> options = EnumSet.noneOf(ListenerOptions.class);

	/**
	 * Creates a packet whitelist for a given priority with a set of packet IDs.
	 * @param priority - the listener priority.
	 * @param whitelist - set of IDs to observe/enable. 
	 */
	public ListeningWhitelist(ListenerPriority priority, Set<Integer> whitelist) {
		this(priority, whitelist, GamePhase.PLAYING);
	}
	
	/**
	 * Creates a packet whitelist for a given priority with a set of packet IDs.
	 * @param priority - the listener priority.
	 * @param whitelist - set of IDs to observe/enable.
	 * @param gamePhase - which game phase to receieve notifications on.
	 */
	public ListeningWhitelist(ListenerPriority priority, Set<Integer> whitelist, GamePhase gamePhase) {
		this.priority = priority;
		this.whitelist = whitelist;
		this.gamePhase = gamePhase;
	}
	
	/**
	 * Creates a packet whitelist of a given priority for a list of packets.
	 * @param priority - the listener priority.
	 * @param whitelist - list of packet IDs to observe/enable.
	 */
	public ListeningWhitelist(ListenerPriority priority, Integer... whitelist) {
		this.priority = priority;
		this.whitelist = Sets.newHashSet(whitelist);
		this.gamePhase = GamePhase.PLAYING;
	}
	
	/**
	 * Creates a packet whitelist for a given priority with a set of packet IDs.
	 * @param priority - the listener priority.
	 * @param whitelist - list of packet IDs to observe/enable.
	 * @param gamePhase - which game phase to receieve notifications on.
	 */
	public ListeningWhitelist(ListenerPriority priority, Integer[] whitelist, GamePhase gamePhase) {
		this.priority = priority;
		this.whitelist = Sets.newHashSet(whitelist);
		this.gamePhase = gamePhase;
	}
	
	/**
	 * Creates a packet whitelist for a given priority with a set of packet IDs and options.
	 * @param priority - the listener priority.
	 * @param whitelist - list of packet IDs to observe/enable.
	 * @param gamePhase - which game phase to receieve notifications on.
	 */
	public ListeningWhitelist(ListenerPriority priority, Integer[] whitelist, GamePhase gamePhase, ListenerOptions... options) {
		this.priority = priority;
		this.whitelist = Sets.newHashSet(whitelist);
		this.gamePhase = gamePhase;
		
		if (options != null) {
			this.options.addAll(Arrays.asList(options));
		}
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

	/**
	 * Retrieve which game phase this listener is active under.
	 * @return The active game phase.
	 */
	public GamePhase getGamePhase() {
		return gamePhase;
	}
	
	/**
	 * Retrieve every special option associated with this whitelist.
	 * @return Every special option.
	 */
	public Set<ListenerOptions> getOptions() {
		return Collections.unmodifiableSet(options);
	}
	
	@Override
	public int hashCode(){
	    return Objects.hashCode(priority, whitelist, gamePhase, options);
	}

	/**
	 * Determine if any of the given IDs can be found in the whitelist.
	 * @param whitelist - whitelist to test.
	 * @param idList - list of packet IDs to find. 
	 * @return TRUE if any of the packets in the list can be found in the whitelist, FALSE otherwise.
	 */
	public static boolean containsAny(ListeningWhitelist whitelist, int... idList) {
		if (whitelist != null) {
			for (int i = 0; i < idList.length; i++) {
				if (whitelist.getWhitelist().contains(idList[i]))
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Determine if the given whitelist is empty or not.
	 * @param whitelist - the whitelist to test.
	 * @return TRUE if the whitelist is empty, FALSE otherwise.
	 */
	public static boolean isEmpty(ListeningWhitelist whitelist) {
		if (whitelist == EMPTY_WHITELIST)
			return true;
		else if (whitelist == null)
			return true;
		else
			return whitelist.getWhitelist().isEmpty();
	}
	
	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof ListeningWhitelist){
	        final ListeningWhitelist other = (ListeningWhitelist) obj;
	        return Objects.equal(priority, other.priority)
	            && Objects.equal(whitelist, other.whitelist)
		        && Objects.equal(gamePhase, other.gamePhase)
		        && Objects.equal(options, other.options);
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
				.add("packets", whitelist)
				.add("gamephase", gamePhase)
				.add("options", options).
				toString();
	}
}
