package com.comphenix.protocol.utility;

public class MinecraftReflectionTestUtil {

    public static final String RELEASE_TARGET = "26.1";
    public static final String NMS = "net.minecraft";
    public static final String OBC = "org.bukkit.craftbukkit";

    public static void init() {
        MinecraftReflection.setMinecraftPackage(NMS, OBC);
        MinecraftVersion.setCurrentVersion(MinecraftVersion.LATEST);
    }
}
