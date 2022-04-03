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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.StreamSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.util.Arrays;

/**
 * A packet represented only by its id and bytes.
 *
 * @author dmulloy2
 */
@SuppressWarnings("deprecation") // yea we need to do that :/
public class WirePacket {

	private final int id;
	private final byte[] bytes;

	/**
	 * Constructs a new WirePacket with a given type and contents
	 *
	 * @param type  Type of the packet
	 * @param bytes Contents of the packet
	 */
	public WirePacket(PacketType type, byte[] bytes) {
		this.id = checkNotNull(type, "type cannot be null").getCurrentId();
		this.bytes = bytes;
	}

	/**
	 * Constructs a new WirePacket with a given id and contents
	 *
	 * @param id    ID of the packet
	 * @param bytes Contents of the packet
	 */
	public WirePacket(int id, byte[] bytes) {
		this.id = id;
		this.bytes = bytes;
	}

	/**
	 * Creates a WirePacket from an existing PacketContainer
	 *
	 * @param packet Existing packet
	 * @return The resulting WirePacket
	 */
	public static WirePacket fromPacket(PacketContainer packet) {
		int id = packet.getType().getCurrentId();
		return new WirePacket(id, bytesFromPacket(packet));
	}

	/**
	 * Creates a byte array from an existing PacketContainer containing all the bytes from that packet
	 *
	 * @param packet Existing packet
	 * @return the byte array
	 */
	public static byte[] bytesFromPacket(PacketContainer packet) {
		checkNotNull(packet, "packet cannot be null!");

		ByteBuf buffer = PacketContainer.createPacketBuffer();
		ByteBuf store = PacketContainer.createPacketBuffer();

		// Read the bytes once
		MethodAccessor write = MinecraftMethods.getPacketWriteByteBufMethod();
		write.invoke(packet.getHandle(), buffer);

		byte[] bytes = StreamSerializer.getDefault().getBytesAndRelease(buffer);

		// Rewrite them to the packet to avoid issues with certain packets
		if (packet.getType() == PacketType.Play.Server.CUSTOM_PAYLOAD
				|| packet.getType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
			// Make a copy of the array before writing
			byte[] ret = Arrays.copyOf(bytes, bytes.length);
			store.writeBytes(bytes);

			MethodAccessor read = MinecraftMethods.getPacketReadByteBufMethod();
			read.invoke(packet.getHandle(), store);

			bytes = ret;
		}

		ReferenceCountUtil.safeRelease(store);
		return bytes;
	}

	/**
	 * Creates a WirePacket from an existing Minecraft packet
	 *
	 * @param packet Existing Minecraft packet
	 * @return The resulting WirePacket
	 * @throws IllegalArgumentException If the packet is null or not a Minecraft packet
	 */
	public static WirePacket fromPacket(Object packet) {
		checkNotNull(packet, "packet cannot be null!");
		checkArgument(MinecraftReflection.isPacketClass(packet), "packet must be a Minecraft packet");

		ByteBuf buffer = PacketContainer.createPacketBuffer();

		MethodAccessor write = MinecraftMethods.getPacketWriteByteBufMethod();
		write.invoke(packet, buffer);

		byte[] bytes = StreamSerializer.getDefault().getBytesAndRelease(buffer);
		int id = PacketType.fromClass(packet.getClass()).getCurrentId();

		return new WirePacket(id, bytes);
	}

	public static void writeVarInt(ByteBuf output, int value) {
		while (true) {
			if ((value & ~0x7F) == 0) {
				output.writeByte(value);
				break;
			} else {
				output.writeByte((value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public static int readVarInt(ByteBuf input) {
		int result = 0;
		for (byte j = 0; j < 5; j++) {
			int nextByte = input.readByte();
			result |= (nextByte & 0x7F) << j * 7;
			if ((nextByte & 0x80) != 128) {
				return result;
			}
		}
		throw new RuntimeException("VarInt is too big");
	}

	/**
	 * Gets this packet's ID
	 *
	 * @return The ID
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * Gets this packet's contents as a byte array
	 *
	 * @return The contents
	 */
	public byte[] getBytes() {
		return this.bytes;
	}

	/**
	 * Writes the id of this packet to a given output
	 *
	 * @param output Output to write to
	 */
	public void writeId(ByteBuf output) {
		writeVarInt(output, this.id);
	}

	/**
	 * Writes the contents of this packet to a given output
	 *
	 * @param output Output to write to
	 */
	public void writeBytes(ByteBuf output) {
		checkNotNull(output, "output cannot be null!");
		output.writeBytes(this.bytes);
	}

	/**
	 * Fully writes the ID and contents of this packet to a given output
	 *
	 * @param output Output to write to
	 */
	public void writeFully(ByteBuf output) {
		this.writeId(output);
		this.writeBytes(output);
	}

	/**
	 * Serializes this packet into a byte buffer
	 *
	 * @return The buffer
	 */
	public ByteBuf serialize() {
		ByteBuf buffer = Unpooled.buffer();
		this.writeFully(buffer);
		return buffer;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj instanceof WirePacket) {
			WirePacket that = (WirePacket) obj;
			return this.id == that.id && Arrays.equals(this.bytes, that.bytes);
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.bytes);
		result = prime * result + this.id;
		return result;
	}

	@Override
	public String toString() {
		return "WirePacket[id=" + this.id + ", bytes=" + Arrays.toString(this.bytes) + "]";
	}
}
