/**
 * (c) 2015 dmulloy2
 */
package com.comphenix.protocol.injector.netty;

import io.netty.buffer.ByteBuf;

/**
 * @author dmulloy2
 */

public class WirePacket {
	private final int id;
	private final byte[] bytes;

	public WirePacket(int id, byte[] bytes) {
		this.id = id;
		this.bytes = bytes;
	}

	public int getId() {
		return id;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void writeId(ByteBuf output) {
		int i = id;
		while ((i & -128) != 0) {
			output.writeByte(i & 127 | 128);
			i >>>= 7;
		}

		output.writeByte(i);
	}

	public void writeBytes(ByteBuf output) {
		output.writeBytes(bytes);
	}
}