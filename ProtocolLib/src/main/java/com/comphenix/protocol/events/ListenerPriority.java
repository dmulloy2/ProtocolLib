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

/**
 * Represents a packet event priority, similar to the Bukkit EventPriority.
 * 
 * @author Kristian
 */
public enum ListenerPriority {
	/**
	 * Event call is of very low importance and should be ran first, to allow
	 * other plugins to further customise the outcome.
	 */
	LOWEST(0),
	/**
	 * Event call is of low importance.
	 */
	LOW(1),
	/**
	 * Event call is neither important or unimportant, and may be ran normally.
	 */
	NORMAL(2),
	/**
	 * Event call is of high importance.
	 */
	HIGH(3),
	/**
	 * Event call is critical and must have the final say in what happens to the
	 * event.
	 */
	HIGHEST(4),
	/**
	 * Event is listened to purely for monitoring the outcome of an event.
	 * <p/>
	 * No modifications to the event should be made under this priority.
	 */
	MONITOR(5);

	private final int slot;

	private ListenerPriority(int slot) {
		this.slot = slot;
	}

	/**
	 * A low slot represents a low priority.
	 * @return Integer representation of this priorty.
	 */
	public int getSlot() {
		return slot;
	}
}
