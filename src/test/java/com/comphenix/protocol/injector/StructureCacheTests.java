package com.comphenix.protocol.injector;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
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

	@Test
	public void testCreatorLookupCanReenterCache() {
		assertTrue(StructureCache.canCreateInstance(ReentrantConstructor.class));
		assertNull(ReentrantConstructor.failure);
	}

	private static final class ReentrantConstructor {
		private static final AtomicBoolean ENTERED = new AtomicBoolean();
		private static volatile RuntimeException failure;

		private ReentrantConstructor() {
			if (ENTERED.compareAndSet(false, true)) {
				try {
					StructureCache.canCreateInstance(ReentrantConstructor.class);
				} catch (RuntimeException ex) {
					failure = ex;
				}
			}
		}
	}

	private static final class MutableStaticInstance {
		public static final MutableStaticInstance INSTANCE = new MutableStaticInstance();

		private MutableStaticInstance() {
		}
	}
}
