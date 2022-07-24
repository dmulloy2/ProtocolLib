/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.io.ByteStreams;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Construct a ByteBuf around an input stream and an output stream.
 * <p>
 * Note that as streams usually don't support seeking, this implementation will ignore all indexing in the byte buffer.
 *
 * @author Kristian
 */
@SuppressWarnings("unused")
public class NettyByteBufAdapter extends AbstractByteBuf {

	private static final int CAPACITY = Integer.MAX_VALUE;

	// For modifying the reader or writer index
	private static FieldAccessor READER_INDEX;
	private static FieldAccessor WRITER_INDEX;

	private final DataInputStream input;
	private final DataOutputStream output;

	private NettyByteBufAdapter(DataInputStream input, DataOutputStream output) {
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
		if (input == null) {
			READER_INDEX.set(this, Integer.MAX_VALUE);
		}

		if (output == null) {
			WRITER_INDEX.set(this, Integer.MAX_VALUE);
		}
	}

	/**
	 * Construct a new Minecraft packet serializer using the current byte buf adapter.
	 *
	 * @param input - the input stream.
	 * @return A packet serializer with a wrapped byte buf adapter.
	 */
	public static ByteBuf packetReader(DataInputStream input) {
		return (ByteBuf) MinecraftReflection.getPacketDataSerializer(new NettyByteBufAdapter(input, null));
	}

	/**
	 * Construct a new Minecraft packet deserializer using the current byte buf adapter.
	 *
	 * @param output - the output stream.
	 * @return A packet serializer with a wrapped byte buf adapter.
	 */
	public static ByteBuf packetWriter(DataOutputStream output) {
		return (ByteBuf) MinecraftReflection.getPacketDataSerializer(new NettyByteBufAdapter(null, output));
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
			return this.input.readByte();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected short _getShort(int paramInt) {
		try {
			return this.input.readShort();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected int _getUnsignedMedium(int paramInt) {
		try {
			return this.input.readUnsignedShort();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected int _getInt(int paramInt) {
		try {
			return this.input.readInt();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected long _getLong(int paramInt) {
		try {
			return this.input.readLong();
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
	}

	@Override
	protected void _setByte(int index, int value) {
		try {
			this.output.writeByte(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setShort(int index, int value) {
		try {
			this.output.writeShort(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setMedium(int index, int value) {
		try {
			this.output.writeShort(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setInt(int index, int value) {
		try {
			this.output.writeInt(value);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	protected void _setLong(int index, long value) {
		try {
			this.output.writeLong(value);
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
		return ByteBufAllocator.DEFAULT;
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
				dst.setByte(dstIndex + i, this.input.read());
			}
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
		try {
			this.input.read(dst, dstIndex, length);
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, ByteBuffer dst) {
		try {
			dst.put(ByteStreams.toByteArray(this.input));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read input.", e);
		}
		return this;
	}

	@Override
	public ByteBuf getBytes(int index, OutputStream dst, int length) throws IOException {
		ByteStreams.copy(ByteStreams.limit(this.input, length), dst);
		return this;
	}

	@Override
	public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
		byte[] data = ByteStreams.toByteArray(ByteStreams.limit(this.input, length));

		out.write(ByteBuffer.wrap(data));
		return data.length;
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
		byte[] buffer = new byte[length];
		src.getBytes(srcIndex, buffer);

		try {
			this.output.write(buffer);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
		try {
			this.output.write(src, srcIndex, length);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public ByteBuf setBytes(int index, ByteBuffer src) {
		try {
			WritableByteChannel channel = Channels.newChannel(this.output);

			channel.write(src);
			return this;
		} catch (IOException e) {
			throw new RuntimeException("Cannot write output.", e);
		}
	}

	@Override
	public int setBytes(int index, InputStream in, int length) throws IOException {
		InputStream limit = ByteStreams.limit(in, length);
		ByteStreams.copy(limit, this.output);
		return length - limit.available();
	}

	@Override
	public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		WritableByteChannel channel = Channels.newChannel(this.output);

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

	protected int _getIntLE(int arg0) {
		return 0;
	}

	protected long _getLongLE(int arg0) {
		return 0;
	}

	protected short _getShortLE(int arg0) {
		return 0;
	}

	protected int _getUnsignedMediumLE(int arg0) {
		return 0;
	}

	protected void _setIntLE(int arg0, int arg1) {
	}

	protected void _setLongLE(int arg0, long arg1) {
	}

	protected void _setMediumLE(int arg0, int arg1) {
	}

	protected void _setShortLE(int arg0, int arg1) {
	}

	public int getBytes(int arg0, FileChannel arg1, long arg2, int arg3) throws IOException {
		return 0;
	}

	public int setBytes(int arg0, FileChannel arg1, long arg2, int arg3) {
		return 0;
	}

	public ByteBuf touch() {
		return null;
	}

	public ByteBuf touch(Object arg0) {
		return null;
	}
}
