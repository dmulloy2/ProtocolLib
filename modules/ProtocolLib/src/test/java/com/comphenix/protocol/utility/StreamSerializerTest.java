package com.comphenix.protocol.utility;

import java.io.*;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import net.minecraft.server.v1_13_R1.IntHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import static org.junit.Assert.assertEquals;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PowerMockIgnore({ "org.apache.log4j.*", "org.apache.logging.*", "org.bukkit.craftbukkit.libs.jline.*" })
//@PrepareForTest(CraftItemFactory.class)
public class StreamSerializerTest {

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializeItemMeta();
	}

	@Test
	public void testMinecraftReflection() {
		assertEquals(IntHashMap.class, MinecraftReflection.getIntHashMapClass());
	}

	@Test
	public void testStrings() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		String initial = "Hello - this is a test.";

		// Buffer
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		serializer.serializeString(new DataOutputStream(buffer), initial);

		DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		String deserialized = serializer.deserializeString(input, 50);

		assertEquals(initial, deserialized);
	}

	// For future reference, items are saved in the ChunkRegionLoader and TileEntityChest

	@Test
	public void testCompound() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		NbtCompound initial = NbtFactory.ofCompound("tag");
		initial.put("name", "Ole");
		initial.put("age", 20);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		serializer.serializeCompound(new DataOutputStream(buffer), initial);

		DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer.toByteArray()));
		NbtCompound deserialized = serializer.deserializeCompound(input);

		assertEquals(initial, deserialized);
	}

	@Test
	public void testItems() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		ItemStack initial = new ItemStack(Material.STRING);

		String serialized = serializer.serializeItemStack(initial);
		ItemStack deserialized = serializer.deserializeItemStack(serialized);

		assertEquals(initial, deserialized);
	}

	@Test
	public void testItemMeta() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		ItemStack initial = new ItemStack(Material.BLUE_WOOL, 2);

		ItemMeta meta = initial.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Blue Wool");
		initial.setItemMeta(meta);

		String serialized = serializer.serializeItemStack(initial);
		ItemStack deserialized = serializer.deserializeItemStack(serialized);

		assertEquals(initial, deserialized);
	}
}
