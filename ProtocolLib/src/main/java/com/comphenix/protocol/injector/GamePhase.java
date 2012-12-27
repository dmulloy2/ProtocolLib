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

/**
 * The current player phase. This is used to limit the number of different injections.
 * 
 * @author Kristian
 */
public enum GamePhase {
	/**
	 * Only listen for packets sent or received before a player has logged in.
	 */
	LOGIN,
	
	/**
	 * Only listen for packets sent or received after a player has logged in.
	 */
	PLAYING,
	
	/**
	 * Listen for every sent and received packet.
	 */
	BOTH;
	
	/**
	 * Determine if the current value represents the login phase.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasLogin() {
		return this == LOGIN || this == BOTH;
	}
	
	/**
	 * Determine if the current value represents the playing phase.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasPlaying() {
		return this == PLAYING || this == BOTH;
	}
}
