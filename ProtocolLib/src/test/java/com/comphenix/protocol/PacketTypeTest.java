package com.comphenix.protocol;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;

public class PacketTypeTest {
	@BeforeClass
	public static void initializeReflection() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testFindCurrent() {
		assertEquals(PacketType.Play.Client.STEER_VEHICLE, PacketType.findCurrent(Protocol.PLAY, Sender.CLIENT, 12));
	}
}
