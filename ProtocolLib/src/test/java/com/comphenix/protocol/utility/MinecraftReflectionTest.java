package com.comphenix.protocol.utility;

import static org.junit.Assert.*;

import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.google.common.collect.Maps;

public class MinecraftReflectionTest {
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
		MinecraftReflection.minecraftPackage = null;
	}

	@Test
	public void testNbtStreamTools() {
		assertEquals(NBTCompressedStreamTools.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
	}
	
	@Test
	public void testChatComponent() {
		assertEquals(IChatBaseComponent.class, MinecraftReflection.getIChatBaseComponent());
	}
	
	@Test
	public void testChatSerializer() {
		assertEquals(ChatSerializer.class, MinecraftReflection.getChatSerializer());
	}
}
