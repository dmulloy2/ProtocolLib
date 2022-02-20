package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.comphenix.protocol.BukkitInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedChatComponentTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testText() {
		WrappedChatComponent test = WrappedChatComponent.fromText("Hello.");
		String json = test.getJson();
		assertNotNull(json);

		WrappedChatComponent clone = WrappedChatComponent.fromJson(json);
		assertEquals(json, clone.getJson());
	}
}
