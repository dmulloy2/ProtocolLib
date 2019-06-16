package com.comphenix.protocol.events;

import org.bukkit.plugin.Plugin;

/**
 * Represents a custom packet serializer onto the network stream.
 * 
 * @author Kristian
 */
public interface PacketOutputHandler {
	/**
	 * Retrieve the priority that decides the order each network handler is allowed to manipulate the output buffer.
	 * <p>
	 * Higher priority is executed before lower.
	 * @return The handler priority.
	 */
	public ListenerPriority getPriority();
	
	/**
	 * The plugin that owns this output handler.
	 * @return The owner plugin.
	 */
	public Plugin getPlugin();
	
	/**
	 * Invoked when a given packet is to be written to the output stream.
	 * <p>
	 * Note that the buffer is initially filled with the output from the default write method.
	 * <p>
	 * In Minecraft 1.6.4, the header is always excluded, whereas it MUST be included in Minecraft 1.7.2. Call 
	 * {@link NetworkMarker#requireOutputHeader()} to determine this.
	 * @param event - the packet that will be outputted.
	 * @param buffer - the data that is currently scheduled to be outputted.
	 * @return The modified byte array to write. NULL is not permitted.
	 */
	public byte[] handle(PacketEvent event, byte[] buffer);
}
