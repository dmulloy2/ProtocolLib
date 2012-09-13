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
		metrics.start();
		
		// Determine who is using this library
		addPluginUserGraph(metrics);
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

	/**
	 * Retrieve the current metrics object.
	 * @return Metrics object.
	 */
	public Metrics getMetrics() {
		return metrics;
	}
}
