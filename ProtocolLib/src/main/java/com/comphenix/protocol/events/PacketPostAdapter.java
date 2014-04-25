package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

/**
 * Represents an adapter version of a post listener.
 * @author Kristian
 */
public abstract class PacketPostAdapter implements PacketPostListener {
	private Plugin plugin;
	
	public PacketPostAdapter(Plugin plugin) {
		this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}
