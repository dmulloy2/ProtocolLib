package com.comphenix.protocol.injector.player;

import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * Represents an error message from a player injector.
 * 
 * @author Kristian
 */
class UnsupportedListener {
	private String message;
	private int[] packets;
	
	/**
	 * Create a new error message.
	 * @param message - the message.
	 * @param packets - unsupported packets.
	 */
	public UnsupportedListener(String message, int[] packets) {
		super();
		this.message = message;
		this.packets = packets;
	}

	/**
	 * Retrieve the error message.
	 * @return Error message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Retrieve all unsupported packets.
	 * @return Unsupported packets.
	 */
	public int[] getPackets() {
		return packets;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", message, Joiner.on(", ").join(Arrays.asList(packets)));
	}
}
