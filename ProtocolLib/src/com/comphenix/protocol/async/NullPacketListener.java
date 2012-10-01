package com.comphenix.protocol.async;

import org.bukkit.plugin.Plugin;

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
			return new ListeningWhitelist(priority, whitelist.getWhitelist());
		else 
			return null;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}
