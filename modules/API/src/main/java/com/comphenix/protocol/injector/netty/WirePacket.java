/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
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
