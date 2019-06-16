package com.comphenix.protocol.injector;

/**
 * Sets the inject hook type. Different types allow for maximum compatibility.
 * @author Kristian
 */
public enum PlayerInjectHooks {
	/**
	 * The injection hook that does nothing. Set when every other inject hook fails.
	 */
	NONE,

	/**
	 * Override the network handler object itself. Only works in 1.3.
	 * <p>
	 * Cannot intercept MapChunk packets.
	 */
	NETWORK_MANAGER_OBJECT,

	/**
	 * Override the packet queue lists in NetworkHandler.
	 * <p>
	 * Cannot intercept MapChunk packets.
	 */
	NETWORK_HANDLER_FIELDS,

	/**
	 * Override the server handler object. Versatile, but a tad slower.
	 */
	NETWORK_SERVER_OBJECT;
}
