package com.comphenix.protocol.utility;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.ChunkCoordIntPair;
import net.minecraft.server.v1_13_R2.DataWatcher;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.IChatBaseComponent;
import net.minecraft.server.v1_13_R2.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R2.NBTCompressedStreamTools;
import net.minecraft.server.v1_13_R2.PacketPlayOutUpdateAttributes.AttributeSnapshot;
import net.minecraft.server.v1_13_R2.PlayerConnection;
import net.minecraft.server.v1_13_R2.ServerPing;
import net.minecraft.server.v1_13_R2.ServerPing.ServerData;
import net.minecraft.server.v1_13_R2.ServerPing.ServerPingPlayerSample;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import com.comphenix.protocol.BukkitInitialization;
import com.mojang.authlib.GameProfile;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PowerMockIgnore({ "org.apache.log4j.*", "org.apache.logging.*", "org.bukkit.craftbukkit.libs.jline.*" })
public class MinecraftReflectionTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializeItemMeta();
	}

	// Mocking objects
	private interface FakeEntity {
		Entity getBukkitEntity();
	}

	private interface FakeBlock {
		Block getBukkitEntity();
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
	public void testNullable() {
		assertNull(MinecraftReflection.getNullableNMS("ProtocolLib"));
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
	public void testDataWatcherItem() {
		assertEquals(DataWatcher.Item.class, MinecraftReflection.getDataWatcherItemClass());
	}

	@Test
	public void testItemStacks() {
		ItemStack stack = new ItemStack(Material.GOLDEN_SWORD);
		Object nmsStack = MinecraftReflection.getMinecraftItemStack(stack);
		assertItemsEqual(stack, MinecraftReflection.getBukkitItemStack(nmsStack));

		// The NMS handle for CraftItemStack is null with Material.AIR, make sure it is handled correctly
		assertNotNull(MinecraftReflection.getMinecraftItemStack(CraftItemStack.asCraftCopy(new ItemStack(Material.AIR))));
	}

	@Test
	public void testGameProfile() {
		assertEquals(GameProfile.class, MinecraftReflection.getGameProfileClass());
	}
}
