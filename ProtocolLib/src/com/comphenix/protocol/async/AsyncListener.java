package com.comphenix.protocol.async;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ListeningWhitelist;

public interface AsyncListener {
	public void onAsyncPacket(AsyncPacket packet);
	
	public ListeningWhitelist getSendingWhitelist();
	
	/**
	 * Retrieve the plugin that created this async packet listener.
	 * @return The plugin, or NULL if not available.
	 */
	public Plugin getPlugin();
}
