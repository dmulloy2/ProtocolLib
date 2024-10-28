package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChunkCoordIntPairTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void test() {
        net.minecraft.world.level.ChunkPos pair = new net.minecraft.world.level.ChunkPos(1, 2);
        ChunkCoordIntPair specific = ChunkCoordIntPair.getConverter().getSpecific(pair);

        assertEquals(1, specific.getChunkX());
        assertEquals(2, specific.getChunkZ());

        net.minecraft.world.level.ChunkPos roundtrip =
                (net.minecraft.world.level.ChunkPos) ChunkCoordIntPair.getConverter().
                        getGeneric(specific);

        assertEquals(1, roundtrip.x);
        assertEquals(2, roundtrip.z);
    }
}
