package com.comphenix.protocol.utility;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.minecraft.server.v1_8_R2.ChatComponentText;
import net.minecraft.server.v1_8_R2.IChatBaseComponent;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.google.common.collect.Maps;

public class MinecraftReflectionTest {
	// Mocking objects
	private interface FakeEntity {
		public Entity getBukkitEntity();
	}

	private interface FakeBlock {
		public Block getBukkitEntity();
	}

	@BeforeClass
	public static void initializeReflection() throws IllegalAccessException {
		BukkitInitialization.initializePackage();

		// Set up a package with no class loader knowledge
		MinecraftReflection.minecraftPackage = new CachedPackage(
			MinecraftReflection.getMinecraftPackage(),
			ClassSource.fromMap(Maps.<String, Class<?>>newHashMap())
		);
	}

	@AfterClass
	public static void undoMocking() {
		// NOP
		MinecraftReflection.minecraftPackage = null;
	}

	@Test
	public void testBukkitMethod() {
		FakeEntity entity = mock(FakeEntity.class);
		FakeBlock block = mock(FakeBlock.class);

		MinecraftReflection.getBukkitEntity(entity);
		MinecraftReflection.getBukkitEntity(block);

		verify(entity, times(1)).getBukkitEntity();
		verify(block, times(1)).getBukkitEntity();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIllegalClass() {
		MinecraftReflection.getBukkitEntity("Hello");
	}

	/* @Test
	public void testNbtStreamTools() {
		assertEquals(NBTCompressedStreamTools.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
	} */

	@Test
	public void testChatComponent() {
		assertEquals(IChatBaseComponent.class, MinecraftReflection.getIChatBaseComponentClass());
	}

	@Test
	public void testChatComponentText() {
		assertEquals(ChatComponentText.class, MinecraftReflection.getChatComponentTextClass());
	}

	/* @Test
	public void testChatSerializer() {
		assertEquals(ChatSerializer.class, MinecraftReflection.getChatSerializerClass());
	}

	@Test
	public void testServerPing() {
		assertEquals(ServerPing.class, MinecraftReflection.getServerPingClass());
	}

	@Test
	public void testServerPingPlayerSample() {
		assertEquals(ServerPingPlayerSample.class, MinecraftReflection.getServerPingPlayerSampleClass());
	}

	@Test
	public void testServerPingServerData() {
		assertEquals(ServerData.class, MinecraftReflection.getServerPingServerDataClass());
	}

	@Test
	public void testChunkCoordIntPair() {
		assertEquals(ChunkCoordIntPair.class, MinecraftReflection.getChunkCoordIntPair());
	}

	@Test
	public void testWatchableObject() {
		assertEquals(WatchableObject.class, MinecraftReflection.getWatchableObjectClass());
	} */
}
