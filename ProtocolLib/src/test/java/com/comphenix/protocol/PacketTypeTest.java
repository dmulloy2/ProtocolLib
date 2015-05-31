package com.comphenix.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.netty.NettyProtocolRegistry;

public class PacketTypeTest {

	@BeforeClass
	public static void initializeReflection() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testFindCurrent() {
		assertEquals(PacketType.Play.Client.STEER_VEHICLE, PacketType.findCurrent(Protocol.PLAY, Sender.CLIENT, 12));
	}

	@Test
	public void ensureAllExist() {
		NettyProtocolRegistry registry = new NettyProtocolRegistry();
		Map<PacketType, Class<?>> lookup = registry.getPacketTypeLookup();
		for (Entry<PacketType, Class<?>> entry : lookup.entrySet()) {
			PacketType type = entry.getKey();
			Class<?> clazz = entry.getValue();

			if (type.isDynamic()) {
				fail("Packet " + clazz + " does not have a corresponding PacketType!");
			}
		}
	}
}