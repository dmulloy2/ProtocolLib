package com.comphenix.protocol.events;

/**
 * Represents additional options a listener may require.
 * 
 * @author Kristian
 */
public enum ListenerOptions {
	/**
	 * Retrieve the serialized client packet as it appears on the network stream.
	 */
	INTERCEPT_INPUT_BUFFER,
}
