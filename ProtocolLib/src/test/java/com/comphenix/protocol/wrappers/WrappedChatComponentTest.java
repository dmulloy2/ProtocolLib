package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class WrappedChatComponentTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testText() {
		WrappedChatComponent test = WrappedChatComponent.fromText("Hello.");
		String json = test.getJson();
		
		assertNotNull(json);
	}
}
