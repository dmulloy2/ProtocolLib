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

package com.comphenix.protocol.metrics;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.metrics.Metrics.Graph;

public class Statistics {

	// Metrics
	private Metrics metrics;
	
	public Statistics(Plugin plugin) throws IOException {
		metrics = new Metrics(plugin);
		
		// Determine who is using this library
		addPluginUserGraph(metrics);
		
		metrics.start();
	}
	
	private void addPluginUserGraph(Metrics metrics) {
	
		Graph pluginUsers = metrics.createGraph("Plugin Users");
		
		for (Map.Entry<String, Integer> entry : getPluginUsers(ProtocolLibrary.getProtocolManager()).entrySet()) {
			final int count = entry.getValue();
			
			// Plot plugins of this type
			pluginUsers.addPlotter(new Metrics.Plotter(entry.getKey()) {
				@Override
				public int getValue() {
					return count;
				}
			});
		}
	}
	
	// Retrieve loaded plugins
	private Map<String, Integer> getPluginUsers(ProtocolManager manager) {
		
		Map<String, Integer> users = new HashMap<String, Integer>();
			
		for (PacketListener listener : manager.getPacketListeners()) {
			
			String name = PacketAdapter.getPluginName(listener);
			
			// Increment occurence
			if (!users.containsKey(name)) {
				users.put(name, 1);
			} else {
				users.put(name, users.get(name) + 1);
			}
		}
		
		return users;
	}
}
