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

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * Represents a NO OPERATION listener.
 * 
 * @author Kristian
 */
class NullPacketListener implements PacketListener {
	
	private ListeningWhitelist sendingWhitelist;
	private ListeningWhitelist receivingWhitelist;
	private Plugin plugin;

	/**
	 * Create a no-op listener with the same whitelist and plugin as the given listener.
	 * @param original - the packet listener to copy.
	 */
	public NullPacketListener(PacketListener original) {
		this.sendingWhitelist = cloneWhitelist(ListenerPriority.LOW, original.getSendingWhitelist());
		this.receivingWhitelist = cloneWhitelist(ListenerPriority.LOW, original.getReceivingWhitelist());
		this.plugin = original.getPlugin();
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		// NULL
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		// NULL
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return sendingWhitelist;
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return receivingWhitelist;
	}
	
	private ListeningWhitelist cloneWhitelist(ListenerPriority priority, ListeningWhitelist whitelist) {
		if (whitelist != null) 
			// We don't use the Bukkit API, so don't engage the ProtocolLib synchronization code
			return ListeningWhitelist.newBuilder(whitelist).priority(priority).mergeOptions(ListenerOptions.ASYNC).build();
		else 
			return null;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}
