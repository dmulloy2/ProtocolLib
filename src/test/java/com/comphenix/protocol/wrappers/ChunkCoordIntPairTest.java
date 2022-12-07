package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ChunkCoordIntPairTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void test() {
		net.minecraft.world.level.ChunkCoordIntPair pair = new net.minecraft.world.level.ChunkCoordIntPair(1, 2);
		ChunkCoordIntPair specific = ChunkCoordIntPair.getConverter().getSpecific(pair);

		assertEquals(1, specific.getChunkX());
		assertEquals(2, specific.getChunkZ());

		net.minecraft.world.level.ChunkCoordIntPair roundtrip =
				(net.minecraft.world.level.ChunkCoordIntPair) ChunkCoordIntPair.getConverter().
						getGeneric(specific);

		assertEquals(1, roundtrip.e);
		assertEquals(2, roundtrip.f);
	}
}
