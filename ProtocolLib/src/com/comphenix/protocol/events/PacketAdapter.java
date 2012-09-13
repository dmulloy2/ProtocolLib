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

import org.bukkit.plugin.Plugin;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * Represents a packet listener with useful constructors.
 * 
 * @author Kristian
 */
public abstract class PacketAdapter implements PacketListener {
	
	protected Plugin plugin;
	protected Set<Integer> packetsID;
	protected ConnectionSide connectionSide;
	
	/**
	 * Initialize a packet listener.
	 * @param plugin - the plugin that spawned this listener.
	 * @param connectionSide - the packet type the listener is looking for.
	 * @param packets - the packet IDs the listener is looking for.
	 */
	public PacketAdapter(Plugin plugin, ConnectionSide connectionSide, Integer... packets) {
		this.plugin = plugin;
		this.connectionSide = connectionSide;
		this.packetsID = Sets.newHashSet(packets);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		// Default is to do nothing
	}
	
	@Override
	public void onPacketSending(PacketEvent event) {
		// And here too
	}
	
	@Override
	public ConnectionSide getConnectionSide() {
		return connectionSide;
	}
	
	@Override
	public Set<Integer> getPacketsID() {
		return packetsID;
	}
	
	@Override
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Retrieves the name of the plugin that has been associated with the listener.
	 * @return Name of the associated plugin.
	 */
	public static String getPluginName(PacketListener listener) {
		
		Plugin plugin = listener.getPlugin();
		
		// Try to get the plugin name
		try {
			if (plugin == null)
				return "UNKNOWN";
			else
				return plugin.getName();
			
		} catch (NoSuchMethodError e) {
			return plugin.toString();
		}
	}
	
	@Override
	public String toString() {		
		// This is used by the error reporter 
		return String.format("PacketAdapter[plugin=%s, side=%s, packets=%s]", 
				getPluginName(this), getConnectionSide().name(), 
				Joiner.on(", ").join(packetsID));
	}
}
