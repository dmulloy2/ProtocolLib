package com.comphenix.protocol.events;

import java.util.Set;

import org.bukkit.plugin.Plugin;


public interface PacketListener {
	
	/**
	 * Invoked right before a packet is transmitted from the server to the client.
	 * <p>
	 * Note that the packet may be replaced, if needed.
	 * 
	 * @param event - the packet that should be sent.
	 */
	public void onPacketSending(PacketEvent event);

	/**
	 * Invoked right before a recieved packet from a client is being processed.
	 * @param event - the packet that has been recieved.
	 */
	public void onPacketReceiving(PacketEvent event);
	
	/**
	 * Retrieve whether or not we're listening for client or server packets.
	 * @return The type of packets we expect.
	 */
	public ConnectionSide getConnectionSide();
	
	/**
	 * Set of packet ids we expect to recieve.
	 * @return Packets IDs.
	 */
	public Set<Integer> getPacketsID();
	
	/**
	 * Retrieve the plugin that created list packet listener.
	 * @return The plugin, or NULL if not available.
	 */
	public Plugin getPlugin();
}
