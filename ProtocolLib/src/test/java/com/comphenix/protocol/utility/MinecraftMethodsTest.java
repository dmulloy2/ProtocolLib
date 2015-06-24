package com.comphenix.protocol.utility;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class MinecraftMethodsTest {

	@BeforeClass
	public static void initializeReflection() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testSendPacketMethod() {
		assertNotNull(MinecraftMethods.getSendPacketMethod());
	}
}
