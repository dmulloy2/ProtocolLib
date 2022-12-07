/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.injector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author dmulloy2
 */
public class WirePacketTest {

	@BeforeAll
	public static void beforeClass() {
		BukkitInitialization.initializeAll();
	}

	// @Test
	public void testPackets() {
		List<String> failures = new ArrayList<>();

		for (PacketType type : PacketType.values()) {
			if (type.isDeprecated()) {
				continue;
			}

			try {
				PacketContainer packet = new PacketContainer(type);
				WirePacket wire = WirePacket.fromPacket(packet);
				WirePacket handle = WirePacket.fromPacket(packet.getHandle());
				assertEquals(wire, handle);
			} catch (Exception ex) {
				failures.add(type + " :: " + ex.getMessage());
				System.out.println(type);
				ex.printStackTrace();
			}
		}

		assertEquals(failures, new ArrayList<>());
	}

	@Test
	public void testSerialization() {
		int id = 42;
		byte[] array = {1, 3, 7, 21, 88, 67, 8};

		WirePacket packet = new WirePacket(id, array);

		ByteBuf buf = packet.serialize();

		int backId = WirePacket.readVarInt(buf);
		byte[] backArray = new byte[buf.readableBytes()];
		buf.readBytes(backArray);

		assertEquals(id, backId);
		assertArrayEquals(array, backArray);
	}
}