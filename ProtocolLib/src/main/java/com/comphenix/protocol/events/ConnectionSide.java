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

import com.comphenix.protocol.PacketType.Sender;

/**
 * Used to set a packet filter.
 * 
 * @author Kristian
 */
public enum ConnectionSide {
	/**
	 * Listen for server side packets that will invoke onPacketSending().
	 */
	SERVER_SIDE,
	
	/**
	 * Listen for client side packets that will invoke onPacketReceiving().
	 */
	CLIENT_SIDE,
	
	/**
	 * Listen for both client and server side packets.
	 */
	BOTH;
	
	public boolean isForClient() {
		return this == CLIENT_SIDE || this == BOTH;
	}
	
	public boolean isForServer() {
		return this == SERVER_SIDE || this == BOTH;
	}
	
	/**
	 * Retrieve the sender of this connection side.
	 * <p>
	 * This is NULL for {@link #BOTH}.
	 * @return The sender.
	 */
	public Sender getSender() {
		if (this == SERVER_SIDE)
			return Sender.SERVER;
		else if (this == CLIENT_SIDE)
			return Sender.CLIENT;
		return null;
	}
	
	/**
	 * If both connection sides are present, return {@link #BOTH} - otherwise, return the one valud connection side.
	 * <p>
	 * NULL is not a valid connection side.
	 * @param a - the first connection side.
	 * @param b - the second connection side.
	 * @return BOTH or the one valid side, or NULL.
	 */
	public static ConnectionSide add(ConnectionSide a, ConnectionSide b) {
		if (a == null)
			return b;
		if (b == null)
			return a;
		
		// Now merge them together
		boolean client = a.isForClient() || b.isForClient();
		boolean server = a.isForServer() || b.isForServer();
		
		if (client && server)
			return BOTH;
		else if (client)
			return CLIENT_SIDE;
		else
			return SERVER_SIDE;
	}
}
