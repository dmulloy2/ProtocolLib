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
		net.minecraft.server.v1_12_R1.ChunkCoordIntPair pair = new net.minecraft.server.v1_12_R1.ChunkCoordIntPair(1, 2);
		ChunkCoordIntPair specific = ChunkCoordIntPair.getConverter().getSpecific(pair);

		assertEquals(1, specific.getChunkX());
		assertEquals(2, specific.getChunkZ());

		net.minecraft.server.v1_12_R1.ChunkCoordIntPair roundtrip =
			(net.minecraft.server.v1_12_R1.ChunkCoordIntPair) ChunkCoordIntPair.getConverter().
			getGeneric(specific);

		assertEquals(1, roundtrip.x);
		assertEquals(2, roundtrip.z);
	}
}
