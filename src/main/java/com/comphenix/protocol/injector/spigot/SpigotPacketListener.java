package com.comphenix.protocol.injector.spigot;

/**
 * Represents a proxy for a Spigot packet listener.
 * 
 * @author Kristian
 */
interface SpigotPacketListener {
	/**
	 * Called when a packet has been received and is about to be handled by the
	 * current Connection. 
	 * <p>
	 * The returned packet will be the packet passed on for handling, or in the case of 
	 * null being returned, not handled at all.
	 *
	 * @param networkManager - the NetworkManager receiving the packet
	 * @param connection - the connection which will handle the packet
	 * @param packet - the received packet
	 * @return the packet to be handled, or null to cancel
	 */
	public Object packetReceived(Object networkManager, Object connection, Object packet);
	
	/**
	 * Called when a packet is queued to be sent. 
	 * <p>
	 * The returned packet will be the packet sent. In the case of null being returned, 
	 * the packet will not be sent.
	 * @param networkManager - the NetworkManager which will send the packet
	 * @param connection - the connection which queued the packet
	 * @param packet - the queue packet
	 * @return the packet to be sent, or null if the packet will not be sent.
	 */
	public Object packetQueued(Object networkManager, Object connection, Object packet);
}
