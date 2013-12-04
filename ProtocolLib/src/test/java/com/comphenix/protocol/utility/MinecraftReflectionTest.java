package com.comphenix.protocol.utility;

import static org.junit.Assert.*;

import net.minecraft.server.v1_7_R1.NBTCompressedStreamTools;
import net.minecraft.util.com.google.common.collect.Maps;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;

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
	
	@Test
	public void testNbtStreamTools() {
		assertEquals(NBTCompressedStreamTools.class, MinecraftReflection.getNbtCompressedStreamToolsClass());
	}
}
