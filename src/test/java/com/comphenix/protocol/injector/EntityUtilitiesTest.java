package com.comphenix.protocol.injector;

import static com.comphenix.protocol.utility.TestUtils.setFinalField;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;

public class EntityUtilitiesTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testReflection() {
        CraftWorld bukkit = mock(CraftWorld.class);
        ServerLevel world = mock(ServerLevel.class);
        when(bukkit.getHandle()).thenReturn(world);

        ServerChunkCache provider = mock(ServerChunkCache.class);
		when(world.getChunkSource()).thenReturn(provider);

        ChunkMap chunkMap = mock(ChunkMap.class);
        Field chunkMapField = FuzzyReflection.fromClass(ServerChunkCache.class, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(ChunkMap.class).build());
        setFinalField(provider, chunkMapField, chunkMap);

        CraftEntity bukkitEntity = mock(CraftEntity.class);
        Entity fakeEntity = mock(Entity.class);
        when(fakeEntity.getBukkitEntity()).thenReturn(bukkitEntity);

        EntityTracker tracker = mock(EntityTracker.class);
        Field trackerField = FuzzyReflection.fromClass(EntityTracker.class, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(Entity.class).build());
        setFinalField(tracker, trackerField, fakeEntity);

        Int2ObjectMap<EntityTracker> trackerMap = new Int2ObjectOpenHashMap<>();
        trackerMap.put(1, tracker);
        Field trackedEntitiesField = FuzzyReflection.fromClass(ChunkMap.class, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(Int2ObjectMap.class).build());
        setFinalField(chunkMap, trackedEntitiesField, trackerMap);
    }
}
