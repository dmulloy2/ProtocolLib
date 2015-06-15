package com.comphenix.protocol.utility;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateAttributes.AttributeSnapshot;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.ServerPing;
import net.minecraft.server.v1_8_R3.ServerPing.ServerData;
import net.minecraft.server.v1_8_R3.ServerPing.ServerPingPlayerSample;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

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
			ClassSource.fromClassLoader()
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

	@Test
	public void testAttributeSnapshot() {
		assertEquals(AttributeSnapshot.class, MinecraftReflection.getAttributeSnapshotClass());
	}

	@Test
	public void testChatComponent() {
		assertEquals(IChatBaseComponent.class, MinecraftReflection.getIChatBaseComponentClass());
	}

	@Test
	public void testChatComponentText() {
		assertEquals(ChatComponentText.class, MinecraftReflection.getChatComponentTextClass());
	}

	@Test
	public void testChatSerializer() {
		assertEquals(ChatSerializer.class, MinecraftReflection.getChatSerializerClass());
	}

	@Test
	public void testChunkCoordIntPair() {
		assertEquals(ChunkCoordIntPair.class, MinecraftReflection.getChunkCoordIntPair());
	}

	@Test
	public void testIBlockData() {
		assertEquals(IBlockData.class, MinecraftReflection.getIBlockDataClass());
	}

	@Test
	public void testPlayerConnection() {
		assertEquals(PlayerConnection.class, MinecraftReflection.getPlayerConnectionClass());
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
	public void testNbtStreamTools() {
		assertEquals(NBTCompressedStreamTools.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
	}

	@Test
	public void testWatchableObject() {
		assertEquals(WatchableObject.class, MinecraftReflection.getWatchableObjectClass());
	}

	@Test
	public void testItemStacks() {
		ItemStack stack = new ItemStack(Material.GOLD_SWORD);
		Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
		assertEquals(stack, MinecraftReflection.getBukkitItemStack(nmsStack));
	}
}