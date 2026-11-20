package com.comphenix.protocol.injector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
	public void testConcurrentPacketCreation() throws Exception {
		// packets are created from netty and plugin threads; the MAP_CHUNK and SCOREBOARD_OBJECTIVE creators used to
		// rewind one shared buffer for every packet
		List<PacketType> types = List.of(
				PacketType.Play.Server.MAP_CHUNK,
				PacketType.Play.Server.SCOREBOARD_OBJECTIVE,
				PacketType.Play.Server.SPAWN_ENTITY);

		ExecutorService pool = Executors.newFixedThreadPool(8);
		try {
			List<Future<?>> futures = new ArrayList<>();
			for (int thread = 0; thread < 8; thread++) {
				futures.add(pool.submit(() -> {
					for (int i = 0; i < 250; i++) {
						for (PacketType type : types) {
							assertNotNull(StructureCache.newPacket(type));
						}
					}
					return null;
				}));
			}
			for (Future<?> future : futures) {
				future.get(2, TimeUnit.MINUTES);
			}
		} finally {
			pool.shutdownNow();
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
