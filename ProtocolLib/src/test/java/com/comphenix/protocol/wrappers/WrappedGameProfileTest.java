package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.google.common.base.Charsets;

public class WrappedGameProfileTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void testSkinUpdate() {
		final UUID uuid = UUID.nameUUIDFromBytes("123".getBytes(Charsets.UTF_8));
		
		assertEquals(null, new WrappedGameProfile((String)null, "Test").getId());
		assertEquals(uuid, new WrappedGameProfile("123", "Test").getUUID());
	}
	
	@SuppressWarnings("deprecation")
	@Test(expected = RuntimeException.class)
	public void testNullFailure() {
		new WrappedGameProfile((String)null, null);
	}
}
