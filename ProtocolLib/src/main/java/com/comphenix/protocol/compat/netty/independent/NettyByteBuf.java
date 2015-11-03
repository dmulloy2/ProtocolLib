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
package com.comphenix.protocol.compat.netty.independent;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;

import com.comphenix.protocol.compat.netty.WrappedByteBuf;

/**
 * @author dmulloy2
 */

public class NettyByteBuf implements WrappedByteBuf {
	private WeakReference<ByteBuf> handle;

	public NettyByteBuf(ByteBuf handle) {
		this.handle = new WeakReference<ByteBuf>(handle);
	}

	@Override
	public void writeBytes(ObjectInputStream input, int id) throws IOException {
		handle.get().writeBytes(input, id);
	}

	@Override
	public Object getHandle() {
		return handle.get();
	}

	@Override
	public int readableBytes() {
		return handle.get().readableBytes();
	}

	@Override
	public void readBytes(ObjectOutputStream output, int readableBytes) throws IOException {
		handle.get().readBytes(output, readableBytes);
	}

	@Override
	public void readBytes(byte[] data) {
		handle.get().readBytes(data);
	}

	@Override
	public void writeByte(byte b) {
		handle.get().writeByte(b);
	}

	@Override
	public void writeByte(int i) {
		handle.get().writeByte(i);
	}

	@Override
	public void writeBytes(byte[] bytes) {
		handle.get().writeBytes(bytes);
	}

	@Override
	public byte[] array() {
		return handle.get().array();
	}
}