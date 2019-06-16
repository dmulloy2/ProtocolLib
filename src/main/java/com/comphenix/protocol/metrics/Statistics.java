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

import com.comphenix.protocol.ProtocolLib;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Statistics {

	// Metrics
	private Metrics metrics;

	public Statistics(ProtocolLib plugin) throws IOException {
		metrics = new Metrics(plugin);
		metrics.logFailedRequests(plugin.getProtocolConfig().isDebug());

		// Determine who is using this library
		addPluginUserGraph(metrics);
	}

	private void addPluginUserGraph(Metrics metrics) {
		metrics.addCustomChart(new Metrics.AdvancedPie("Plugin Users", this::getPluginUsers));
		metrics.addCustomChart(new Metrics.SimplePie("buildVersion", () -> splitVersion().getRight()));
	}

	public static Pair<String, String> splitVersion() {
		String version = ProtocolLibrary.getPlugin().getDescription().getVersion();
		if (version.contains("-b")) {
			String[] split = version.split("-b");
			return Pair.of(split[0], split[1]);
		} else {
			return Pair.of(version, "Unknown");
		}
	}

	// Retrieve loaded plugins
	private Map<String, Integer> getPluginUsers() {
		Map<String, Integer> users = new HashMap<>();

		for (PacketListener listener : ProtocolLibrary.getProtocolManager().getPacketListeners()) {
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
