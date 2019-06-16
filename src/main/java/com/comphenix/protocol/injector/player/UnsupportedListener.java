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

package com.comphenix.protocol.injector.player;

import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * Represents an error message from a player injector.
 * 
 * @author Kristian
 */
class UnsupportedListener {
	private String message;
	private int[] packets;
	
	/**
	 * Create a new error message.
	 * @param message - the message.
	 * @param packets - unsupported packets.
	 */
	public UnsupportedListener(String message, int[] packets) {
		super();
		this.message = message;
		this.packets = packets;
	}

	/**
	 * Retrieve the error message.
	 * @return Error message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Retrieve all unsupported packets.
	 * @return Unsupported packets.
	 */
	public int[] getPackets() {
		return packets;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", message, Joiner.on(", ").join(Arrays.asList(packets)));
	}
}
