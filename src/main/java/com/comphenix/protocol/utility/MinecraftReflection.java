/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.utility;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.wrappers.EnumWrappers;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Methods and constants specifically used in conjuction with reflecting Minecraft object.
 *
 * @author Kristian
 */
public final class MinecraftReflection {

    private static final ClassSource CLASS_SOURCE = ClassSource.fromClassLoader();

    /**
     * Regular expression that matches a canonical Java class.
     */
    private static final String CANONICAL_REGEX = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
    private static final String MINECRAFT_CLASS_NAME_REGEX = "net\\.minecraft\\." + CANONICAL_REGEX;

    /**
     * Represents a regular expression that will match the version string in a package: org.bukkit.craftbukkit.v1_6_R2 ->
     * v1_6_R2
     */
    private static final Pattern PACKAGE_VERSION_MATCHER = Pattern.compile(".*\\.(v\\d+_\\d+_\\w*\\d+)");

    // Cache of getBukkitEntity
    private static final Map<Class<?>, MethodAccessor> BUKKIT_ENTITY_CACHE = new HashMap<>();

    /**
     * The Entity package in Forge 1.5.2
     */
    private static final String FORGE_ENTITY_PACKAGE = "net.minecraft.entity";

    // Package private for the purpose of unit testing
    static CachedPackage minecraftPackage;
    static CachedPackage craftbukkitPackage;
    static CachedPackage libraryPackage;

    /**
     * Regular expression computed dynamically.
     */
    private static String DYNAMIC_PACKAGE_MATCHER = null;
    /**
     * The package name of all the classes that belongs to the native code in Minecraft.
     */
    private static String MINECRAFT_PREFIX_PACKAGE = "net.minecraft.server";
    private static String MINECRAFT_FULL_PACKAGE = null;
    private static String CRAFTBUKKIT_PACKAGE = null;

    // fuzzy matcher for minecraft class objects
    private static AbstractFuzzyMatcher<Class<?>> fuzzyMatcher;
    // The NMS version
    private static String packageVersion;
    // net.minecraft.server
    private static Class<?> itemStackArrayClass;
    // Whether we are using netty
    private static Boolean cachedWatcherObject;

    // ---- ItemStack conversions
    private static Object itemStackAir = null;
    private static Boolean nullEnforced = null;

    private static MethodAccessor asNMSCopy = null;
    private static MethodAccessor asCraftMirror = null;
    private static MethodAccessor isEmpty = null;

    private static Boolean isMojangMapped = null;

    private MinecraftReflection() {
        // No need to make this constructable.
    }

    /**
     * Retrieve a regular expression that can match Minecraft package objects.
     *
     * @return Minecraft package matcher.
     */
    public static String getMinecraftObjectRegex() {
        if (DYNAMIC_PACKAGE_MATCHER == null) {
            getMinecraftPackage();
        }
        return DYNAMIC_PACKAGE_MATCHER;
    }

    /**
     * Retrieve a abstract fuzzy class matcher for Minecraft objects.
     *
     * @return A matcher for Minecraft objects.
     */
    public static AbstractFuzzyMatcher<Class<?>> getMinecraftObjectMatcher() {
        if (fuzzyMatcher == null) {
            fuzzyMatcher = FuzzyMatchers.matchRegex(getMinecraftObjectRegex());
        }
        return fuzzyMatcher;
    }

