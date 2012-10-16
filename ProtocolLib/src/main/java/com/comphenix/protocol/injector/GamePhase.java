package com.comphenix.protocol.injector;

/**
 * The current player phase. This is used to limit the number of different injections.
 * 
 * @author Kristian
 */
public enum GamePhase {
	/**
	 * Only listen for packets sent or received before a player has logged in.
	 */
	LOGIN,
	
	/**
	 * Only listen for packets sent or received after a player has logged in.
	 */
	PLAYING,
	
	/**
	 * Listen for every sent and received packet.
	 */
	BOTH;
	
	/**
	 * Determine if the current value represents the login phase.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasLogin() {
		return this == LOGIN || this == BOTH;
	}
	
	/**
	 * Determine if the current value represents the playing phase.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasPlaying() {
		return this == PLAYING || this == BOTH;
	}
}
