package com.comphenix.protocol.utility;

import static org.junit.Assert.*;

import net.minecraft.server.v1_7_R1.ChatSerializer;
import net.minecraft.server.v1_7_R1.IChatBaseComponent;
import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_7_R1.ServerPing;
import net.minecraft.server.v1_7_R1.ServerPingPlayerSample;
import net.minecraft.server.v1_7_R1.ServerPingServerData;

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
		assertEquals(IChatBaseComponent.class, MinecraftReflection.getIChatBaseComponentClass());
	}
	
	@Test
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
		assertEquals(ServerPingServerData.class, MinecraftReflection.getServerPingServerDataClass());
	}
}
