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

import org.bukkit.plugin.Plugin;

/**
 * Represents a listener that receives notifications when packets are sending or being received.
 * <p>
 * Use {@link PacketAdapter} for a simple wrapper around this interface.
 * @author Kristian
 */
public interface PacketListener {
	
	/**
	 * Invoked right before a packet is transmitted from the server to the client.
	 * <p>
	 * Note that the packet may be replaced, if needed.
	 * <p>
	 * This method is executed on the main thread in 1.6.4 and earlier, and thus the Bukkit API is safe to use.
	 * <p>
	 * In Minecraft 1.7.2 and later, this method MAY be executed asynchronously, but only if {@link ListenerOptions#ASYNC} 
	 * have been specified in the listener. This is off by default.
	 * @param event - the packet that should be sent.
	 */
	public void onPacketSending(PacketEvent event);

	/**
	 * Invoked right before a received packet from a client is being processed.
	 * <p>
	 * <b>WARNING</b>: </br> 
	 * This method will be called <i>asynchronously</i>! You should synchronize with the main 
	 * thread using {@link org.bukkit.scheduler.BukkitScheduler#scheduleSyncDelayedTask(Plugin, Runnable, long) scheduleSyncDelayedTask} 
	 * if you need to call the Bukkit API.
	 * @param event - the packet that has been received.
	 */
	public void onPacketReceiving(PacketEvent event);
	
	/**
	 * Retrieve which packets sent by the server this listener will observe.
	 * @return List of server packets to observe, along with the priority.
	 */
	public ListeningWhitelist getSendingWhitelist();
	
	/**
	 * Retrieve which packets sent by the client this listener will observe.
	 * @return List of server packets to observe, along with the priority.
	 */
	public ListeningWhitelist getReceivingWhitelist();
	
	/**
	 * Retrieve the plugin that created list packet listener.
	 * @return The plugin, or NULL if not available.
	 */
	public Plugin getPlugin();
}
