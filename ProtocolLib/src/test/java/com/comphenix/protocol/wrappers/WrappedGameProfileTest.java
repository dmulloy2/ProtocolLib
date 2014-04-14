package com.comphenix.protocol.wrappers;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class WrappedGameProfileTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testSkinUpdate() {
		final String nullUUID = "00000000-0000-0000-0000-000000000000";
		assertEquals(null, new WrappedGameProfile((String)null, "Test").getId());
		assertEquals(nullUUID, new WrappedGameProfile("", "Test").getId());
		assertEquals(nullUUID, new WrappedGameProfile("0", "Test").getId());
		assertEquals(nullUUID, new WrappedGameProfile("00-0", "Test").getId());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullFailure() {
		new WrappedGameProfile((String)null, null);
	}
}
