package com.comphenix.protocol.utility;

import org.junit.BeforeClass;

import com.comphenix.protocol.BukkitInitialization;

public class MinecraftMethodsTest {
	@BeforeClass
	public static void initializeReflection() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}

//	@Test
//	public void testSendPacketMethod() {
//		assertNotNull(MinecraftMethods.getSendPacketMethod());
//	}
}
