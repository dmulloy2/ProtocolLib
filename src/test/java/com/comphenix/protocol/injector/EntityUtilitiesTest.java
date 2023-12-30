package com.comphenix.protocol.injector;

import java.lang.reflect.Field;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.PlayerChunkMap.EntityTracker;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.comphenix.protocol.utility.TestUtils.setFinalField;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityUtilitiesTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testReflection() {
        CraftWorld bukkit = mock(CraftWorld.class);
        WorldServer world = mock(WorldServer.class);
        when(bukkit.getHandle()).thenReturn(world);

        ChunkProviderServer provider = mock(ChunkProviderServer.class);
		when(world.l()).thenReturn(provider);

        PlayerChunkMap chunkMap = mock(PlayerChunkMap.class);
        Field chunkMapField = FuzzyReflection.fromClass(ChunkProviderServer.class, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(PlayerChunkMap.class).build());
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
        Field trackedEntitiesField = FuzzyReflection.fromClass(PlayerChunkMap.class, true)
                .getField(FuzzyFieldContract.newBuilder().typeExact(Int2ObjectMap.class).build());
        setFinalField(chunkMap, trackedEntitiesField, trackerMap);
    }
}
