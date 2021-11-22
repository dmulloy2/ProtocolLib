package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

public class ChunkCoordIntPairTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
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

		assertEquals(1, roundtrip.c);
		assertEquals(2, roundtrip.d);
	}
}
