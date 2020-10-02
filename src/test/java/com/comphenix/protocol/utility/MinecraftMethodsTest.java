package com.comphenix.protocol.utility;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.comphenix.protocol.injector.server.TemporaryPlayer;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

import java.lang.reflect.Field;

public class MinecraftMethodsTest {

	@BeforeClass
	public static void initializeReflection() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testSendPacketMethods() {
		assertNotNull(MinecraftMethods.getSendPacketMethod());
		assertNotNull(MinecraftMethods.getNetworkManagerHandleMethod());
	}

	private void setNull(final String fieldName) throws NoSuchFieldException, IllegalAccessException {
		Field field = MinecraftMethods.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(null, null);
	}

	@Test
	public void initializePacket() throws NoSuchFieldException, IllegalAccessException {
		setNull("packetReadByteBuf");
		setNull("packetWriteByteBuf");

		assertNotNull(MinecraftMethods.getPacketWriteByteBufMethod());
		assertNotNull(MinecraftMethods.getPacketReadByteBufMethod());
	}
}
