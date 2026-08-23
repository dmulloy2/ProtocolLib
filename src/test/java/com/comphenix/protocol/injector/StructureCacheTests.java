package com.comphenix.protocol.injector;

import java.math.BigInteger;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
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

	@Test
	public void testReusesImmutableConstant() {
		assertSame(BigInteger.ZERO, StructureCache.newInstance(BigInteger.class));
	}

	@Test
	public void testDoesNotReuseMutableStaticInstance() {
		assertNotSame(MutableStaticInstance.INSTANCE, StructureCache.newInstance(MutableStaticInstance.class));
	}

	private static final class MutableStaticInstance {
		public static final MutableStaticInstance INSTANCE = new MutableStaticInstance();

		private MutableStaticInstance() {
		}
	}
}
