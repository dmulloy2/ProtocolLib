package com.comphenix.protocol.utility;

public class MinecraftReflectionTestUtil {

	public static final String PACKAGE_VERSION = "v1_19_R1";
	public static final String NMS = "net.minecraft";
	public static final String OBC = "org.bukkit.craftbukkit." + PACKAGE_VERSION;

	public static void init() {
		MinecraftReflection.setMinecraftPackage(NMS, OBC);
		MinecraftVersion.setCurrentVersion(MinecraftVersion.LATEST);
	}
}
