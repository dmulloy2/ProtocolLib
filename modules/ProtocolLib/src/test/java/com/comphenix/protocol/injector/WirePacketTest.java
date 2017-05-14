/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.injector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;

import io.netty.buffer.ByteBuf;

/**
 * @author dmulloy2
 */
public class WirePacketTest {
	
	@BeforeClass
	public static void beforeClass() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testPackets() {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
		packet.getChatTypes().write(0, ChatType.CHAT);

		WirePacket wire = WirePacket.fromPacket(packet);
		WirePacket handle = WirePacket.fromPacket(packet.getHandle());
		assertEquals(wire, handle);
	}

	@Test
	public void testSerialization() {
		int id = 42;
		byte[] array = { 1, 3, 7, 21, 88, 67, 8 };

		WirePacket packet = new WirePacket(id, array);

		ByteBuf buf = packet.serialize();

		int backId = WirePacket.readVarInt(buf);
		byte[] backArray = new byte[buf.readableBytes()];
		buf.readBytes(backArray);

		assertEquals(id, backId);
		assertArrayEquals(array, backArray);
	}
}