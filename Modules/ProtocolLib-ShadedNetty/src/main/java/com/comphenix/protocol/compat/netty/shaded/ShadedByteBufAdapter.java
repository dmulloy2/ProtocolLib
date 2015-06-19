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
package com.comphenix.protocol.compat.netty.shaded;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

import net.minecraft.util.io.netty.buffer.AbstractByteBuf;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.ByteBufAllocator;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.io.ByteStreams;
import com.google.common.io.LimitInputStream;

/**
 * Construct a ByteBuf around an input stream and an output stream.
 * <p>
 * Note that as streams usually don't support seeking, this implementation will ignore
 * all indexing in the byte buffer.
 * @author Kristian
 */
public class ShadedByteBufAdapter extends AbstractByteBuf {
	private DataInputStream input;
	private DataOutputStream output;
	
	// For modifying the reader or writer index
	private static FieldAccessor READER_INDEX;
	private static FieldAccessor WRITER_INDEX;
	
	private static final int CAPACITY = Short.MAX_VALUE;
	
	private ShadedByteBufAdapter(DataInputStream input, DataOutputStream output) {
		// Just pick a figure
		super(CAPACITY);
		this.input = input;
		this.output = output;
		
		// Prepare accessors
		try {
			if (READER_INDEX == null) {
				READER_INDEX = Accessors.getFieldAccessor(AbstractByteBuf.class.getDeclaredField("readerIndex"));
			}
			if (WRITER_INDEX == null) {
				WRITER_INDEX = Accessors.getFieldAccessor(AbstractByteBuf.class.getDeclaredField("writerIndex"));
			}
		} catch (Exception e) {
			throw new RuntimeException("Cannot initialize ByteBufAdapter.", e);
		}
		
		// "Infinite" reading/writing
		if (input == null)
			READER_INDEX.set(this, Integer.MAX_VALUE);
		if (output == null)
			WRITER_INDEX.set(this, Integer.MAX_VALUE);
	}

	/**
	 * Construct a new Minecraft packet serializer using the current byte buf adapter.
	 * @param input - the input stream.
	 * @return A packet serializer with a wrapped byte buf adapter.
	 */
	public static ByteBuf packetReader(DataInputStream input) {
		return (ByteBuf) MinecraftReflection.getPacketDataSerializer(new ShadedByteBufAdapter(input, null));
	}
	
	/**
	 * Construct a new Minecraft packet deserializer using the current byte buf adapter.
	 * @param output - the output stream.
	 * @return A packet serializer with a wrapped byte buf adapter.
	 */
	public static ByteBuf packetWriter(DataOutputStream output) {
		return (ByteBuf) MinecraftReflection.getPacketDataSerializer(new ShadedByteBufAdapter(null, output));
	}
	
	@Override
	public int refCnt() {
		return 1;
	}

	@Override
	public boolean release() {
		return false;
	}

	@Override
	public boolean release(int paramInt) {
		return false;
	}

	@Override
	protected byte _getByte(int paramInt) {
		try {
			return input.readByte();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected short _getShort(int paramInt) {
		try {
			return input.readShort();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected int _getUnsignedMedium(int paramInt) {
		try {
			return input.readUnsignedShort();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected int _getInt(int paramInt) {
		try {
			return input.readInt();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected long _getLong(int paramInt) {
		try {
			return input.readLong();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected void _setByte(int index, int value) {
		try {
			output.writeByte(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setShort(int index, int value) {
		try {
			output.writeShort(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setMedium(int index, int value) {
		try {
			output.writeShort(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setInt(int index, int value) {
		try {
			output.writeInt(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setLong(int index, long value) {
		try {
			output.writeLong(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public int capacity() {
		return CAPACITY;
	}

	@Override
	public ByteBuf capacity(int paramInt) {
		return this;
	}

	@Override
	public ByteBufAllocator alloc() {
		return null;
	}

	@Override
	public ByteOrder order() {
		return ByteOrder.LITTLE_ENDIAN;
	}

	@Override
	public ByteBuf unwrap() {
		return null;
	}

	@Override
	public boolean isDirect() {
		return false;
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
		try {
			for (int i = 0; i < length; i++) {
				dst.setByte(dstIndex + i, input.read());
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
		try {
			input.read(dst, dstIndex, length);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuffer dst) {
		try {
			dst.put(ByteStreams.toByteArray(input));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, OutputStream dst, int length) throws IOException {
		ByteStreams.copy(new LimitInputStream(input, length), dst);
		return this;
	}

	@Override
	public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
		byte[] data = ByteStreams.toByteArray(new LimitInputStream(input, length));
		
		out.write(ByteBuffer.wrap(data));
		return data.length;
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
		byte[] buffer = new byte[length];
		src.getBytes(srcIndex, buffer);
		
		try {
			output.write(buffer);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
		try {
			output.write(src, srcIndex, length);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuffer src) {
		try {
			WritableByteChannel channel = Channels.newChannel(output);

			channel.write(src);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public int setBytes(int index, InputStream in, int length) throws IOException {
		LimitInputStream limit = new LimitInputStream(in, length);
		ByteStreams.copy(limit, output);
		return length - limit.available();
	}

	@Override
	public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		WritableByteChannel channel = Channels.newChannel(output);
		
		int count = in.read(buffer);
		channel.write(buffer);
		return count;
	}

	@Override
	public ByteBuf copy(int index, int length) {
		throw new UnsupportedOperationException("Cannot seek in input stream.");
	}

	@Override
	public int nioBufferCount() {
		return 0;
	}

	@Override
	public ByteBuffer nioBuffer(int paramInt1, int paramInt2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ByteBuffer internalNioBuffer(int paramInt1, int paramInt2) {
		return null;
	}

	@Override
	public ByteBuffer[] nioBuffers(int paramInt1, int paramInt2) {
		return null;
	}

	@Override
	public boolean hasArray() {
		return false;
	}

	@Override
	public byte[] array() {
		return null;
	}

	@Override
	public int arrayOffset() {
		return 0;
	}

	@Override
	public boolean hasMemoryAddress() {
		return false;
	}

	@Override
	public long memoryAddress() {
		return 0;
	}

	@Override
	public ByteBuf retain(int paramInt) {
		return this;
	}

	@Override
	public ByteBuf retain() {
		return this;
	}
}
