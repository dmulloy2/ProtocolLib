package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/**
 * Represents an adapter version of the output handler interface.
 * @author Kristian
 */
public abstract class PacketOutputAdapter implements PacketOutputHandler {
	private final Plugin plugin;
	private final ListenerPriority priority;
	
	/**
	 * Construct a new packet output adapter with the given values.
	 * @param priority - the output handler priority.
	 * @param plugin - the owner plugin.
	 */
	public PacketOutputAdapter(Plugin plugin, ListenerPriority priority) {
		this.priority = priority;
		this.plugin = plugin;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
	
	@Override
	public ListenerPriority getPriority() {
		return priority;
	}
}
