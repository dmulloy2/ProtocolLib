package com.comphenix.protocol.utility;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemFactory;
import org.bukkit.inventory.ItemStack;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest(CraftItemFactory.class)
public class StreamSerializerTest {
	@BeforeClass
	public static void initializeBukkit() throws IllegalAccessException {
		BukkitInitialization.initializeItemMeta();
	}
	
	@Test
	public void testSerializer() throws IOException {
		ItemStack before = new ItemStack(Material.GOLD_AXE);
		
		StreamSerializer serializer = new StreamSerializer();
		String data = serializer.serializeItemStack(before);
		ItemStack after = serializer.deserializeItemStack(data);
	
		assertEquals(before.getType(), after.getType());
		assertEquals(before.getAmount(), after.getAmount());
	}
	
	@Test
	public void testStrings() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		String initial = "Hello - this is a ∆ÿ≈ test.";
		
		// Buffer
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		serializer.serializeString(new DataOutputStream(buffer), initial);
		
		DataInputStream input = new DataInputStream(
				new ByteArrayInputStream(buffer.toByteArray()));
		String deserialized = serializer.deserializeString(input, 50);
		
		assertEquals(initial, deserialized);
	}
	
	@Test
	public void testCompound() throws IOException {
		StreamSerializer serializer = new StreamSerializer();
		NbtCompound initial = NbtFactory.ofCompound("tag");
		initial.put("name", "Ole");
		initial.put("age", 20);
		
		// Buffer
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		serializer.serializeCompound(new DataOutputStream(buffer), initial);
		
		DataInputStream input = new DataInputStream(
				new ByteArrayInputStream(buffer.toByteArray()));
		NbtCompound deserialized = serializer.deserializeCompound(input);
		
		assertEquals(initial, deserialized);
	}
}
