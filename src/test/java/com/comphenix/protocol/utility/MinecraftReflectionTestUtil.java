package com.comphenix.protocol.utility;

public class MinecraftReflectionTestUtil {

	public static final String PACKAGE_VERSION = "v1_18_R2";
	public static final String NMS = "net.minecraft";
	public static final String OBC = "org.bukkit.craftbukkit." + PACKAGE_VERSION;
	public static final MinecraftVersion CURRENT_VERSION = MinecraftVersion.CAVES_CLIFFS_2;

	public static void init() {
		MinecraftReflection.setMinecraftPackage(NMS, OBC);
		MinecraftVersion.setCurrentVersion(CURRENT_VERSION);
	}
}