    /**
     * Retrieve the name of the Minecraft server package.
     *
     * @return Full canonical name of the Minecraft server package.
     */
    public static String getMinecraftPackage() {
        // Speed things up
        if (MINECRAFT_FULL_PACKAGE != null) {
            return MINECRAFT_FULL_PACKAGE;
        }

        try {
            // get the bukkit version we're running on
            Server craftServer = Bukkit.getServer();
            CRAFTBUKKIT_PACKAGE = craftServer.getClass().getPackage().getName();

            // Parse the package version
            Matcher packageMatcher = PACKAGE_VERSION_MATCHER.matcher(CRAFTBUKKIT_PACKAGE);
            if (packageMatcher.matches()) {
                packageVersion = packageMatcher.group(1);
            } else if (!MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) { // ignore version prefix since it's no longer needed
                MinecraftVersion version = new MinecraftVersion(craftServer);

                // Just assume R1 - it's probably fine (warn anyway)
                packageVersion = "v" + version.getMajor() + "_" + version.getMinor() + "_R1";
                ProtocolLogger.log(Level.SEVERE, "Assuming package version: " + packageVersion);
            }

            if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
                // total rework of the NMS structure in 1.17 (at least there's no versioning)
                MINECRAFT_FULL_PACKAGE = MINECRAFT_PREFIX_PACKAGE = "net.minecraft";
                setDynamicPackageMatcher(MINECRAFT_CLASS_NAME_REGEX);
            } else {
                // extract the server version from the return type of "getHandle" in CraftEntity
                Method getHandle = getCraftEntityClass().getMethod("getHandle");
                MINECRAFT_FULL_PACKAGE = getHandle.getReturnType().getPackage().getName();

                // Pretty important invariant
                if (!MINECRAFT_FULL_PACKAGE.startsWith(MINECRAFT_PREFIX_PACKAGE)) {
                    // See if we got the Forge entity package
                    if (MINECRAFT_FULL_PACKAGE.equals(FORGE_ENTITY_PACKAGE)) {
                        // Use the standard NMS versioned package
                        MINECRAFT_FULL_PACKAGE = CachedPackage.combine(MINECRAFT_PREFIX_PACKAGE, packageVersion);
                    } else {
                        // Assume they're the same instead
                        MINECRAFT_PREFIX_PACKAGE = MINECRAFT_FULL_PACKAGE;
                    }

                    // The package is usually flat, so go with that assumption
                    String matcher =
                            (MINECRAFT_PREFIX_PACKAGE.length() > 0 ? Pattern.quote(MINECRAFT_PREFIX_PACKAGE + ".") : "") + CANONICAL_REGEX;

                    // We'll still accept the default location, however
                    setDynamicPackageMatcher("(" + matcher + ")|(" + MINECRAFT_CLASS_NAME_REGEX + ")");

                } else {
                    // Use the standard matcher
                    setDynamicPackageMatcher(MINECRAFT_CLASS_NAME_REGEX);
                }
            }

            return MINECRAFT_FULL_PACKAGE;
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Cannot find getHandle() in CraftEntity", exception);
        }
    }

    /**
     * Retrieve the package version of the underlying CraftBukkit server.
     *
     * @return The craftbukkit package version.
     */
    public static String getPackageVersion() {
        getMinecraftPackage();
        return packageVersion;
    }

    /**
     * Update the dynamic package matcher.
     *
     * @param regex - the Minecraft package regex.
     */
    private static void setDynamicPackageMatcher(String regex) {
        DYNAMIC_PACKAGE_MATCHER = regex;
        // Ensure that the matcher is regenerated
        fuzzyMatcher = null;
    }

    /**
     * Used during debugging and testing.
     *
     * @param minecraftPackage   - the current Minecraft package.
     * @param craftBukkitPackage - the current CraftBukkit package.
     */
    static void setMinecraftPackage(String minecraftPackage, String craftBukkitPackage) {
        MINECRAFT_FULL_PACKAGE = minecraftPackage;
        CRAFTBUKKIT_PACKAGE = craftBukkitPackage;

        // Make sure it exists
        if (getMinecraftServerClass() == null) {
            throw new IllegalArgumentException("Cannot find MinecraftServer for package " + minecraftPackage);
        }

        // Standard matcher
        setDynamicPackageMatcher(MINECRAFT_CLASS_NAME_REGEX);
    }

    /**
     * Retrieve the name of the root CraftBukkit package.
     *
     * @return Full canonical name of the root CraftBukkit package.
     */
    public static String getCraftBukkitPackage() {
        // Ensure it has been initialized
        if (CRAFTBUKKIT_PACKAGE == null) {
            getMinecraftPackage();
        }

        return CRAFTBUKKIT_PACKAGE;
    }

    /**
     * Dynamically retrieve the Bukkit entity from a given entity.
     *
     * @param nmsObject - the NMS entity.
     * @return A bukkit entity.
     * @throws RuntimeException If we were unable to retrieve the Bukkit entity.
     */
    public static Object getBukkitEntity(Object nmsObject) {
        if (nmsObject == null) {
            return null;
        }

        // We will have to do this dynamically, unfortunately
        try {
            Class<?> clazz = nmsObject.getClass();
            MethodAccessor accessor = BUKKIT_ENTITY_CACHE.get(clazz);

            if (accessor == null) {
                MethodAccessor created = Accessors.getMethodAccessor(clazz, "getBukkitEntity");
                accessor = BUKKIT_ENTITY_CACHE.putIfAbsent(clazz, created);

                // We won the race
                if (accessor == null) {
                    accessor = created;
                }
            }

            return accessor.invoke(nmsObject);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot get Bukkit entity from " + nmsObject, e);
        }
    }

    /**
     * Retrieve the Bukkit player from a given PlayerConnection.
     *
     * @param playerConnection The PlayerConnection.
     * @return A bukkit player.
     * @throws RuntimeException If we were unable to retrieve the Bukkit player.
     */
    public static Player getBukkitPlayerFromConnection(Object playerConnection) {
        try {
            return (Player) getBukkitEntity(MinecraftFields.getPlayerFromConnection(playerConnection));
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot get Bukkit entity from connection " + playerConnection, e);
        }
    }

    /**
     * Determine if a given object can be found within the package net.minecraft.server.
     *
     * @param obj - the object to test.
     * @return TRUE if it can, FALSE otherwise.
     */
    public static boolean isMinecraftObject(Object obj) {
        if (obj == null) {
            return false;
        }

        // Doesn't matter if we don't check for the version here
        return obj.getClass().getName().startsWith(MINECRAFT_PREFIX_PACKAGE);
    }

    /**
     * Determine if the given class is found within the package net.minecraft.server, or any equivalent package.
     *
     * @param clazz - the class to test.
     * @return TRUE if it can, FALSE otherwise.
     */
    public static boolean isMinecraftClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be NULL.");
        }

        return getMinecraftObjectMatcher().isMatch(clazz, null);
    }

    /**
     * Determine if a given object is found in net.minecraft.server, and has the given name.
     *
     * @param obj       - the object to test.
     * @param className - the class name to test.
     * @return TRUE if it can, FALSE otherwise.
     */
    public static boolean isMinecraftObject(Object obj, String className) {
        if (obj == null) {
            return false;
        }

        String javaName = obj.getClass().getName();
        return javaName.startsWith(MINECRAFT_PREFIX_PACKAGE) && javaName.endsWith(className);
    }

    /**
     * Determine if a given Object is compatible with a given Class. That is, whether or not the Object is an instance of
     * that Class or one of its subclasses. If either is null, false is returned.
     *
     * @param clazz  Class to test for, may be null
     * @param object the Object to test, may be null
     * @return True if it is, false if not
     * @see Class#isAssignableFrom(Class)
     */
    public static boolean is(Class<?> clazz, Object object) {
        if (clazz == null || object == null) {
            return false;
        }
        
        // check for accidental class objects
        if (object instanceof Class) {
            return clazz.isAssignableFrom((Class<?>) object);
        }

        return clazz.isAssignableFrom(object.getClass());
    }

    /**
     * Equivalent to {@link #is(Class, Object)} but we don't call getClass again
     */
    public static boolean is(Class<?> clazz, Class<?> test) {
        if (clazz == null || test == null) {
            return false;
        }

        return clazz.isAssignableFrom(test);
    }

    /**
     * Determine if a given object is a BlockPosition.
     *
     * @param obj - the object to test.
     * @return TRUE if it can, FALSE otherwise.
     */
    public static boolean isBlockPosition(Object obj) {
        return is(getBlockPositionClass(), obj);
    }

    /**
     * Determine if the given object is an NMS ChunkCoordIntPar.
     *
     * @param obj - the object.
     * @return TRUE if it can, FALSE otherwise.
     */
    public static boolean isChunkCoordIntPair(Object obj) {
        return is(getChunkCoordIntPair(), obj);
    }

    /**
     * Determine if the given object is actually a Minecraft packet.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isPacketClass(Object obj) {
        return is(getPacketClass(), obj);
    }

    public static boolean isPacketClass(Class<?> clazz) {
        return is(getPacketClass(), clazz);
    }

    /**
     * Determine if the given object is assignable to a NetServerHandler (PlayerConnection)
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isServerHandler(Object obj) {
        return is(getPlayerConnectionClass(), obj);
    }

    /**
     * Determine if the given object is actually a Minecraft packet.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isMinecraftEntity(Object obj) {
        return is(getEntityClass(), obj);
    }

    /**
     * Determine if the given object is a NMS ItemStack.
     *
     * @param value - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isItemStack(Object value) {
        return is(getItemStackClass(), value);
    }

    /**
     * Determine if the given object is a CraftPlayer class.
     *
     * @param value - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isCraftPlayer(Object value) {
        return is(getCraftPlayerClass(), value);
    }

    /**
     * Determine if the given object is a Minecraft player entity.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isMinecraftPlayer(Object obj) {
        return is(getEntityPlayerClass(), obj);
    }

    /**
     * Determine if the given object is a data watcher object.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isDataWatcher(Object obj) {
        return is(getDataWatcherClass(), obj);
    }

    /**
     * Determine if the given object is an IntHashMap object.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isIntHashMap(Object obj) {
        return is(getIntHashMapClass(), obj);
    }

    /**
     * Determine if the given object is a CraftItemStack instancey.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isCraftItemStack(Object obj) {
        return is(getCraftItemStackClass(), obj);
    }

    public static boolean isIChatBaseComponent(Class<?> target) {
        return is(getIChatBaseComponentClass(), target);
    }

    /**
     * Retrieve the EntityPlayer (NMS) class.
     *
     * @return The entity class.
     */
    public static Class<?> getEntityPlayerClass() {
        try {
            return getMinecraftClass("server.level.EntityPlayer", "server.level.ServerPlayer", "EntityPlayer");
        } catch (RuntimeException e) {
            try {
                // Grab CraftPlayer's handle
                Method getHandle = FuzzyReflection
                        .fromClass(getCraftBukkitClass("entity.CraftPlayer"))
                        .getMethodByName("getHandle");

                // EntityPlayer is the return type
                return setMinecraftClass("server.level.EntityPlayer", getHandle.getReturnType());
            } catch (IllegalArgumentException e1) {
                throw new RuntimeException("Could not find EntityPlayer class.", e1);
            }
        }
    }

    /**
     * Retrieve the EntityHuman class.
     *
     * @return The entity human class.
     */
    public static Class<?> getEntityHumanClass() {
        // Assume its the direct superclass
        return getEntityPlayerClass().getSuperclass();
    }

    /**
     * Retrieve the GameProfile class.
     *
     * @return The game profile class.
     */
    public static Class<?> getGameProfileClass() {
        return getClass("com.mojang.authlib.GameProfile");
    }

    public static Class<?> getGameProfilePropertyMapClass() {
        return getClass("com.mojang.authlib.properties.PropertyMap");
    }

    /**
     * Retrieve the entity (NMS) class.
     *
     * @return The entity class.
     */
    public static Class<?> getEntityClass() {
        try {
            return getMinecraftClass("world.entity.Entity", "Entity");
        } catch (RuntimeException e) {
            return fallbackMethodReturn("Entity", "entity.CraftEntity", "getHandle");
        }
    }

    /**
     * Retrieve the CraftChatMessage.
     *
     * @return The CraftChatMessage class.
     */
    public static Class<?> getCraftChatMessage() {
        return getCraftBukkitClass("util.CraftChatMessage");
    }

    /**
     * Retrieve the WorldServer (NMS) class.
     *
     * @return The WorldServer class.
     */
    public static Class<?> getWorldServerClass() {
        try {
            return getMinecraftClass("server.level.WorldServer", "server.level.ServerLevel", "WorldServer");
        } catch (RuntimeException e) {
            return fallbackMethodReturn("WorldServer", "CraftWorld", "getHandle");
        }
    }

    /**
     * Retrieve the World (NMS) class.
     *
     * @return The world class.
     */
    public static Class<?> getNmsWorldClass() {
        try {
            return getMinecraftClass("world.level.World", "world.level.Level", "World");
        } catch (RuntimeException e) {
            return setMinecraftClass("world.level.World", getWorldServerClass().getSuperclass());
        }
    }

    /**
     * Fallback on the return value of a named method in order to get a NMS class.
     *
     * @param nmsClass   - the expected name of the Minecraft class.
     * @param craftClass - a CraftBukkit class to look at.
     * @param methodName - the method we will use.
     * @return The return value of this method, which will be saved to the package cache.
     */
    private static Class<?> fallbackMethodReturn(String nmsClass, String craftClass, String methodName) {
        Class<?> result = FuzzyReflection.fromClass(getCraftBukkitClass(craftClass))
                .getMethodByName(methodName)
                .getReturnType();
        // Save the result
        return setMinecraftClass(nmsClass, result);
    }

    /**
     * Retrieve the packet class.
     *
     * @return The packet class.
     */
    public static Class<?> getPacketClass() {
        return getMinecraftClass("network.protocol.Packet", "Packet");
    }

    public static Class<?> getByteBufClass() {
        return getClass("io.netty.buffer.ByteBuf");
    }

    /**
     * Retrieve the EnumProtocol class.
     *
     * @return The Enum protocol class.
     */
    public static Class<?> getEnumProtocolClass() {
        return getMinecraftClass("network.EnumProtocol", "network.ConnectionProtocol", "EnumProtocol");
    }

    /**
     * Retrieve the IChatBaseComponent class.
     *
     * @return The IChatBaseComponent.
     */
    public static Class<?> getIChatBaseComponentClass() {
        return getMinecraftClass("network.chat.IChatBaseComponent", "network.chat.IChatbaseComponent", "network.chat.Component", "IChatBaseComponent");
    }

    public static Optional<Class<?>> getPackedBundlePacketClass() {
        return getOptionalNMS("network.protocol.game.ClientboundBundlePacket", "ClientboundBundlePacket");
    }

    public static boolean isBundlePacket(Class<?> packetClass) {
        return Optionals.Equals(getPackedBundlePacketClass(), packetClass);
    }

    public static boolean isBundleDelimiter(Class<?> packetClass) {
        Class<?> bundleDelimiterClass = getBundleDelimiterClass().orElse(null);
        return bundleDelimiterClass != null && (packetClass.equals(bundleDelimiterClass) || bundleDelimiterClass.isAssignableFrom(packetClass));
    }

    public static Optional<Class<?>> getBundleDelimiterClass() {
        return getOptionalNMS("network.protocol.BundleDelimiterPacket","BundleDelimiterPacket");
    }

    public static Class<?> getIChatBaseComponentArrayClass() {
        return getArrayClass(getIChatBaseComponentClass());
    }

    /**
     * Retrieve the NMS chat component text class.
     *
     * @return The chat component class.
     */
    public static Class<?> getChatComponentTextClass() {
        return getMinecraftClass("network.chat.ChatComponentText", "network.chat.TextComponent", "ChatComponentText");
    }

    /**
     * Attempt to find the ChatSerializer class.
     *
     * @return The serializer class.
     * @throws IllegalStateException If the class could not be found or deduced.
     */
    public static Class<?> getChatSerializerClass() {
        return getMinecraftClass("network.chat.IChatBaseComponent$ChatSerializer",
                "network.chat.Component$Serializer", "network.chat.ComponentSerialization", "IChatBaseComponent$ChatSerializer");
    }

    /**
     * Retrieve the component style serializer class.
     *
     * @return The serializer class.
     */
    public static Class<?> getStyleSerializerClass() {
        return getMinecraftClass(
            "network.chat.Style$Serializer",
            "network.chat.ChatModifier$ChatModifierSerializer",
            "ChatModifier$ChatModifierSerializer");
    }

    /**
     * Retrieve the ServerPing class.
     *
     * @return The ServerPing class.
     */
    public static Class<?> getServerPingClass() {
        return getMinecraftClass("network.protocol.status.ServerPing", "network.protocol.status.ServerStatus", "ServerPing");
    }

    /**
     * Retrieve the ServerPingServerData class.
     *
     * @return The ServerPingServerData class.
     */
    public static Class<?> getServerPingServerDataClass() {
        return getMinecraftClass("network.protocol.status.ServerPing$ServerData", "network.protocol.status.ServerStatus$Version", "ServerPing$ServerData");
    }

    /**
     * Retrieve the ServerPingPlayerSample class.
     *
     * @return The ServerPingPlayerSample class.
     */
    public static Class<?> getServerPingPlayerSampleClass() {
        return getMinecraftClass(
                "network.protocol.status.ServerPing$ServerPingPlayerSample",
                "network.protocol.status.ServerStatus$Players",
                "ServerPing$ServerPingPlayerSample");
    }

    /**
     * Retrieve the MinecraftServer class.
     *
     * @return MinecraftServer class.
     */
    public static Class<?> getMinecraftServerClass() {
        try {
            return getMinecraftClass("server.MinecraftServer", "MinecraftServer");
        } catch (RuntimeException e) {
            // Reset cache and try again
            resetCacheForNMSClass("server.MinecraftServer");

            useFallbackServer();
            return getMinecraftClass("server.MinecraftServer");
        }
    }

    /**
     * Retrieve the NMS statistics class.
     *
     * @return The statistics class.
     */
    public static Class<?> getStatisticClass() {
        return getMinecraftClass("stats.Statistic", "stats.Stat", "Statistic");
    }

    /**
     * Retrieve the NMS statistic list class.
     *
     * @return The statistic list class.
     */
    public static Class<?> getStatisticListClass() {
        return getMinecraftClass("stats.StatisticList", "stats.Stats", "StatisticList");
    }

    /**
     * Retrieve the player list class (or ServerConfigurationManager),
     *
     * @return The player list class.
     */
    public static Class<?> getPlayerListClass() {
        try {
            return getMinecraftClass("server.players.PlayerList", "PlayerList");
        } catch (RuntimeException e) {
            // Reset cache and try again
            resetCacheForNMSClass("server.players.PlayerList");

            useFallbackServer();
            return getMinecraftClass("server.players.PlayerList");
        }
    }

    /**
     * Retrieve the PlayerConnection class.
     *
     * @return The PlayerConnection class.
     */
    public static Class<?> getPlayerConnectionClass() {
        return getMinecraftClass("server.network.PlayerConnection", "server.network.ServerGamePacketListenerImpl", "PlayerConnection");
    }

    /**
     * Retrieve the NetworkManager class.
     *
     * @return The NetworkManager class.
     */
    public static Class<?> getNetworkManagerClass() {
        return getMinecraftClass("network.NetworkManager", "network.Connection", "NetworkManager");
    }

    /**
     * Retrieve the NMS ItemStack class.
     *
     * @return The ItemStack class.
     */
    public static Class<?> getItemStackClass() {
        try {
            return getMinecraftClass("world.item.ItemStack", "ItemStack");
        } catch (RuntimeException e) {
            // Use the handle reference
            return setMinecraftClass("world.item.ItemStack", FuzzyReflection.fromClass(getCraftItemStackClass(), true)
                    .getFieldByName("handle")
                    .getType());
        }
    }

    /**
     * Retrieve the Block (NMS) class.
     *
     * @return Block (NMS) class.
     */
    public static Class<?> getBlockClass() {
        return getMinecraftClass("world.level.block.Block", "Block");
    }

    public static Class<?> getItemClass() {
        return getNullableNMS("world.item.Item", "Item");
    }

    public static Class<?> getFluidTypeClass() {
        return getNullableNMS("world.level.material.FluidType", "world.level.material.Fluid", "FluidType");
    }

    public static Class<?> getParticleTypeClass() {
        return getNullableNMS("core.particles.ParticleType", "core.particles.SimpleParticleType", "ParticleType");
    }

    public static Class<?> getParticleClass() {
        return getNullableNMS("core.particles.Particle");
    }

    /**
     * Retrieve the WorldType class.
     *
     * @return The WorldType class.
     */
    public static Class<?> getWorldTypeClass() {
        return getMinecraftClass("WorldType");
    }

    /**
     * Retrieve the DataWatcher class.
     *
     * @return The DataWatcher class.
     */
    public static Class<?> getDataWatcherClass() {
        return getMinecraftClass("network.syncher.DataWatcher", "network.syncher.SynchedEntityData", "DataWatcher");
    }

    /**
     * Retrieves the BlockPosition class.
     *
     * @return The BlockPosition class.
     */
    public static Class<?> getBlockPositionClass() {
        return getMinecraftClass("core.BlockPosition", "core.BlockPos", "BlockPosition");
    }

    /**
     * Retrieves the Vec3D class.
     *
     * @return The Vec3D class.
     */
    public static Class<?> getVec3DClass() {
        return getMinecraftClass("world.phys.Vec3D", "world.phys.Vec3", "Vec3D");
    }

    /**
     * Retrieve the ChunkCoordIntPair class.
     *
     * @return The ChunkCoordIntPair class.
     */
    public static Class<?> getChunkCoordIntPair() {
        return getMinecraftClass("world.level.ChunkCoordIntPair", "world.level.ChunkPos", "ChunkCoordIntPair");
    }

    /**
     * Retrieve the DataWatcher Item class.
     *
     * @return The class
     */
    public static Class<?> getDataWatcherItemClass() {
        return getMinecraftClass("network.syncher.DataWatcher$Item", "network.syncher.SynchedEntityData$DataItem", "DataWatcher$Item", "DataWatcher$WatchableObject");
    }

    public static Class<?> getDataWatcherObjectClass() {
        return getNullableNMS("network.syncher.DataWatcherObject", "network.syncher.EntityDataAccessor", "DataWatcherObject");
    }

    public static boolean watcherObjectExists() {
        if (cachedWatcherObject == null) {
            cachedWatcherObject = getDataWatcherObjectClass() != null;
        }

        return cachedWatcherObject;
    }

    public static Class<?> getDataWatcherSerializerClass() {
        return getNullableNMS("network.syncher.DataWatcherSerializer", "network.syncher.EntityDataSerializer", "DataWatcherSerializer");
    }

    public static Class<?> getDataWatcherRegistryClass() {
        return getMinecraftClass("network.syncher.DataWatcherRegistry", "network.syncher.EntityDataSerializers", "DataWatcherRegistry");
    }

    public static Class<?> getMinecraftKeyClass() {
        return getMinecraftClass("resources.MinecraftKey", "resources.Identifier", "resources.ResourceLocation", "MinecraftKey");
    }

    public static Class<?> getMobEffectListClass() {
        return getMinecraftClass("world.effect.MobEffectList", "MobEffectList", "world.effect.MobEffect", "world.effect.MobEffects");
    }

    public static Class<?> getSoundEffectClass() {
        return getNullableNMS("sounds.SoundEffect", "sounds.SoundEvent", "SoundEffect", "sounds.SoundEvents");
    }

    /**
     * Retrieve the ServerConnection abstract class.
     *
     * @return The ServerConnection class.
     */
    public static Class<?> getServerConnectionClass() {
        return getMinecraftClass("server.network.ServerConnection", "server.network.ServerConnectionListener", "ServerConnection");
    }

    /**
     * Retrieve the NBT base class.
     *
     * @return The NBT base class.
     */
    public static Class<?> getNBTBaseClass() {
        return getMinecraftClass("nbt.NBTBase", "nbt.Tag", "NBTBase");
    }

    /**
     * Retrieve the NBT read limiter class.
     *
     * @return The NBT read limiter.
     */
    public static Class<?> getNBTReadLimiterClass() {
        return getMinecraftClass("nbt.NBTReadLimiter", "nbt.NbtAccounter", "NBTReadLimiter");
    }

    /**
     * Retrieve the NBT Compound class.
     *
     * @return The NBT Compond class.
     */
    public static Class<?> getNBTCompoundClass() {
        return getMinecraftClass("nbt.NBTTagCompound", "nbt.CompoundTag", "NBTTagCompound");
    }

    /**
     * Retrieve the EntityTracker (NMS) class.
     *
     * @return EntityTracker class.
     */
    public static Class<?> getEntityTrackerClass() {
        return getMinecraftClass("server.level.PlayerChunkMap$EntityTracker", "server.level.ChunkMap$TrackedEntity", "EntityTracker");
    }

    /**
     * Retrieve the attribute snapshot class.
     * <p>
     * This stores the final value of an attribute, along with all the associated computational steps.
     *
     * @return The attribute snapshot class.
     */
    public static Class<?> getAttributeSnapshotClass() {
        return getMinecraftClass(
                "network.protocol.game.PacketPlayOutUpdateAttributes$AttributeSnapshot",
                "network.protocol.game.ClientboundUpdateAttributesPacket$AttributeSnapshot",
                "AttributeSnapshot",
                "PacketPlayOutUpdateAttributes$AttributeSnapshot");
    }

    /**
     * Retrieve the IntHashMap class.
     *
     * @return IntHashMap class.
     */
    public static Class<?> getIntHashMapClass() {
        return getNullableNMS("IntHashMap");
    }

    /**
     * Retrieve the attribute modifier class.
     *
     * @return Attribute modifier class.
     */
    public static Class<?> getAttributeModifierClass() {
        return getMinecraftClass("world.entity.ai.attributes.AttributeModifier", "AttributeModifier");
    }

    /**
     * Retrieve the net.minecraft.server.MobEffect class.
     *
     * @return The mob effect class.
     */
    public static Class<?> getMobEffectClass() {
        return getMinecraftClass("world.effect.MobEffect", "world.effect.MobEffectInstance", "MobEffect");
    }

    /**
     * Retrieve the packet data serializer class that overrides ByteBuf.
     *
     * @return The data serializer class.
     */
    public static Class<?> getPacketDataSerializerClass() {
        return getMinecraftClass("network.PacketDataSerializer", "network.FriendlyByteBuf", "PacketDataSerializer");
    }

    /**
     * Retrieve the NBTCompressedStreamTools class.
     *
     * @return The NBTCompressedStreamTools class.
     */
    public static Class<?> getNbtCompressedStreamToolsClass() {
        return getMinecraftClass("nbt.NBTCompressedStreamTools", "nbt.NbtIo", "NBTCompressedStreamTools");
    }

    /**
     * Retrieve the NMS tile entity class.
     *
     * @return The tile entity class.
     */
    public static Class<?> getTileEntityClass() {
        return getMinecraftClass("world.level.block.entity.TileEntity", "world.level.block.entity.BlockEntity", "TileEntity");
    }

    /**
     * Retrieve the NMS team parameters class.
     *
     * @return The team parameters class.
     */
    public static Optional<Class<?>> getTeamParametersClass() {
        Optional<Class<?>> clazz = getOptionalNMS(
            "network.protocol.game.ClientboundSetPlayerTeamPacket$Parameters",
            "network.protocol.game.PacketPlayOutScoreboardTeam$b"
        );

        if (!clazz.isPresent()) {
            try {
                Class<?> clazz1 = PacketType.Play.Server.SCOREBOARD_TEAM.getPacketClass().getClasses()[0];
                setMinecraftClass("network.protocol.game.ClientboundSetPlayerTeamPacket$Parameters", clazz1);
                return Optional.of(clazz1);
            } catch (Exception ignored) {
            }
        }

        return clazz;
    }

    /**
     * Retrieve the NMS component style class.
     *
     * @return The component style class.
     */
    public static Class<?> getComponentStyleClass() {
        return getMinecraftClass(
            "network.chat.Style",
            "network.chat.ChatModifier",
            "ChatModifier"
        );
    }

    /**
     * Retrieve the NMS NumberFormat class.
     *
     * @return The NumberFormat class.
     */
    public static Optional<Class<?>> getNumberFormatClass() {
        return getOptionalNMS("network.chat.numbers.NumberFormat");
    }

    /**
     * Retrieve the NMS BlankFormat class.
     *
     * @return The FixedFormat class.
     */
    public static Optional<Class<?>> getBlankFormatClass() {
        return getOptionalNMS("network.chat.numbers.BlankFormat");
    }

    /**
     * Retrieve the NMS FixedFormat class.
     *
     * @return The FixedFormat class.
     */
    public static Optional<Class<?>> getFixedFormatClass() {
        return getOptionalNMS("network.chat.numbers.FixedFormat");
    }

    /**
     * Retrieve the NMS StyledFormat class.
     *
     * @return The StyledFormat class.
     */
    public static Optional<Class<?>> getStyledFormatClass() {
        return getOptionalNMS("network.chat.numbers.StyledFormat");
    }

    /**
     * Retrieve the Gson class used by Minecraft.
     *
     * @return The Gson class.
     */
    public static Class<?> getMinecraftGsonClass() {
        return getMinecraftLibraryClass("com.google.gson.Gson");
    }

    /**
     * Retrieve the ItemStack[] class.
     *
     * @return The ItemStack[] class.
     */
    public static Class<?> getItemStackArrayClass() {
        if (itemStackArrayClass == null) {
            itemStackArrayClass = getArrayClass(getItemStackClass());
        }
        return itemStackArrayClass;
    }

    /**
     * Retrieve the array class of a given component type.
     *
     * @param componentType - type of each element in the array.
     * @return The class of the array.
     */
    public static Class<?> getArrayClass(Class<?> componentType) {
        // A bit of a hack, but it works
        return Array.newInstance(componentType, 0).getClass();
    }

    /**
     * Retrieve the CraftItemStack class.
     *
     * @return The CraftItemStack class.
     */
    public static Class<?> getCraftItemStackClass() {
        return getCraftBukkitClass("inventory.CraftItemStack");
    }

    /**
     * Retrieve the CraftPlayer class.
     *
     * @return CraftPlayer class.
     */
    public static Class<?> getCraftPlayerClass() {
        return getCraftBukkitClass("entity.CraftPlayer");
    }

    /**
     * Retrieve the CraftWorld class.
     *
     * @return The CraftWorld class.
     */
    public static Class<?> getCraftWorldClass() {
        return getCraftBukkitClass("CraftWorld");
    }

    /**
     * Retrieve the CraftEntity class.
     *
     * @return CraftEntity class.
     */
    public static Class<?> getCraftEntityClass() {
        return getCraftBukkitClass("entity.CraftEntity");
    }

    /**
     * Retrieve the CraftChatMessage introduced in 1.7.2
     *
     * @return The CraftChatMessage class.
     */
    public static Class<?> getCraftMessageClass() {
        return getCraftBukkitClass("util.CraftChatMessage");
    }

    /**
     * Retrieve the PlayerInfoData class in 1.8.
     *
     * @return The PlayerInfoData class
     */
    public static Class<?> getPlayerInfoDataClass() {
        try {
            return getMinecraftClass(
                "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry",
                "network.protocol.game.PacketPlayOutPlayerInfo$PlayerInfoData",
                "network.protocol.game.ClientboundPlayerInfoPacket$PlayerUpdate",
                "PacketPlayOutPlayerInfo$PlayerInfoData",
                "PlayerInfoData"
            );
        } catch (Exception ignored) {
            return setMinecraftClass(
                    "network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry",
                    PacketType.Play.Server.PLAYER_INFO.getPacketClass().getClasses()[0]);
        }
    }

    static Class<?> getOrInferMinecraftClass(String className, Supplier<Class<?>> supplier, String... aliases) {
        return getOptionalNMS(className, aliases).orElseGet(() -> {
            Class<?> clazz = supplier.get();
            return setMinecraftClass(className, clazz);
        });
    }

    /**
     * Retrieves the entity use action class in 1.17.
     *
     * @return The EntityUseAction class
     */
    public static Class<?> getEnumEntityUseActionClass() {
        return getOrInferMinecraftClass("ServerboundInteractPacket.Action", () -> {
            Class<?> packetClass = PacketType.Play.Client.USE_ENTITY.getPacketClass();
            FuzzyReflection fuzzyReflection = FuzzyReflection.fromClass(packetClass, true);

            Field field = fuzzyReflection.getField(FuzzyFieldContract.newBuilder()
                .banModifier(Modifier.STATIC)
                .typeDerivedOf(Object.class)
                .build());
            if (field != null) {
                return field.getType();
            }

            try {
                return fuzzyReflection.getFieldByType("^.*(EnumEntityUseAction)").getType();
            } catch (IllegalArgumentException ignored) {
                return fuzzyReflection.getFieldByType("^.*(Action)").getType();
            }
        });
    }

    /**
     * Get a method accessor to get the actual use action out of the wrapping EnumEntityUseAction in 1.17.
     *
     * @return a method accessor to get the actual use action
     */
    public static MethodAccessor getEntityUseActionEnumMethodAccessor() {
        FuzzyReflection fuzzy = FuzzyReflection.fromClass(MinecraftReflection.getEnumEntityUseActionClass(), true);
        return Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
                .returnTypeExact(EnumWrappers.getEntityUseActionClass())
                .build()));
    }

    /**
     * Get a field accessor for the hand in the wrapping EnumEntityUseAction in 1.17.
     *
     * @param enumEntityUseAction the object instance of the action, the field is not present in attack.
     * @return a field accessor for the hand in the wrapping EnumEntityUseAction
     */
    public static FieldAccessor getHandEntityUseActionEnumFieldAccessor(Object enumEntityUseAction) {
        FuzzyReflection fuzzy = FuzzyReflection.fromObject(enumEntityUseAction, true);
        return Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract.newBuilder()
                .typeExact(EnumWrappers.getHandClass())
                .build()));
    }

    /**
     * Get a field accessor for the vec3d in the wrapping EnumEntityUseAction in 1.17.
     *
     * @param enumEntityUseAction the object instance of the action, the field is not present in attack.
     * @return a field accessor for the hand in the wrapping EnumEntityUseAction
     */
    public static FieldAccessor getVec3EntityUseActionEnumFieldAccessor(Object enumEntityUseAction) {
        FuzzyReflection fuzzy = FuzzyReflection.fromObject(enumEntityUseAction, true);
        return Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract.newBuilder()
                .typeExact(MinecraftReflection.getVec3DClass())
                .build()));
    }

    /**
     * Determine if the given object is a PlayerInfoData.
     *
     * @param obj - the given object.
     * @return TRUE if it is, FALSE otherwise.
     */
    public static boolean isPlayerInfoData(Object obj) {
        return is(getPlayerInfoDataClass(), obj);
    }

    /**
     * Retrieve the IBlockData class in 1.8.
     *
     * @return The IBlockData class
     */
    public static Class<?> getIBlockDataClass() {
        return getMinecraftClass("world.level.block.state.IBlockData", "world.level.block.state.BlockState", "IBlockData");
    }

    /**
     * Retrieve the MultiBlockChangeInfo class in 1.8.
     *
     * @return The MultiBlockChangeInfo class
     */
    public static Class<?> getMultiBlockChangeInfoClass() {
        return getMinecraftClass("MultiBlockChangeInfo", "PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
    }

    /**
     * Retrieve the MultiBlockChangeInfo array class in 1.8.
     *
     * @return The MultiBlockChangeInfo array class
     */
    public static Class<?> getMultiBlockChangeInfoArrayClass() {
        return getArrayClass(getMultiBlockChangeInfoClass());
    }

    /**
     * Retrieve the PacketPlayOutGameStateChange.a class, aka GameState in 1.16
     *
     * @return The GameState class
     */
    public static Class<?> getGameStateClass() {
        // it's called "a" so there's not a lot we can do to identify it
        Class<?> packetClass = PacketType.Play.Server.GAME_STATE_CHANGE.getPacketClass();
        return packetClass.getClasses()[0];
    }

    public static boolean signUpdateExists() {
        return getNullableNMS("PacketPlayOutUpdateSign") != null;
    }

    public static Class<?> getNonNullListClass() {
        return getMinecraftClass("core.NonNullList", "NonNullList");
    }

    public static MethodAccessor getNonNullListCreateAccessor() {
        try {
            Class<?> nonNullListType = getNonNullListClass();
            Method method = FuzzyReflection.fromClass(nonNullListType).getMethod(FuzzyMethodContract.newBuilder()
                    .returnTypeExact(nonNullListType)
                    .requireModifier(Modifier.STATIC)
                    .parameterCount(0)
                    .build());
            return Accessors.getMethodAccessor(method);
        } catch (Exception ex) {
            return null;
        }
    }

    public static Class<?> getCraftSoundClass() {
        return getCraftBukkitClass("CraftSound");
    }

    public static Class<?> getSectionPositionClass() {
        return getMinecraftClass("core.SectionPosition", "core.SectionPos", "SectionPosition");
    }

    /**
     * Retrieves the Bukkit equivalent of a NMS ItemStack. This method should preserve NBT data and will never return null
     * when supplied with a valid ItemStack. Empty ItemStacks are treated as AIR.
     *
     * @param generic NMS ItemStack
     * @return The Bukkit equivalent
     */
    public static ItemStack getBukkitItemStack(Object generic) {
        if (generic == null) {
            // Convert null to AIR - 1.11 behavior
            return new ItemStack(Material.AIR);
        }

        if (generic instanceof ItemStack) {
            ItemStack bukkit = (ItemStack) generic;

            // They're probably looking for the CraftItemStack
            // If it's one already our work is done
            if (is(getCraftItemStackClass(), generic)) {
                return bukkit;
            }

            // If not, convert it to one
            Object nmsStack = getMinecraftItemStack(bukkit);
            return getBukkitItemStack(nmsStack);
        }

        if (!is(getItemStackClass(), generic)) {
            // We can't do anything with non-ItemStacks
            throw new IllegalArgumentException(generic + " is not an ItemStack!");
        }

        try {
            // Check null enforcement - 1.11 behavior
            if (nullEnforced == null) {
                isEmpty = Accessors.getMethodAccessor(getItemStackClass().getMethod("isEmpty"));
                nullEnforced = true;
            }

            if (nullEnforced) {
                if ((boolean) isEmpty.invoke(generic)) {
                    return new ItemStack(Material.AIR);
                }
            }
        } catch (ReflectiveOperationException ex) {
            nullEnforced = false;
        }

        if (asCraftMirror == null) {
            try {
                Method asMirrorMethod = getCraftItemStackClass().getMethod("asCraftMirror", getItemStackClass());
                asCraftMirror = Accessors.getMethodAccessor(asMirrorMethod);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to obtain CraftItemStack.asCraftMirror", ex);
            }
        }

        try {
            // Convert to a craft mirror to preserve NBT data
            return (ItemStack) asCraftMirror.invoke(nullEnforced, generic);
        } catch (IllegalStateException ex) {
            throw new RuntimeException("Failed to obtain craft mirror of " + generic, ex);
        }
    }

    /**
     * Retrieves the NMS equivalent of a Bukkit ItemStack. This method will never return null and should preserve NBT
     * data. Null inputs are treated as empty (AIR) ItemStacks.
     *
     * @param specific Bukkit ItemStack
     * @return The NMS equivalent
     */
    public static Object getMinecraftItemStack(ItemStack specific) {
        if (asNMSCopy == null) {
            try {
                Method asNmsCopyMethod = getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class);
                asNMSCopy = Accessors.getMethodAccessor(asNmsCopyMethod);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Failed to obtain CraftItemStack.asNMSCopy", ex);
            }
        }

        if (is(getCraftItemStackClass(), specific)) {
            // If it's already a CraftItemStack, use its handle
            Object unwrapped = BukkitUnwrapper.getInstance().unwrapItem(specific);
            if (unwrapped != null) {
                return unwrapped;
            } else {
                if (itemStackAir == null) {
                    // Easiest way to get the Material.AIR ItemStack?
                    itemStackAir = getMinecraftItemStack(new ItemStack(Material.AIR));
                }
                return itemStackAir;
            }
        }

        try {
            // If not, grab a NMS copy
            return asNMSCopy.invoke(null, specific);
        } catch (IllegalStateException ex) {
            throw new RuntimeException("Failed to make NMS copy of " + specific, ex);
        }
    }

    /**
     * Retrieve the given class by name.
     *
     * @param className - name of the class.
     * @return The class.
     */
    private static Class<?> getClass(String className) {
        return getClassSource().loadClass(className)
            .orElseThrow(() -> new RuntimeException("Cannot find class " + className));
    }

    /**
     * Retrieve the class object of a specific CraftBukkit class.
     *
     * @param className - the specific CraftBukkit class.
     * @return Class object.
     * @throws RuntimeException If we are unable to find the given class.
     */
    public static Class<?> getCraftBukkitClass(String className) {
        if (craftbukkitPackage == null) {
            craftbukkitPackage = new CachedPackage(getCraftBukkitPackage(), getClassSource());
        }

        return craftbukkitPackage.getPackageClass(className)
                .orElseThrow(() -> new RuntimeException("Failed to find CraftBukkit class: " + className));
    }

    /**
     * Retrieve the class object of a specific Minecraft class.
     *
     * @param className - the specific Minecraft class.
     * @return Class object.
     * @throws RuntimeException If we are unable to find the given class.
     */
    public static Class<?> getMinecraftClass(String className) {
        if (minecraftPackage == null) {
            minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
        }

        return minecraftPackage.getPackageClass(className)
                .orElseThrow(() -> new RuntimeException("Failed to find NMS class: " + className));
    }

    /**
     * Optionally retrieve the class object of a NMS (net.minecraft.server) class.
     * If the class does not exist, the optional will be empty
     *
     * @param className NMS class name
     * @param aliases Potential aliases
     * @return Optional that may contain the class
     */
    public static Optional<Class<?>> getOptionalNMS(String className, String... aliases) {
        if (minecraftPackage == null) {
            minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
        }

        return minecraftPackage.getPackageClass(className, aliases);
    }

    /**
     * Retrieves a nullable NMS (net.minecraft.server) class. We will attempt to
     * look up the class and its aliases, but will return null if none is found.
     *
     * @param className NMS class name
     * @param aliases Potential aliases
     * @return The class, or null if not found
     */
    public static Class<?> getNullableNMS(String className, String... aliases) {
        return getOptionalNMS(className, aliases).orElse(null);
    }

    private static void resetCacheForNMSClass(String className) {
        if (minecraftPackage == null) {
            minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
        }

        minecraftPackage.removePackageClass(className);
    }

    /**
     * Set the class object for the specific Minecraft class.
     *
     * @param className - name of the Minecraft class.
     * @param clazz     - the new class object.
     * @return The provided clazz object.
     */
    private static Class<?> setMinecraftClass(String className, Class<?> clazz) {
        if (minecraftPackage == null) {
            minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
        }

        minecraftPackage.setPackageClass(className, clazz);
        return clazz;
    }

    /**
     * Retrieve the current class source.
     *
     * @return The class source.
     */
    private static ClassSource getClassSource() {
        return CLASS_SOURCE;
    }

    /**
     * Retrieve the first class that matches a specified Minecraft name.
     *
     * @param className - the specific Minecraft class.
     * @param aliases   - alternative names for this Minecraft class.
     * @return Class object.
     * @throws RuntimeException If we are unable to find any of the given classes.
     */
    public static Class<?> getMinecraftClass(String className, String... aliases) {
        return getOptionalNMS(className, aliases)
            .orElseThrow(() -> new RuntimeException(String.format("Unable to find %s (%s)", className, String.join(", ", aliases))));
    }

    /**
     * Retrieve the class object of a specific Minecraft library class.
     *
     * @param className - the specific library Minecraft class.
     * @return Class object.
     * @throws RuntimeException If we are unable to find the given class.
     */
    public static Class<?> getMinecraftLibraryClass(String className) {
        if (libraryPackage == null) {
            libraryPackage = new CachedPackage("", getClassSource());
        }

        return libraryPackage.getPackageClass(className)
                .orElseThrow(() -> new RuntimeException("Failed to find class: " + className));
    }

    public static Optional<Class<?>> getOptionalLibraryClass(String className) {
        if (libraryPackage == null) {
            libraryPackage = new CachedPackage("", getClassSource());
        }

        return libraryPackage.getPackageClass(className);
    }

    /**
     * Set the class object for the specific library class.
     *
     * @param className - name of the Minecraft library class.
     * @param clazz     - the new class object.
     * @return The provided clazz object.
     */
    private static Class<?> setMinecraftLibraryClass(String className, Class<?> clazz) {
        if (libraryPackage == null) {
            libraryPackage = new CachedPackage("", getClassSource());
        }

        libraryPackage.setPackageClass(className, clazz);
        return clazz;
    }

    /**
     * Dynamically retrieve the NetworkManager name.
     *
     * @return Name of the NetworkManager class.
     */
    public static String getNetworkManagerName() {
        return getNetworkManagerClass().getSimpleName();
    }

    /**
     * Retrieve an instance of the packet data serializer wrapper.
     *
     * @param buffer - the buffer.
     * @return The instance.
     */
    public static Object getPacketDataSerializer(Object buffer) {
        try {
            return MinecraftMethods.getFriendlyBufBufConstructor().apply((ByteBuf) buffer);
        } catch (Exception e) {
            throw new RuntimeException("Cannot construct packet serializer.", e);
        }
    }

    public static Object createPacketDataSerializer(int initialSize) {
        // validate the initial size
        if (initialSize <= 0) {
            initialSize = 256;
        }

        Object buffer = Unpooled.buffer(initialSize);
        return getPacketDataSerializer(buffer);
    }

    public static Class<?> getNbtTagTypes() {
        return getMinecraftClass("nbt.NBTTagTypes", "nbt.TagTypes", "NBTTagTypes");
    }

    public static Class<?> getChatDeserializer() {
        return getMinecraftClass("util.ChatDeserializer", "util.GsonHelper", "ChatDeserializer");
    }

    public static Class<?> getChatMutableComponentClass() {
        return getMinecraftClass("network.chat.IChatMutableComponent", "network.chat.MutableComponent");
    }

    public static Class<?> getDimensionManager() {
        return getMinecraftClass("world.level.dimension.DimensionManager", "world.level.dimension.DimensionType", "DimensionManager");
    }

    public static Class<?> getMerchantRecipeList() {
        return getMinecraftClass("world.item.trading.MerchantRecipeList", "world.item.trading.MerchantOffers", "MerchantRecipeList");
    }

    public static Class<?> getResourceKey() {
        return getMinecraftClass("resources.ResourceKey", "ResourceKey");
    }

    public static Class<?> getEntityTypes() {
        return getMinecraftClass("world.entity.EntityTypes", "world.entity.EntityType", "EntityTypes");
    }

    public static Class<?> getParticleParam() {
        return getMinecraftClass("core.particles.ParticleParam", "core.particles.ParticleOptions", "ParticleParam");
    }

    public static Class<?> getSectionPosition() {
        return getMinecraftClass("core.SectionPosition", "core.SectionPos", "SectionPosition");
    }

    public static Class<?> getChunkProviderServer() {
        return getMinecraftClass("server.level.ChunkProviderServer", "server.level.ServerChunkCache", "ChunkProviderServer");
    }

    public static Class<?> getPlayerChunkMap() {
        return getMinecraftClass("server.level.PlayerChunkMap", "server.level.ChunkMap", "PlayerChunkMap");
    }

    public static Class<?> getIRegistry() {
        return getNullableNMS("core.IRegistry", "core.Registry", "IRegistry");
    }

    public static Class<?> getBuiltInRegistries() {
        return getNullableNMS("core.registries.BuiltInRegistries");
    }

    public static Class<?> getAttributeBase() {
        return getMinecraftClass("world.entity.ai.attributes.AttributeBase", "world.entity.ai.attributes.Attribute", "AttributeBase");
    }

    public static Class<?> getProfilePublicKeyClass() {
        return getMinecraftClass("world.entity.player.ProfilePublicKey");
    }

    public static Class<?> getMessageSignatureClass() {
        return getMinecraftClass("network.chat.MessageSignature");
    }

    public static Class<?> getSaltedSignatureClass() {
        try {
            return getMinecraftClass("SaltedSignature");
        } catch (RuntimeException runtimeException) {
            Class<?> minecraftEncryption = getMinecraftClass("util.MinecraftEncryption", "util.Crypt", "MinecraftEncryption");
            FuzzyMethodContract constructorContract = FuzzyMethodContract.newBuilder()
                    .parameterCount(2)
                    .parameterExactType(Long.TYPE, 0)
                    .parameterExactType(byte[].class, 1)
                    .build();

            for (Class<?> subclass : minecraftEncryption.getClasses()) {
                FuzzyReflection fuzzyReflection = FuzzyReflection.fromClass(subclass, true);
                List<Constructor<?>> constructors = fuzzyReflection.getConstructorList(constructorContract);

                if (!constructors.isEmpty()) {
                    return setMinecraftClass("SaltedSignature", subclass);
                }
            }

            Class<?> messageSigClass = getMinecraftClass("network.chat.MessageSignature", "MessageSignature");
            FuzzyClassContract signatureContract = FuzzyClassContract.newBuilder()
                    .constructor(FuzzyMethodContract.newBuilder()
                            .parameterCount(2)
                            .parameterSuperOf(Long.TYPE, 0)
                            .parameterSuperOf(byte[].class, 1)
                            .build())
                    .build();

            FuzzyFieldContract fuzzyFieldContract = FuzzyFieldContract.newBuilder()
                    .typeMatches(getMinecraftObjectMatcher().and(signatureContract))
                    .build();

            Class<?> signatureClass = FuzzyReflection.fromClass(messageSigClass, true)
                    .getField(fuzzyFieldContract)
                    .getType();
            return setMinecraftClass("SaltedSignature", signatureClass);
        }
    }

    public static Class<?> getProfilePublicKeyDataClass() {
        return getProfilePublicKeyClass().getClasses()[0];
    }

    public static Class<?> getRemoteChatSessionClass() {
        return getMinecraftClass("network.chat.RemoteChatSession");
    }

    public static Class<?> getRemoteChatSessionDataClass() {
        return getRemoteChatSessionClass().getClasses()[0];
    }

    public static Class<?> getFastUtilClass(String className) {
        return getLibraryClass("it.unimi.dsi.fastutil." + className);
    }

    public static Class<?> getInt2ObjectMapClass() {
        return getFastUtilClass("ints.Int2ObjectMap");
    }

    public static Class<?> getIntArrayListClass() {
        return getFastUtilClass("ints.IntArrayList");
    }

    public static Class<?> getLibraryClass(String classname) {
        try {
            return getMinecraftLibraryClass(classname);
        } catch (RuntimeException ex) {
            try {
                Class<?> clazz = getMinecraftLibraryClass("org.bukkit.craftbukkit.libs." + classname);
                setMinecraftLibraryClass(classname, clazz);
                return clazz;
            } catch (Exception ignored) {
                throw ex;
            }
        }
    }

    /**
     * Fallback method that can determine the MinecraftServer and the ServerConfigurationManager.
     */
    private static void useFallbackServer() {
        // Get the first constructor that matches CraftServer(MINECRAFT_OBJECT, ANY)
        Constructor<?> selected = FuzzyReflection.fromClass(getCraftServer())
                .getConstructor(FuzzyMethodContract.newBuilder()
                        .parameterMatches(getMinecraftObjectMatcher(), 0)
                        .parameterCount(2)
                        .build());
        Class<?>[] params = selected.getParameterTypes();

        // Jackpot - two classes at the same time!
        setMinecraftClass("MinecraftServer", params[0]);
        setMinecraftClass("PlayerList", params[1]);
    }

    public static Class<?> getLevelChunkPacketDataClass() {
        return getNullableNMS("network.protocol.game.ClientboundLevelChunkPacketData");
    }

    public static Class<?> getLightUpdatePacketDataClass() {
        return getNullableNMS("network.protocol.game.ClientboundLightUpdatePacketData");
    }

    public static Class<?> getBlockEntityTypeClass() {
        return getMinecraftClass("world.level.block.entity.BlockEntityType", "world.level.block.entity.TileEntityTypes", "TileEntityTypes");
    }

    public static Class<?> getBlockEntityInfoClass() {
        try {
            return getMinecraftClass("BlockEntityInfo");
        } catch (RuntimeException expected) {
            Class<?> infoClass = (Class<?>) ((ParameterizedType) FuzzyReflection.fromClass(getLevelChunkPacketDataClass(),
                    true).getFieldListByType(List.class).get(0).getGenericType()).getActualTypeArguments()[0];

            setMinecraftClass("BlockEntityInfo", infoClass);

            return infoClass;
        }
    }

    public static Class<?> getDynamicOpsClass() {
        return getLibraryClass("com.mojang.serialization.DynamicOps");
    }

    public static Class<?> getJsonOpsClass() {
        return getLibraryClass("com.mojang.serialization.JsonOps");
    }

    public static Class<?> getNbtOpsClass() {
        return getMinecraftClass("nbt.DynamicOpsNBT" /* Spigot Mappings */, "nbt.NbtOps" /* Mojang Mappings */);
    }

    public static Class<?> getCodecClass() {
        return getLibraryClass("com.mojang.serialization.Codec");
    }

    public static Class<?> getHolderClass() {
        return getMinecraftClass("core.Holder");
    }

    public static Class<?> getCraftServer() {
        return getCraftBukkitClass("CraftServer");
    }
 
    public static Class<?> getHolderLookupProviderClass() {
        return getMinecraftClass("core.HolderLookup$a" /* Spigot Mappings */, "core.HolderLookup$Provider" /* Mojang Mappings */);
    }

    public static Class<?> getRegistryAccessClass() {
        return getMinecraftClass("core.IRegistryCustom" /* Spigot Mappings */, "core.RegistryAccess" /* Mojang Mappings */);
    }

    public static Class<?> getProtocolInfoClass() {
        return getMinecraftClass("network.ProtocolInfo");
    }

    public static Class<?> getProtocolInfoUnboundClass() {
        return getMinecraftClass("network.ProtocolInfo$a" /* Spigot Mappings */, "network.ProtocolInfo$Unbound" /* Mojang Mappings */);
    }

    public static Class<?> getPacketFlowClass() {
        return getMinecraftClass("network.protocol.EnumProtocolDirection" /* Spigot Mappings */, "network.protocol.PacketFlow" /* Mojang Mappings */);
    }

    public static Class<?> getStreamCodecClass() {
        return getMinecraftClass("network.codec.StreamCodec");
    }

    public static Optional<Class<?>> getRegistryFriendlyByteBufClass() {
        return getOptionalNMS("network.RegistryFriendlyByteBuf");
    }

    public static boolean isMojangMapped() {
        if (isMojangMapped == null) {
            String nmsWorldName = getWorldServerClass().getName();
            isMojangMapped = nmsWorldName.contains("ServerLevel");
        }

        return isMojangMapped;
    }
}
