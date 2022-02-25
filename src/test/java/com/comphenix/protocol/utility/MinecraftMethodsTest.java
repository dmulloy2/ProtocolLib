package com.comphenix.protocol.utility;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.comphenix.protocol.BukkitInitialization;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MinecraftMethodsTest {

	@BeforeAll
	public static void initializeReflection() {
		BukkitInitialization.initializeAll();
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
		this.setNull("packetReadByteBuf");
		this.setNull("packetWriteByteBuf");

		assertNotNull(MinecraftMethods.getPacketWriteByteBufMethod());
		// TODO it's now a constructor
		// assertNotNull(MinecraftMethods.getPacketReadByteBufMethod());
	}
}
