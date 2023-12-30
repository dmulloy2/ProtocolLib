package com.comphenix.protocol.injector.packet;

import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PacketRegistryTests {

	@BeforeAll
	public static void beforeAll() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testRegistryInit() {
		PacketRegistry.reset();
		// completing without exception
		PacketRegistry.initialize();
	}

	@Test
	public void testAllPacketsRegistered() {
		List<PacketType> missing = new ArrayList<>();
		for (PacketType type : PacketType.values()) {
			if (type.isDeprecated()) {
				continue;
			}
			if (!PacketRegistry.tryGetPacketClass(type).isPresent()) {
				missing.add(type);
			}
		}
		assertTrue(missing.isEmpty(), "Missing packets: " + missing);
	}
}
