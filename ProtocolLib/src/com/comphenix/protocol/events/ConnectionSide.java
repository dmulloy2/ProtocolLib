package com.comphenix.protocol.events;

/**
 * Used to set a packet filter.
 * 
 * @author Kristian
 */
public enum ConnectionSide {
	/**
	 * Listen for server side packets that will invoke onPacketSending().
	 */
	SERVER_SIDE,
	
	/**
	 * Listen for client side packets that will invoke onPacketReceiving().
	 */
	CLIENT_SIDE,
	
	/**
	 * Listen for both client and server side packets.
	 */
	BOTH;
	
	public boolean isForClient() {
		return this == CLIENT_SIDE || this == BOTH;
	}
	
	public boolean isForServer() {
		return this == SERVER_SIDE || this == BOTH;
	}
}
