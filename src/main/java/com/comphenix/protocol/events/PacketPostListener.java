package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/**
 * Represents a packet listener that is invoked after a packet has been sent or received.
 * @author Kristian
 */
public interface PacketPostListener {
	/**
	 * Retrieve the plugin this listener belongs to.
	 * @return The assoicated plugin.
	 */
	public Plugin getPlugin();
	
	/**
	 * Invoked after a packet has been sent or received.
	 * <p>
	 * Note that this is invoked asynchronously.
	 * @param event - the packet event.
	 */
	public void onPostEvent(PacketEvent event);
}
