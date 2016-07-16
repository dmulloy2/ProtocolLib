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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.comphenix.protocol.utility.MinecraftReflection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * A packet represented only by its id and bytes.
 * @author dmulloy2
 */
public class WirePacket {
	private final int id;
	private final byte[] bytes;

	/**
	 * Constructs a new WirePacket with a given type and contents
	 * @param type Type of the packet
	 * @param bytes Contents of the packet
	 */
	public WirePacket(PacketType type, byte[] bytes) {
		this.id = checkNotNull(type, "type cannot be null").getCurrentId();
		this.bytes = bytes;
	}

	/**
	 * Constructs a new WirePacket with a given id and contents
	 * @param id ID of the packet
	 * @param bytes Contents of the packet
	 */
	public WirePacket(int id, byte[] bytes) {
		this.id = id;
		this.bytes = bytes;
	}

	/**
	 * Gets this packet's ID
	 * @return The ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets this packet's contents as a byte array
	 * @return The contents
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Writes the id of this packet to a given output
	 * @param output Output to write to
	 */
	public void writeId(ByteBuf output) {
		writeVarInt(output, id);
	}

	/**
	 * Writes the contents of this packet to a given output
	 * @param output Output to write to
	 */
	public void writeBytes(ByteBuf output) {
		checkNotNull(output, "output cannot be null!");
		output.writeBytes(bytes);
	}

	/**
	 * Fully writes the ID and contents of this packet to a given output
	 * @param output Output to write to
	 */
	public void writeFully(ByteBuf output) {
		writeId(output);
		writeBytes(output);
	}

	/**
	 * Serializes this packet into a byte buffer
	 * @return The buffer
	 */
	public ByteBuf serialize() {
		ByteBuf buffer = Unpooled.buffer();
		writeFully(buffer);
		return buffer;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof WirePacket) {
			WirePacket that = (WirePacket) obj;
			return this.id == that.id &&
					Arrays.equals(this.bytes, that.bytes);
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		result = prime * result + id;
		return result;
	}

	@Override
	public String toString() {
		return "WirePacket[id=" + id + ", bytes=" + Arrays.toString(bytes) + "]";
	}

	private static byte[] getBytes(ByteBuf buffer) {
		byte[] array = new byte[buffer.readableBytes()];
		buffer.readBytes(array);
		return array;
	}

	/**
	 * Creates a WirePacket from an existing PacketContainer
	 * @param packet Existing packet
	 * @return The resulting WirePacket
	 */
	public static WirePacket fromPacket(PacketContainer packet) {
		checkNotNull(packet, "packet cannot be null!");

		int id = packet.getType().getCurrentId();

		ByteBuf buffer = PacketContainer.createPacketBuffer();
		Method write = MinecraftMethods.getPacketWriteByteBufMethod();

		try {
			write.invoke(packet.getHandle(), buffer);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to serialize packet contents.", ex);
		}

		return new WirePacket(id, getBytes(buffer));
	}

	/**
	 * Creates a WirePacket from an existing Minecraft packet
	 * @param packet Existing Minecraft packet
	 * @return The resulting WirePacket
	 * @throws IllegalArgumentException If the packet is null or not a Minecraft packet
	 */
	public static WirePacket fromPacket(Object packet) {
		checkNotNull(packet, "packet cannot be null!");
		checkArgument(MinecraftReflection.isPacketClass(packet), "packet must be a Minecraft packet");

		PacketType type = PacketType.fromClass(packet.getClass());
		int id = type.getCurrentId();

		ByteBuf buffer = PacketContainer.createPacketBuffer();
		Method write = MinecraftMethods.getPacketWriteByteBufMethod();

		try {
			write.invoke(packet, buffer);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to serialize packet contents.", ex);
		}

		return new WirePacket(id, getBytes(buffer));
	}

	public static void writeVarInt(ByteBuf output, int i) {
		checkNotNull(output, "output cannot be null!");

		while ((i & -128) != 0) {
			output.writeByte(i & 127 | 128);
			i >>>= 7;
		}

		output.writeByte(i);
	}

	public static int readVarInt(ByteBuf input) {
		checkNotNull(input, "input cannot be null!");

        int i = 0;
        int j = 0;

        byte b0;

        do {
            b0 = input.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while ((b0 & 128) == 128);

        return i;
	}
}
