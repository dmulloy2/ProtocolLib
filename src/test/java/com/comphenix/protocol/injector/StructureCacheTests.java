package com.comphenix.protocol.injector;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructureCacheTests {

	@BeforeAll
	public static void beforeAll() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testInitTrickSerializer() {
		try {
			StructureCache.initTrickDataSerializer();
		} catch (IllegalStateException ex) {
			// no exception or an already injected exception means it succeeded
			assertTrue(ex.getMessage().contains("Cannot inject already loaded type"));
		}
	}
}
