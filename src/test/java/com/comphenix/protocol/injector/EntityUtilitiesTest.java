package com.comphenix.protocol.injector;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.accessors.Accessors;

import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.PlayerChunkMap;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;

import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EntityUtilitiesTest {

	@BeforeClass
	public static void beforeClass() {
		BukkitInitialization.initializeItemMeta();
	}

	@Test
	public void testReflection() {
		CraftWorld bukkit = mock(CraftWorld.class);
		WorldServer world = mock(WorldServer.class);
		when(bukkit.getHandle()).thenReturn(world);

		ChunkProviderServer provider = mock(ChunkProviderServer.class);
		when(world.getChunkProvider()).thenReturn(provider);

		PlayerChunkMap chunkMap = mock(PlayerChunkMap.class);
		Accessors.getFieldAccessor(ChunkProviderServer.class, "playerChunkMap", true).set(provider, chunkMap);

		CraftEntity bukkitEntity = mock(CraftEntity.class);
		Entity fakeEntity = mock(Entity.class);
		when(fakeEntity.getBukkitEntity()).thenReturn(bukkitEntity);

		PlayerChunkMap.EntityTracker tracker = mock(PlayerChunkMap.EntityTracker.class);
		Accessors.getFieldAccessor(PlayerChunkMap.EntityTracker.class, "tracker", true).set(tracker, fakeEntity);

		Int2ObjectMap<PlayerChunkMap.EntityTracker> trackerMap = new Int2ObjectOpenHashMap<>();
		trackerMap.put(1, tracker);
		Accessors.getFieldAccessor(PlayerChunkMap.class, "trackedEntities", true).set(chunkMap, trackerMap);

		assertEquals(bukkitEntity, EntityUtilities.getInstance().getEntityFromID(bukkit, 1));
	}
}
