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

import java.io.DataInputStream;
import java.io.DataOutput;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.ClassAnalyser;
import com.comphenix.protocol.reflect.ClassAnalyser.AsmMethod;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.RemappedClassSource.RemapperUnavaibleException;
import com.comphenix.protocol.utility.RemappedClassSource.RemapperUnavaibleException.Reason;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * Methods and constants specifically used in conjuction with reflecting Minecraft object.
 *
 * @author Kristian
 */
public class MinecraftReflection {
	public static final ReportType REPORT_CANNOT_FIND_MCPC_REMAPPER = new ReportType("Cannot find MCPC/Cauldron remapper.");
	public static final ReportType REPORT_CANNOT_LOAD_CPC_REMAPPER = new ReportType("Unable to load MCPC/Cauldron remapper.");
	public static final ReportType REPORT_NON_CRAFTBUKKIT_LIBRARY_PACKAGE = new ReportType("Cannot find standard Minecraft library location. Assuming MCPC/Cauldron.");

	/**
	 * Regular expression that matches a canonical Java class.
	 */
	private static final String CANONICAL_REGEX = "(\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*\\.)+\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

	/**
	 * Regular expression that matches a Minecraft object.
	 * <p>
	 * Replaced by the method {@link #getMinecraftObjectRegex()}.
	 */
	@Deprecated
	public static final String MINECRAFT_OBJECT = "net\\.minecraft\\." + CANONICAL_REGEX;

	/**
	 * Regular expression computed dynamically.
	 */
	private static String DYNAMIC_PACKAGE_MATCHER = null;

	/**
	 * The Entity package in Forge 1.5.2
	 */
	private static final String FORGE_ENTITY_PACKAGE = "net.minecraft.entity";

	/**
	 * The package name of all the classes that belongs to the native code in Minecraft.
	 */
	private static String MINECRAFT_PREFIX_PACKAGE = "net.minecraft.server";

	/**
	 * Represents a regular expression that will match the version string in a package:
	 *    org.bukkit.craftbukkit.v1_6_R2      ->      v1_6_R2
	 */
	private static final Pattern PACKAGE_VERSION_MATCHER = Pattern.compile(".*\\.(v\\d+_\\d+_\\w*\\d+)");

	private static String MINECRAFT_FULL_PACKAGE = null;
	private static String CRAFTBUKKIT_PACKAGE = null;

	// Package private for the purpose of unit testing
	static CachedPackage minecraftPackage;
	static CachedPackage craftbukkitPackage;
	static CachedPackage libraryPackage;

	// Matches classes
	private static AbstractFuzzyMatcher<Class<?>> fuzzyMatcher;

	// The NMS version
	private static String packageVersion;

	// Item stacks
	private static Method craftNMSMethod;
	private static Method craftBukkitNMS;
	private static Method craftBukkitOBC;
	private static boolean craftItemStackFailed;

	private static Constructor<?> craftNMSConstructor;
	private static Constructor<?> craftBukkitConstructor;

	// net.minecraft.server
	private static Class<?> itemStackArrayClass;

	// Cache of getBukkitEntity
	private static ConcurrentMap<Class<?>, MethodAccessor> getBukkitEntityCache = Maps.newConcurrentMap();

	// The current class source
	private static ClassSource classSource;

	/**
	 * Whether or not we're currently initializing the reflection handler.
	 */
	private static boolean initializing;

	// Whether or not we are using netty
	private static Boolean cachedNetty;

	private MinecraftReflection() {
		// No need to make this constructable.
	}

	/**
	 * Retrieve a regular expression that can match Minecraft package objects.
	 * @return Minecraft package matcher.
	 */
	public static String getMinecraftObjectRegex() {
		if (DYNAMIC_PACKAGE_MATCHER == null)
			getMinecraftPackage();
		return DYNAMIC_PACKAGE_MATCHER;
	}

	/**
	 * Retrieve a abstract fuzzy class matcher for Minecraft objects.
	 * @return A matcher for Minecraft objects.
	 */
	public static AbstractFuzzyMatcher<Class<?>> getMinecraftObjectMatcher() {
		if (fuzzyMatcher == null)
			fuzzyMatcher = FuzzyMatchers.matchRegex(getMinecraftObjectRegex(), 50);
		return fuzzyMatcher;
	}

	/**
	 * Retrieve the name of the Minecraft server package.
	 * @return Full canonical name of the Minecraft server package.
	 */
	public static String getMinecraftPackage() {
		// Speed things up
		if (MINECRAFT_FULL_PACKAGE != null)
			return MINECRAFT_FULL_PACKAGE;
		if (initializing)
			throw new IllegalStateException("Already initializing minecraft package!");
		initializing = true;

		Server craftServer = Bukkit.getServer();

		// This server should have a "getHandle" method that we can use
		if (craftServer != null) {
			try {
				// The return type will tell us the full package, regardless of formating
				Class<?> craftClass = craftServer.getClass();
				CRAFTBUKKIT_PACKAGE = getPackage(craftClass.getCanonicalName());

				// Parse the package version
				Matcher packageMatcher = PACKAGE_VERSION_MATCHER.matcher(CRAFTBUKKIT_PACKAGE);
				if (packageMatcher.matches()) {
					packageVersion = packageMatcher.group(1);
				} else {
					MinecraftVersion version = new MinecraftVersion(craftServer);

					// See if we need a package version
					if (MinecraftVersion.SCARY_UPDATE.compareTo(version) <= 0) {
						 // Just assume R1 - it's probably fine
						packageVersion = "v" + version.getMajor() + "_" + version.getMinor() + "_R1";
						ProtocolLibrary.log(Level.WARNING, "Assuming package version: " + packageVersion);
					}
				}

				// Libigot patch
				handleLibigot();

				// Next, do the same for CraftEntity.getHandle() in order to get the correct Minecraft package
				Class<?> craftEntity = getCraftEntityClass();
				Method getHandle = craftEntity.getMethod("getHandle");

				MINECRAFT_FULL_PACKAGE = getPackage(getHandle.getReturnType().getCanonicalName());

				// Pretty important invariantt
				if (!MINECRAFT_FULL_PACKAGE.startsWith(MINECRAFT_PREFIX_PACKAGE)) {
					// See if we got the Forge entity package
					if (MINECRAFT_FULL_PACKAGE.equals(FORGE_ENTITY_PACKAGE)) {
						// USe the standard NMS versioned package
						MINECRAFT_FULL_PACKAGE = CachedPackage.combine(MINECRAFT_PREFIX_PACKAGE, packageVersion);
					} else {
						// Assume they're the same instead
						MINECRAFT_PREFIX_PACKAGE = MINECRAFT_FULL_PACKAGE;
					}

					// The package is usualy flat, so go with that assumption
					String matcher =
							(MINECRAFT_PREFIX_PACKAGE.length() > 0 ?
									Pattern.quote(MINECRAFT_PREFIX_PACKAGE + ".") : "") + CANONICAL_REGEX;

					// We'll still accept the default location, however
					setDynamicPackageMatcher("(" + matcher + ")|(" + MINECRAFT_OBJECT + ")");

				} else {
					// Use the standard matcher
					setDynamicPackageMatcher(MINECRAFT_OBJECT);
				}

				return MINECRAFT_FULL_PACKAGE;

			} catch (SecurityException e) {
				throw new RuntimeException("Security violation. Cannot get handle method.", e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Cannot find getHandle() method on server. Is this a modified CraftBukkit version?", e);
			} finally {
				initializing = false;
			}

		} else {
			initializing = false;
			throw new IllegalStateException("Could not find Bukkit. Is it running?");
		}
	}

	/**
	 * Retrieve the package version of the underlying CraftBukkit server.
	 * @return The package version, or NULL if not applicable (before 1.4.6).
	 */
	public static String getPackageVersion() {
		getMinecraftPackage();
		return packageVersion;
	}

	/**
	 * Update the dynamic package matcher.
	 * @param regex - the Minecraft package regex.
	 */
	private static void setDynamicPackageMatcher(String regex) {
		DYNAMIC_PACKAGE_MATCHER = regex;

		// Ensure that the matcher is regenerated
		fuzzyMatcher = null;
	}

	// Patch for Libigot
	private static void handleLibigot() {
		try {
			getCraftEntityClass();
		} catch (RuntimeException e) {
			// Try reverting the package to the old format
			craftbukkitPackage = null;
			CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit";

			// This might fail too
			getCraftEntityClass();
		}
	}

	/**
	 * Used during debugging and testing.
	 * @param minecraftPackage - the current Minecraft package.
	 * @param craftBukkitPackage - the current CraftBukkit package.
	 */
	public static void setMinecraftPackage(String minecraftPackage, String craftBukkitPackage) {
		MINECRAFT_FULL_PACKAGE = minecraftPackage;
		CRAFTBUKKIT_PACKAGE = craftBukkitPackage;

		// Make sure it exists
		if (getMinecraftServerClass() == null) {
			throw new IllegalArgumentException("Cannot find MinecraftServer for package " + minecraftPackage);
		}

		// Standard matcher
		setDynamicPackageMatcher(MINECRAFT_OBJECT);
	}

	/**
	 * Retrieve the name of the root CraftBukkit package.
	 * @return Full canonical name of the root CraftBukkit package.
	 */
	public static String getCraftBukkitPackage() {
		// Ensure it has been initialized
		if (CRAFTBUKKIT_PACKAGE == null)
			getMinecraftPackage();
		return CRAFTBUKKIT_PACKAGE;
	}

	/**
	 * Retrieve the package name from a given canonical Java class name.
	 * @param fullName - full Java class name.
	 * @return The package name.
	 */
	private static String getPackage(String fullName) {
		int index = fullName.lastIndexOf(".");

		if (index > 0)
			return fullName.substring(0, index);
		else
			return ""; // Default package
	}

	/**
	 * Dynamically retrieve the Bukkit entity from a given entity.
	 * @param nmsObject - the NMS entity.
	 * @return A bukkit entity.
	 * @throws RuntimeException If we were unable to retrieve the Bukkit entity.
	 */
	public static Object getBukkitEntity(Object nmsObject) {
		if (nmsObject == null)
			return null;

		// We will have to do this dynamically, unfortunately
		try {
			Class<?> clazz = nmsObject.getClass();
			MethodAccessor accessor = getBukkitEntityCache.get(clazz);

			if (accessor == null) {
				MethodAccessor created = Accessors.getMethodAccessor(clazz, "getBukkitEntity");
				accessor = getBukkitEntityCache.putIfAbsent(clazz, created);

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
	 * Determine if a given object can be found within the package net.minecraft.server.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isMinecraftObject(@Nonnull Object obj) {
		if (obj == null)
			return false;

		// Doesn't matter if we don't check for the version here
		return obj.getClass().getName().startsWith(MINECRAFT_PREFIX_PACKAGE);
	}

	/**
	 * Determine if the given class is found within the package net.minecraft.server, or any equivalent package.
	 * @param clazz - the class to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isMinecraftClass(@Nonnull Class<?> clazz) {
		if (clazz == null)
			throw new IllegalArgumentException("clazz cannot be NULL.");

		return getMinecraftObjectMatcher().isMatch(clazz, null);
	}

	/**
	 * Determine if a given object is found in net.minecraft.server, and has the given name.
	 * @param obj - the object to test.
	 * @param className - the class name to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isMinecraftObject(@Nonnull Object obj, String className) {
		if (obj == null)
			return false;

		String javaName = obj.getClass().getName();
		return javaName.startsWith(MINECRAFT_PREFIX_PACKAGE) && javaName.endsWith(className);
 	}

	/**
	 * Determine if a given object is a ChunkPosition.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isChunkPosition(Object obj) {
		Class<?> chunkPosition = getChunkPositionClass();
		return obj != null && chunkPosition != null && chunkPosition.isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if a given object is a BlockPosition.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isBlockPosition(Object obj) {
		Class<?> blockPosition = getBlockPositionClass();
		return obj != null && blockPosition != null && blockPosition.isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is an NMS ChunkCoordIntPar.
	 * @param obj - the object.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isChunkCoordIntPair(Object obj) {
		return obj != null && getChunkCoordIntPair().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if a given object is a ChunkCoordinate.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isChunkCoordinates(Object obj) {
		Class<?> chunkCoordinates = getChunkCoordinatesClass();
		return obj != null && chunkCoordinates != null && chunkCoordinates.isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is actually a Minecraft packet.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isPacketClass(Object obj) {
		return obj != null && getPacketClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a NetLoginHandler (PendingConnection)
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isLoginHandler(Object obj) {
		return obj != null && getNetLoginHandlerClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is assignable to a NetServerHandler (PlayerConnection)
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isServerHandler(Object obj) {
		return obj != null && getPlayerConnectionClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is actually a Minecraft packet.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isMinecraftEntity(Object obj) {
		return obj != null && getEntityClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a NMS ItemStack.
	 * @param value - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isItemStack(Object value) {
		return value != null && getItemStackClass().isAssignableFrom(value.getClass());
	}

	/**
	 * Determine if the given object is a CraftPlayer class.
	 * @param value - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isCraftPlayer(Object value) {
		return value != null && getCraftPlayerClass().isAssignableFrom(value.getClass());
	}

	/**
	 * Determine if the given object is a Minecraft player entity.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isMinecraftPlayer(Object obj) {
		return obj != null && getEntityPlayerClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a watchable object.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isWatchableObject(Object obj) {
		return obj != null && getWatchableObjectClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a data watcher object.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isDataWatcher(Object obj) {
		return obj != null && getDataWatcherClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is an IntHashMap object.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isIntHashMap(Object obj) {
		return obj != null && getIntHashMapClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a CraftItemStack instancey.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isCraftItemStack(Object obj) {
		return obj != null && getCraftItemStackClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Retrieve the EntityPlayer (NMS) class.
	 * @return The entity class.
	 */
	public static Class<?> getEntityPlayerClass() {
		try {
			return getMinecraftClass("EntityPlayer");
		} catch (RuntimeException e) {
			try {
				// Grab CraftPlayer's handle
				Method getHandle = FuzzyReflection
						.fromClass(getCraftBukkitClass("entity.CraftPlayer"))
						.getMethodByName("getHandle");

				// EntityPlayer is the return type
				return setMinecraftClass("EntityPlayer", getHandle.getReturnType());
			} catch (IllegalArgumentException e1) {
				throw new RuntimeException("Could not find EntityPlayer class.", e1);
			}
		}
	}

	/**
	 * Retrieve the EntityHuman class.
	 * @return The entity human class.
	 */
	public static Class<?> getEntityHumanClass() {
		// Assume its the direct superclass
		return getEntityPlayerClass().getSuperclass();
	}

	/**
	 * Retrieve the GameProfile class in 1.7.2 and later.
	 * 
	 * @return The game profile class.
	 * @throws IllegalStateException If we are running 1.6.4 or earlier.
	 */
	public static Class<?> getGameProfileClass() {
		if (!isUsingNetty())
			throw new IllegalStateException("GameProfile does not exist in version 1.6.4 and earlier.");

		try {
			return getClass("com.mojang.authlib.GameProfile");
		} catch (Throwable ex) {
			try {
				return getClass("net.minecraft.util.com.mojang.authlib.GameProfile");
			} catch (Throwable ex1) {
				FuzzyReflection reflection = FuzzyReflection.fromClass(PacketType.Login.Client.START.getPacketClass(), true);
				FuzzyFieldContract contract = FuzzyFieldContract.newBuilder()
						.banModifier(Modifier.STATIC)
						.typeMatches(FuzzyMatchers.matchRegex("(.*)(GameProfile)", 1))
						.build();
				return reflection.getField(contract).getType();
			}
		}
	}

	/**
	 * Retrieve the entity (NMS) class.
	 * @return The entity class.
	 */
	public static Class<?> getEntityClass() {
		try {
			return getMinecraftClass("Entity");
		} catch (RuntimeException e) {
			return fallbackMethodReturn("Entity", "entity.CraftEntity", "getHandle");
		}
	}

	/**
	 * Retrieve the CraftChatMessage in Minecraft 1.7.2.
	 * @return The CraftChatMessage class.
	 */
	public static Class<?> getCraftChatMessage() {
		return getCraftBukkitClass("util.CraftChatMessage");
	}

	/**
	 * Retrieve the WorldServer (NMS) class.
	 * @return The WorldServer class.
	 */
	public static Class<?> getWorldServerClass() {
		try {
			return getMinecraftClass("WorldServer");
		} catch (RuntimeException e) {
			return fallbackMethodReturn("WorldServer", "CraftWorld", "getHandle");
		}
	}

	/**
	 * Retrieve the World (NMS) class.
	 * @return The world class.
	 */
	public static Class<?> getNmsWorldClass() {
		try {
			return getMinecraftClass("World");
		} catch (RuntimeException e) {
			return setMinecraftClass("World", getWorldServerClass().getSuperclass());
		}
	}

	/**
	 * Fallback on the return value of a named method in order to get a NMS class.
	 * @param nmsClass - the expected name of the Minecraft class.
	 * @param craftClass - a CraftBukkit class to look at.
	 * @param methodName - the method we will use.
	 * @return The return value of this method, which will be saved to the package cache.
	 */
	private static Class<?> fallbackMethodReturn(String nmsClass, String craftClass, String methodName) {
		Class<?> result = FuzzyReflection.fromClass(getCraftBukkitClass(craftClass)).
			    			getMethodByName(methodName).getReturnType();

		// Save the result
		return setMinecraftClass(nmsClass, result);
	}

	/**
	 * Retrieve the packet class.
	 * @return The packet class.
	 */
	public static Class<?> getPacketClass() {
		try {
			return getMinecraftClass("Packet");
		} catch (RuntimeException e) {
			FuzzyClassContract paketContract = null;

			// What kind of class we're looking for (sanity check)
			if (isUsingNetty()) {
				paketContract = FuzzyClassContract.newBuilder().
						method(FuzzyMethodContract.newBuilder().
							    parameterDerivedOf(getByteBufClass()).
							    returnTypeVoid()).
						method(FuzzyMethodContract.newBuilder().
							    parameterDerivedOf(getByteBufClass(), 0).
							    parameterExactType(byte[].class, 1).
							    returnTypeVoid()).
						build();
			} else {
				paketContract = FuzzyClassContract.newBuilder().
					field(FuzzyFieldContract.newBuilder().
							typeDerivedOf(Map.class).
							requireModifier(Modifier.STATIC)).
					field(FuzzyFieldContract.newBuilder().
							typeDerivedOf(Set.class).
							requireModifier(Modifier.STATIC)).
					method(FuzzyMethodContract.newBuilder().
						    parameterSuperOf(DataInputStream.class).
						    returnTypeVoid()).
					build();
			}

			// Select a method with one Minecraft object parameter
			Method selected = FuzzyReflection.fromClass(getPlayerConnectionClass()).
					getMethod(FuzzyMethodContract.newBuilder().
							parameterMatches(paketContract, 0).
							parameterCount(1).
							build()
					);

			// Save and return
			Class<?> clazz = getTopmostClass(selected.getParameterTypes()[0]);
			return setMinecraftClass("Packet", clazz);
		}
	}

	public static Class<?> getByteBufClass() {
		try {
			return getClass("io.netty.buffer.ByteBuf");
		} catch (Throwable ex) {
			return getClass("net.minecraft.util.io.netty.buffer.ByteBuf");
		}
	}

	/**
	 * Retrieve the EnumProtocol class in 1.7.2.
	 * @return The Enum protocol class.
	 */
	public static Class<?> getEnumProtocolClass() {
		try {
			return getMinecraftClass("EnumProtocol");
		} catch (RuntimeException e) {
			Method protocolMethod = FuzzyReflection.fromClass(getNetworkManagerClass()).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(1).
					parameterDerivedOf(Enum.class, 0).
					build()
			);
			return setMinecraftClass("EnumProtocol", protocolMethod.getParameterTypes()[0]);
		}
	}

	/**
	 * Retrieve the IChatBaseComponent class.
	 * @return The IChatBaseComponent.
	 */
	public static Class<?> getIChatBaseComponentClass() {
		try {
			return getMinecraftClass("IChatBaseComponent");
		} catch (RuntimeException e) {
			return setMinecraftClass("IChatBaseComponent",
				Accessors.getMethodAccessor(getCraftChatMessage(), "fromString", String.class).
					getMethod().getReturnType().getComponentType()
			);
		}
	}

	public static Class<?> getIChatBaseComponentArrayClass() {
		return getArrayClass(getIChatBaseComponentClass());
	}

	/**
	 * Retrieve the NMS chat component text class.
	 * @return The chat component class.
	 */
	public static Class<?> getChatComponentTextClass() {
		try {
			return getMinecraftClass("ChatComponentText");
		} catch (RuntimeException e) {
			try {
				Method getScoreboardDisplayName = FuzzyReflection.fromClass(getEntityClass()).
					getMethodByParameters("getScoreboardDisplayName", getIChatBaseComponentClass(), new Class<?>[0]);
				Class<?> baseClass = getIChatBaseComponentClass();

				for (AsmMethod method : ClassAnalyser.getDefault().getMethodCalls(getScoreboardDisplayName)) {
					Class<?> owner = method.getOwnerClass();

					if (isMinecraftClass(owner) && baseClass.isAssignableFrom(owner)) {
						return setMinecraftClass("ChatComponentText", owner);
					}
				}
			} catch (Exception e1) {
				throw new IllegalStateException("Cannot find ChatComponentText class.", e);
			}
		}
		throw new IllegalStateException("Cannot find ChatComponentText class.");
	}

	/**
	 * Attempt to find the ChatSerializer class.
	 * @return The serializer class.
	 * @throws IllegalStateException If the class could not be found or deduced.
	 */
	public static Class<?> getChatSerializerClass() {
		try {
			return getMinecraftClass("ChatSerializer", "IChatBaseComponent$ChatSerializer");
		} catch (RuntimeException e) {
			// TODO: Figure out a functional fallback
			throw new IllegalStateException("Could not find ChatSerializer class.", e);
		}
	}

	/**
	 * Retrieve the ServerPing class in Minecraft 1.7.2.
	 * @return The ServerPing class.
	 */
	public static Class<?> getServerPingClass() {
		if (!isUsingNetty())
			throw new IllegalStateException("ServerPing is only supported in 1.7.2.");

		try {
			return getMinecraftClass("ServerPing");
		} catch (RuntimeException e) {
			Class<?> statusServerInfo = PacketType.Status.Server.OUT_SERVER_INFO.getPacketClass();

			// Find a server ping object
			AbstractFuzzyMatcher<Class<?>> serverPingContract = FuzzyClassContract.newBuilder().
				field(FuzzyFieldContract.newBuilder().typeExact(String.class).build()).
				field(FuzzyFieldContract.newBuilder().typeDerivedOf(getIChatBaseComponentClass()).build()).
				build().
			and(getMinecraftObjectMatcher());

			return setMinecraftClass("ServerPing",
				FuzzyReflection.fromClass(statusServerInfo, true).
					getField(FuzzyFieldContract.matchType(serverPingContract)).getType());
		}
	}

	/**
	 * Retrieve the ServerPingServerData class in Minecraft 1.7.2.
	 * @return The ServerPingServerData class.
	 */
	public static Class<?> getServerPingServerDataClass() {
		if (!isUsingNetty())
			throw new IllegalStateException("ServerPingServerData is only supported in 1.7.2.");

		try {
			return getMinecraftClass("ServerPingServerData", "ServerPing$ServerData");
		} catch (RuntimeException e) {
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(getServerPingClass(), true);
			return setMinecraftClass("ServerPingServerData", fuzzy.getFieldByType("(.*)(ServerData)(.*)").getType());
		}
	}

	/**
	 * Retrieve the ServerPingPlayerSample class in Minecraft 1.7.2.
	 * @return The ServerPingPlayerSample class.
	 */
	public static Class<?> getServerPingPlayerSampleClass() {
		if (!isUsingNetty())
			throw new IllegalStateException("ServerPingPlayerSample is only supported in 1.7.2.");

		try {
			return getMinecraftClass("ServerPingPlayerSample", "ServerPing$ServerPingPlayerSample");
		} catch (RuntimeException e) {
			Class<?> serverPing = getServerPingClass();

			// Find a server ping object
			AbstractFuzzyMatcher<Class<?>> serverPlayerContract = FuzzyClassContract.newBuilder().
				constructor(FuzzyMethodContract.newBuilder().parameterExactArray(int.class, int.class)).
				field(FuzzyFieldContract.newBuilder().typeExact(getArrayClass(getGameProfileClass()))).
				build().
			and(getMinecraftObjectMatcher());

			return setMinecraftClass("ServerPingPlayerSample", getTypeFromField(serverPing, serverPlayerContract));
		}
	}

	/**
	 * Retrieve the type of the field whose type matches.
	 * @param clazz - the declaring type.
	 * @param fieldTypeMatcher - the field type matcher.
	 * @return The type of the field.
	 */
	private static Class<?> getTypeFromField(Class<?> clazz, AbstractFuzzyMatcher<Class<?>> fieldTypeMatcher) {
		final FuzzyFieldContract fieldMatcher = FuzzyFieldContract.matchType(fieldTypeMatcher);

		return FuzzyReflection.fromClass(clazz, true).
			getField(fieldMatcher).getType();
	}

	/**
	 * Determine if this Minecraft version is using Netty.
	 * <p>
	 * Spigot is ignored in this consideration.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public static boolean isUsingNetty() {
		if (cachedNetty == null) {
			try {
				cachedNetty = getEnumProtocolClass() != null;
			} catch (RuntimeException e) {
				cachedNetty = false;
			}
		}
		return cachedNetty;
	}

	/**
	 * Retrieve the least derived class, except Object.
	 * @return Least derived super class.
	 */
	private static Class<?> getTopmostClass(Class<?> clazz) {
		while (true) {
			Class<?> superClass = clazz.getSuperclass();

			if (superClass == Object.class || superClass == null)
				return clazz;
			else
				clazz = superClass;
		}
	}

	/**
	 * Retrieve the MinecraftServer class.
	 * @return MinecraftServer class.
	 */
	public static Class<?> getMinecraftServerClass() {
		try {
			return getMinecraftClass("MinecraftServer");
		} catch (RuntimeException e) {
			useFallbackServer();
			return getMinecraftClass("MinecraftServer");
		}
	}

	/**
	 * Retrieve the NMS statistics class.
	 * @return The statistics class.
	 */
	public static Class<?> getStatisticClass() {
		// TODO: Implement fallback
		return getMinecraftClass("Statistic");
	}

	/**
	 * Retrieve the NMS statistic list class.
	 * @return The statistic list class.
	 */
	public static Class<?> getStatisticListClass() {
		// TODO: Implement fallback
		return getMinecraftClass("StatisticList");
	}

	/**
	 * Fallback method that can determine the MinecraftServer and the ServerConfigurationManager.
	 */
	private static void useFallbackServer() {
		// Get the first constructor that matches CraftServer(MINECRAFT_OBJECT, ANY)
		Constructor<?> selected = FuzzyReflection.fromClass(getCraftBukkitClass("CraftServer")).
				getConstructor(FuzzyMethodContract.newBuilder().
						parameterMatches(getMinecraftObjectMatcher(), 0).
						parameterCount(2).
						build()
				);
		Class<?>[] params = selected.getParameterTypes();

		// Jackpot - two classes at the same time!
		setMinecraftClass("MinecraftServer", params[0]);
		setMinecraftClass("ServerConfigurationManager", params[1]);
	}

	/**
	 * Retrieve the player list class (or ServerConfigurationManager),
	 * @return The player list class.
	 */
	public static Class<?> getPlayerListClass() {
		try {
			return getMinecraftClass("ServerConfigurationManager", "PlayerList");
		} catch (RuntimeException e) {
			// Try again
			useFallbackServer();
			return getMinecraftClass("ServerConfigurationManager");
		}
	}

	/**
	 * Retrieve the NetLoginHandler class (or PendingConnection)
	 * @return The NetLoginHandler class.
	 */
	public static Class<?> getNetLoginHandlerClass() {
		try {
			return getMinecraftClass("NetLoginHandler", "PendingConnection");
		} catch (RuntimeException e) {
			Method selected = FuzzyReflection.fromClass(getPlayerListClass()).
					getMethod(FuzzyMethodContract.newBuilder().
					   parameterMatches(
							   FuzzyMatchers.matchExact(getEntityPlayerClass()).inverted(), 0
					   ).
					   parameterExactType(String.class, 1).
					   parameterExactType(String.class, 2).
					   build()
					);

			// Save the pending connection reference
			return setMinecraftClass("NetLoginHandler", selected.getParameterTypes()[0]);
		}
	}

	/**
	 * Retrieve the PlayerConnection class (or NetServerHandler)
	 * @return The PlayerConnection class.
	 */
	public static Class<?> getPlayerConnectionClass() {
		try  {
			return getMinecraftClass("PlayerConnection", "NetServerHandler");
		} catch (RuntimeException e) {
			try {
				// Use the player connection field
				return setMinecraftClass("PlayerConnection",
					FuzzyReflection.fromClass(getEntityPlayerClass()).
					getFieldByType("playerConnection", getNetHandlerClass()).getType()
				);

			} catch (RuntimeException e1) {
				// Okay, this must be on 1.7.2
				Class<?> playerClass = getEntityPlayerClass();

				FuzzyClassContract playerConnection = FuzzyClassContract.newBuilder().
					field(FuzzyFieldContract.newBuilder().typeExact(playerClass).build()).
					constructor(FuzzyMethodContract.newBuilder().
							parameterCount(3).
							parameterSuperOf(getMinecraftServerClass(), 0).
							parameterSuperOf(getEntityPlayerClass(), 2).
							build()
					).
					method(FuzzyMethodContract.newBuilder().
							parameterCount(1).
							parameterExactType(String.class).
							build()
					).
					build();

				// If not, use duck typing
				Class<?> fieldType = FuzzyReflection.fromClass(getEntityPlayerClass(), true).getField(
					FuzzyFieldContract.newBuilder().typeMatches(playerConnection).build()
				).getType();

				return setMinecraftClass("PlayerConnection", fieldType);
			}
		}
	}

	/**
	 * Retrieve the NetworkManager class or its interface.
	 * @return The NetworkManager class or its interface.
	 */
	public static Class<?> getNetworkManagerClass() {
		try {
			return getMinecraftClass("INetworkManager", "NetworkManager");
		} catch (RuntimeException e) {
			Constructor<?> selected = FuzzyReflection.fromClass(getPlayerConnectionClass()).
					getConstructor(FuzzyMethodContract.newBuilder().
							parameterSuperOf(getMinecraftServerClass(), 0).
							parameterSuperOf(getEntityPlayerClass(), 2).
							build()
				   );

			// And we're done
			return setMinecraftClass("INetworkManager", selected.getParameterTypes()[1]);
		}
	}

	/**
	 * Retrieve the NetHandler class (or Connection)
	 * @return The NetHandler class.
	 */
	public static Class<?> getNetHandlerClass() {
		try {
			return getMinecraftClass("NetHandler", "Connection");
		} catch (RuntimeException e) {
			// Try getting the net login handler
			return setMinecraftClass("NetHandler", getNetLoginHandlerClass().getSuperclass());
		}
	}

	/**
	 * Retrieve the NMS ItemStack class.
	 * @return The ItemStack class.
	 */
	public static Class<?> getItemStackClass() {
		try {
			return getMinecraftClass("ItemStack");
		} catch (RuntimeException e) {
			// Use the handle reference
			return setMinecraftClass("ItemStack",
					FuzzyReflection.fromClass(getCraftItemStackClass(), true).getFieldByName("handle").getType());
		}
	}

	/**
	 * Retrieve the Block (NMS) class.
	 * @return Block (NMS) class.
	 */
	public static Class<?> getBlockClass() {
		try {
			return getMinecraftClass("Block");
		} catch (RuntimeException e) {
			FuzzyReflection reflect = FuzzyReflection.fromClass(getItemStackClass());
			Set<Class<?>> candidates = new HashSet<Class<?>>();

			// Minecraft objects in the constructor
			for (Constructor<?> constructor : reflect.getConstructors()) {
				for (Class<?> clazz : constructor.getParameterTypes()) {
					if (isMinecraftClass(clazz)) {
						candidates.add(clazz);
					}
				}
			}

			// Useful constructors
			Method selected =
						reflect.getMethod(FuzzyMethodContract.newBuilder().
							parameterMatches(FuzzyMatchers.matchAnyOf(candidates)).
							returnTypeExact(float.class).
						build());
			return setMinecraftClass("Block", selected.getParameterTypes()[0]);
		}
	}

	/**
	 * Retrieve the WorldType class.
	 * @return The WorldType class.
	 */
	public static Class<?> getWorldTypeClass() {
		try {
			return getMinecraftClass("WorldType");
		} catch (RuntimeException e) {
			// Get the first constructor that matches CraftServer(MINECRAFT_OBJECT, ANY)
			Method selected = FuzzyReflection.fromClass(getMinecraftServerClass(), true).
					getMethod(FuzzyMethodContract.newBuilder().
							parameterExactType(String.class, 0).
							parameterExactType(String.class, 1).
							parameterMatches(getMinecraftObjectMatcher()).
							parameterExactType(String.class, 4).
							parameterCount(5).
							build()
										);
			return setMinecraftClass("WorldType", selected.getParameterTypes()[3]);
		}
	}

	/**
	 * Retrieve the DataWatcher class.
	 * @return The DataWatcher class.
	 */
	public static Class<?> getDataWatcherClass() {
		try {
			return getMinecraftClass("DataWatcher");
		} catch (RuntimeException e) {
			// Describe the DataWatcher
			FuzzyClassContract dataWatcherContract = FuzzyClassContract.newBuilder().
				   field(FuzzyFieldContract.newBuilder().
						 requireModifier(Modifier.STATIC).
						 typeDerivedOf(Map.class)).
				   field(FuzzyFieldContract.newBuilder().
					     banModifier(Modifier.STATIC).
						 typeDerivedOf(Map.class)).
				   method(FuzzyMethodContract.newBuilder().
						 parameterExactType(int.class).
						 parameterExactType(Object.class).
						 returnTypeVoid()).
				  build();
			FuzzyFieldContract fieldContract = FuzzyFieldContract.newBuilder().
					typeMatches(dataWatcherContract).
					build();

			// Get such a field and save the result
			return setMinecraftClass("DataWatcher",
						FuzzyReflection.fromClass(getEntityClass(), true).
						getField(fieldContract).
						getType()
				   );
		}
	}

	/**
	 * Retrieves the ChunkPosition class.
	 *
	 * @return The ChunkPosition class.
	 */
	public static Class<?> getChunkPositionClass() {
		try {
			return getMinecraftClass("ChunkPosition");
		} catch (RuntimeException e) {
			try {
				Class<?> normalChunkGenerator = getCraftBukkitClass("generator.NormalChunkGenerator");

				// ChunkPosition a(net.minecraft.server.World world, String string, int i, int i1, int i2) {
				FuzzyMethodContract selected = FuzzyMethodContract.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterMatches(getMinecraftObjectMatcher(), 0)
						.parameterExactType(String.class, 1)
						.parameterExactType(int.class, 2)
						.parameterExactType(int.class, 3)
						.parameterExactType(int.class, 4)
						.build();

				return setMinecraftClass("ChunkPosition",
						FuzzyReflection.fromClass(normalChunkGenerator).getMethod(selected).getReturnType());
			} catch (Throwable ex) {
				return null;
			}
		}
	}

	/**
	 * Retrieves the BlockPosition class.
	 * 
	 * @return The BlockPosition class.
	 */
	public static Class<?> getBlockPositionClass() {
		try {
			return getMinecraftClass("BlockPosition");
		} catch (RuntimeException e) {
			try {
				Class<?> normalChunkGenerator = getCraftBukkitClass("generator.NormalChunkGenerator");

				// BlockPosition findNearestMapFeature(World, String, BlockPosition)
				FuzzyMethodContract selected = FuzzyMethodContract.newBuilder()
						.banModifier(Modifier.STATIC)
						.parameterMatches(getMinecraftObjectMatcher(), 0)
						.parameterExactType(String.class, 1)
						.parameterMatches(getMinecraftObjectMatcher(), 1)
						.build();

				return setMinecraftClass("BlockPosition",
						FuzzyReflection.fromClass(normalChunkGenerator).getMethod(selected).getReturnType());
			} catch (Throwable ex) {
				return null;
			}
		}
	}

	/**
	 * Retrieves the Vec3D class.
	 * @return The Vec3D class.
	 */
	public static Class<?> getVec3DClass() {
		try {
			return getMinecraftClass("Vec3D");
		} catch (RuntimeException e) {
			// TODO: Figure out a fuzzy field contract
			return null;
		}
	}

	/**
	 * Retrieve the ChunkCoordinates class.
	 * @return The ChunkPosition class.
	 */
	public static Class<?> getChunkCoordinatesClass() {
		try {
			return getMinecraftClass("ChunkCoordinates");
		} catch (RuntimeException e) {
			return setMinecraftClass("ChunkCoordinates", WrappedDataWatcher.getTypeClass(6));
		}
	}

	/**
	 * Retrieve the ChunkCoordIntPair class.
	 * @return The ChunkCoordIntPair class.
	 */
	public static Class<?> getChunkCoordIntPair() {
		if (!isUsingNetty())
			throw new IllegalArgumentException("Not supported on 1.6.4 and older.");

		try {
			return getMinecraftClass("ChunkCoordIntPair");
		} catch (RuntimeException e) {
			Class<?> packet = PacketRegistry.getPacketClassFromType(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

			AbstractFuzzyMatcher<Class<?>> chunkCoordIntContract = FuzzyClassContract.newBuilder().
					   field(FuzzyFieldContract.newBuilder().
							 typeDerivedOf(int.class)).
					   field(FuzzyFieldContract.newBuilder().
							 typeDerivedOf(int.class)).
					   method(FuzzyMethodContract.newBuilder().
							 parameterExactArray(int.class).
							 returnDerivedOf( getChunkPositionClass() )).
					  build().and(getMinecraftObjectMatcher());

			Field field = FuzzyReflection.fromClass(packet, true).getField(
				FuzzyFieldContract.matchType(chunkCoordIntContract));
			return setMinecraftClass("ChunkCoordIntPair", field.getType());
		}
	}

	/**
	 * Retrieve the WatchableObject class.
	 * @return The WatchableObject class.
	 */
	public static Class<?> getWatchableObjectClass() {
		try {
			return getMinecraftClass("WatchableObject", "DataWatcher$WatchableObject");
		} catch (RuntimeException e) {
			Method selected = FuzzyReflection.fromClass(getDataWatcherClass(), true).
					getMethod(FuzzyMethodContract.newBuilder().
							 requireModifier(Modifier.STATIC).
							 parameterDerivedOf(isUsingNetty() ? getPacketDataSerializerClass() : DataOutput.class, 0).
							 parameterMatches(getMinecraftObjectMatcher(), 1).
						    build());

			// Use the second parameter
			return setMinecraftClass("WatchableObject", selected.getParameterTypes()[1]);
		}
	}

	/**
	 * Retrieve the ServerConnection abstract class.
	 * @return The ServerConnection class.
	 */
	public static Class<?> getServerConnectionClass() {
		try {
			return getMinecraftClass("ServerConnection");
		} catch (RuntimeException e) {
			Method selected = null;
			FuzzyClassContract.Builder serverConnectionContract = FuzzyClassContract.newBuilder().
					constructor(FuzzyMethodContract.newBuilder().
							parameterExactType(getMinecraftServerClass()).
							parameterCount(1));

			if (isUsingNetty()) {
				serverConnectionContract.
				method(FuzzyMethodContract.newBuilder().
						parameterDerivedOf(InetAddress.class, 0).
						parameterDerivedOf(int.class, 1).
						parameterCount(2)
				);

				selected = FuzzyReflection.fromClass(getMinecraftServerClass()).
					getMethod(FuzzyMethodContract.newBuilder().
							requireModifier(Modifier.PUBLIC).
							returnTypeMatches(serverConnectionContract.build()).
					build());

			} else {
				serverConnectionContract.
					method(FuzzyMethodContract.newBuilder().
							parameterExactType(getPlayerConnectionClass()));

				selected = FuzzyReflection.fromClass(getMinecraftServerClass()).
					getMethod(FuzzyMethodContract.newBuilder().
							requireModifier(Modifier.ABSTRACT).
							returnTypeMatches(serverConnectionContract.build()).
					build());
			}

			// Use the return type
			return setMinecraftClass("ServerConnection", selected.getReturnType());
		}
	}

	/**
	 * Retrieve the NBT base class.
	 * @return The NBT base class.
	 */
	public static Class<?> getNBTBaseClass() {
		try {
			return getMinecraftClass("NBTBase");
		} catch (RuntimeException e) {
			Class<?> nbtBase = null;

			if (isUsingNetty()) {
				FuzzyClassContract tagCompoundContract = FuzzyClassContract.newBuilder().
						field(FuzzyFieldContract.newBuilder().
							typeDerivedOf(Map.class)).
						method(FuzzyMethodContract.newBuilder().
							parameterDerivedOf(DataOutput.class).
							parameterCount(1)).
						build();

				Method selected = FuzzyReflection.fromClass(getPacketDataSerializerClass()).
						getMethod(FuzzyMethodContract.newBuilder().
							banModifier(Modifier.STATIC).
							parameterCount(1).
							parameterMatches(tagCompoundContract).
							returnTypeVoid().
							build()
						 );
				nbtBase = selected.getParameterTypes()[0].getSuperclass();

			} else {
				FuzzyClassContract tagCompoundContract = FuzzyClassContract.newBuilder().
					constructor(FuzzyMethodContract.newBuilder().
						parameterExactType(String.class).
						parameterCount(1)).
					field(FuzzyFieldContract.newBuilder().
						typeDerivedOf(Map.class)).
					build();

				Method selected = FuzzyReflection.fromClass(getPacketClass()).
					getMethod(FuzzyMethodContract.newBuilder().
						requireModifier(Modifier.STATIC).
						parameterSuperOf(DataInputStream.class).
						parameterCount(1).
						returnTypeMatches(tagCompoundContract).
						build()
					 );
				nbtBase = selected.getReturnType().getSuperclass();
			}

			// That can't be correct
			if (nbtBase == null || nbtBase.equals(Object.class)) {
				throw new IllegalStateException("Unable to find NBT base class: " + nbtBase);
			}

			// Use the return type here too
			return setMinecraftClass("NBTBase", nbtBase);
		}
 	}

	/**
	 * Retrieve the NBT read limiter class.
	 * <p>
	 * This is only supported in 1.7.8 (released 2014) and higher.
	 * @return The NBT read limiter.
	 */
	public static Class<?> getNBTReadLimiterClass() {
		return getMinecraftClass("NBTReadLimiter");
	}

	/**
	 * Retrieve the NBT Compound class.
	 * @return The NBT Compond class.
	 */
	public static Class<?> getNBTCompoundClass() {
		try {
			return getMinecraftClass("NBTTagCompound");
		} catch (RuntimeException e) {
			return setMinecraftClass(
				"NBTTagCompound",
				NbtFactory.ofWrapper(NbtType.TAG_COMPOUND, "Test").getHandle().getClass()
			);
		}
	}

	/**
	 * Retrieve the EntityTracker (NMS) class.
	 * @return EntityTracker class.
	 */
	public static Class<?> getEntityTrackerClass() {
		try {
			return getMinecraftClass("EntityTracker");
		} catch (RuntimeException e) {
			FuzzyClassContract entityTrackerContract = FuzzyClassContract.newBuilder().
					field(FuzzyFieldContract.newBuilder().
						  typeDerivedOf(Set.class)).
				    method(FuzzyMethodContract.newBuilder().
				    	  parameterSuperOf(MinecraftReflection.getEntityClass()).
				    	  parameterCount(1).
				    	  returnTypeVoid()).
				    method(FuzzyMethodContract.newBuilder().
						  parameterSuperOf(MinecraftReflection.getEntityClass(), 0).
						  parameterSuperOf(int.class, 1).
						  parameterSuperOf(int.class, 2).
						  parameterCount(3).
						  returnTypeVoid()).
			build();

			Field selected = FuzzyReflection.fromClass(MinecraftReflection.getWorldServerClass(), true).
					getField(FuzzyFieldContract.newBuilder().
							   typeMatches(entityTrackerContract).
							   build()
					);

			// Go by the defined type of this field
			return setMinecraftClass("EntityTracker", selected.getType());
		}
	}

	/**
	 * Retrieve the NetworkListenThread class (NMS).
	 * <p>
	 * Note that this class was removed after Minecraft 1.3.1.
	 * @return NetworkListenThread class.
	 */
	public static Class<?> getNetworkListenThreadClass() {
		try {
			return getMinecraftClass("NetworkListenThread");
		} catch (RuntimeException e) {
			FuzzyClassContract networkListenContract = FuzzyClassContract.newBuilder().
					field(FuzzyFieldContract.newBuilder().
						  typeDerivedOf(ServerSocket.class)).
					field(FuzzyFieldContract.newBuilder().
						  typeDerivedOf(Thread.class)).
					field(FuzzyFieldContract.newBuilder().
						  typeDerivedOf(List.class)).
					method(FuzzyMethodContract.newBuilder().
						  parameterExactType(getPlayerConnectionClass())).
			build();

			Field selected = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass(), true).
					getField(FuzzyFieldContract.newBuilder().
							   typeMatches(networkListenContract).
							   build()
					);

			// Go by the defined type of this field
			return setMinecraftClass("NetworkListenThread", selected.getType());
		}
	}

	/**
	 * Retrieve the attribute snapshot class.
	 * <p>
	 * This stores the final value of an attribute, along with all the associated computational steps.
	 * @return The attribute snapshot class.
	 */
	public static Class<?> getAttributeSnapshotClass() {
		try {
			return getMinecraftClass("AttributeSnapshot", "PacketPlayOutUpdateAttributes$AttributeSnapshot");
		} catch (RuntimeException ex) {
			try {
				// It should be the parameter of a list in the update attributes packet
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(PacketType.Play.Server.UPDATE_ATTRIBUTES.getPacketClass(), true);
				Field field = fuzzy.getFieldByType("attributes", Collection.class);
				Type param = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
				return setMinecraftClass("AttributeSnapshot", (Class<?>) param);
			} catch (Throwable ex1) {
				return getMinecraftClass("AttributeSnapshot");
			}
		}
	}

	/**
	 * Retrieve the IntHashMap class.
	 * @return IntHashMap class.
	 */
	public static Class<?> getIntHashMapClass() {
		try {
			return getMinecraftClass("IntHashMap");
		} catch (RuntimeException e) {
			final Class<?> parent = getEntityTrackerClass();

			// Expected structure of a IntHashMap
			final FuzzyClassContract intHashContract = FuzzyClassContract.newBuilder().
				// add(int key, Object value)
				method(FuzzyMethodContract.newBuilder().
						parameterCount(2).
						parameterExactType(int.class, 0).
						parameterExactType(Object.class, 1).requirePublic()
				).
				// Object get(int key)
				method(FuzzyMethodContract.newBuilder().
						parameterCount(1).
						parameterExactType(int.class).
						returnTypeExact(Object.class).requirePublic()
				).
				// Finally, there should be an array of some kind
				field(FuzzyFieldContract.newBuilder().
						typeMatches(FuzzyMatchers.matchArray(FuzzyMatchers.matchAll()))
				).
			build();

			final AbstractFuzzyMatcher<Field> intHashField = FuzzyFieldContract.newBuilder().
				typeMatches(getMinecraftObjectMatcher().and(intHashContract)).
				build();

			// Use the type of the first field that matches
			return setMinecraftClass("IntHashMap", FuzzyReflection.fromClass(parent).getField(intHashField).getType());
		}
	}

	/**
	 * Retrieve the attribute modifier class.
	 * @return Attribute modifier class.
	 */
	public static Class<?> getAttributeModifierClass() {
		try {
			return getMinecraftClass("AttributeModifier");
		} catch (RuntimeException e) {
			// Initialize first
			getAttributeSnapshotClass();
			return getMinecraftClass("AttributeModifier");
		}
	}

	/**
	 * Retrieve the net.minecraft.server.MobEffect class.
	 * @return The mob effect class.
	 */
	public static Class<?> getMobEffectClass() {
		try {
			return getMinecraftClass("MobEffect");
		} catch (RuntimeException e) {
			// It is the second parameter in Packet41MobEffect
			Class<?> packet = PacketRegistry.getPacketClassFromType(PacketType.Play.Server.ENTITY_EFFECT);
			Constructor<?> constructor = FuzzyReflection.fromClass(packet).getConstructor(
				FuzzyMethodContract.newBuilder().
				parameterCount(2).
				parameterExactType(int.class, 0).
				parameterMatches(getMinecraftObjectMatcher(), 1).
				build()
			);
			return setMinecraftClass("MobEffect", constructor.getParameterTypes()[1]);
		}
	}

	/**
	 * Retrieve the packet data serializer class that overrides ByteBuf.
	 * @return The data serializer class.
	 */
	public static Class<?> getPacketDataSerializerClass() {
		try {
			return getMinecraftClass("PacketDataSerializer");
		} catch (RuntimeException e) {
			Class<?> packet = getPacketClass();
			Method method = FuzzyReflection.fromClass(packet).getMethod(
					FuzzyMethodContract.newBuilder().
					parameterCount(1).
					parameterDerivedOf(getByteBufClass()).
					returnTypeVoid().
					build()
				);
			return setMinecraftClass("PacketDataSerializer", method.getParameterTypes()[0]);
		}
	}

	/**
	 * Retrieve the NBTCompressedStreamTools class.
	 * @return The NBTCompressedStreamTools class.
	 */
	public static Class<?> getNbtCompressedStreamToolsClass() {
		try {
			return getMinecraftClass("NBTCompressedStreamTools");
		} catch (RuntimeException e) {
			Class<?> packetSerializer = getPacketDataSerializerClass();

			// Get the write NBT compound method
			Method writeNbt = FuzzyReflection.fromClass(packetSerializer).
					getMethodByParameters("writeNbt", getNBTCompoundClass());

			try {
				// Now -- we inspect all the method calls within that method, and use the first foreign Minecraft class
				for (AsmMethod method : ClassAnalyser.getDefault().getMethodCalls(writeNbt)) {
					Class<?> owner = method.getOwnerClass();

					if (!packetSerializer.equals(owner) && isMinecraftClass(owner)) {
						return setMinecraftClass("NBTCompressedStreamTools", owner);
					}
				}
			} catch (Exception e1) {
				throw new RuntimeException("Unable to analyse class.", e1);
			}
			throw new IllegalArgumentException("Unable to find NBTCompressedStreamTools.");
		}
	}

	/**
	 * Retrieve the NMS tile entity class.
	 * @return The tile entity class.
	 */
	public static Class<?> getTileEntityClass() {
		return getMinecraftClass("TileEntity");
	}

	private static Class<?> gsonClass = null;

	/**
	 * Retrieve the Gson class used by Minecraft.
	 * @return The Gson class.
	 */
	public static Class<?> getMinecraftGsonClass() {
		if (gsonClass == null) {
			try {
				return gsonClass = getClass("org.bukkit.craftbukkit.libs.com.google.gson.Gson");
			} catch (RuntimeException e) {
				return gsonClass = getClass("com.google.gson.Gson");
			}
		}

		return gsonClass;
	}

	/**
	 * Retrieve the ItemStack[] class.
	 * @return The ItemStack[] class.
	 */
	public static Class<?> getItemStackArrayClass() {
		if (itemStackArrayClass == null)
			itemStackArrayClass = getArrayClass(getItemStackClass());
		return itemStackArrayClass;
	}

	/**
	 * Retrieve the array class of a given component type.
	 * @param componentType - type of each element in the array.
	 * @return The class of the array.
	 */
	public static Class<?> getArrayClass(Class<?> componentType) {
		// Bit of a hack, but it works
		return Array.newInstance(componentType, 0).getClass();
	}

	/**
	 * Retrieve the CraftItemStack class.
	 * @return The CraftItemStack class.
	 */
	public static Class<?> getCraftItemStackClass() {
		return getCraftBukkitClass("inventory.CraftItemStack");
	}

	/**
	 * Retrieve the CraftPlayer class.
	 * @return CraftPlayer class.
	 */
	public static Class<?> getCraftPlayerClass() {
		return getCraftBukkitClass("entity.CraftPlayer");
	}

	/**
	 * Retrieve the CraftWorld class.
	 * @return The CraftWorld class.
	 */
	public static Class<?> getCraftWorldClass() {
		return getCraftBukkitClass("CraftWorld");
	}

	/**
	 * Retrieve the CraftEntity class.
	 * @return CraftEntity class.
	 */
	public static Class<?> getCraftEntityClass() {
		return getCraftBukkitClass("entity.CraftEntity");
	}

	/**
	 * Retrieve the CraftChatMessage introduced in 1.7.2
	 * @return The CraftChatMessage class.
	 */
	public static Class<?> getCraftMessageClass() {
		return getCraftBukkitClass("util.CraftChatMessage");
	}

	/**
	 * Retrieve the PlayerInfoData class in 1.8.
	 * @return The PlayerInfoData class
	 */
	public static Class<?> getPlayerInfoDataClass() {
		return getMinecraftClass("PacketPlayOutPlayerInfo$PlayerInfoData", "PlayerInfoData");
	}

	/**
	 * Determine if the given object is a PlayerInfoData.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isPlayerInfoData(Object obj) {
		Class<?> clazz = getPlayerInfoDataClass();
		return clazz != null && obj.getClass().equals(clazz);
	}

	/**
	 * Retrieve the IBlockData class in 1.8.
	 * @return The IBlockData class
	 */
	public static Class<?> getIBlockDataClass() {
		return getMinecraftClass("IBlockData");
	}

	/**
	 * Retrieve the MultiBlockChangeInfo class in 1.8.
	 * @return The MultiBlockChangeInfo class
	 */
	public static Class<?> getMultiBlockChangeInfoClass() {
		return getMinecraftClass("MultiBlockChangeInfo", "PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
	}

	/**
	 * Retrieve the MultiBlockChangeInfo array class in 1.8.
	 * @return The MultiBlockChangeInfo array class
	 */
	public static Class<?> getMultiBlockChangeInfoArrayClass() {
		return getArrayClass(getMultiBlockChangeInfoClass());
	}

	/**
	 * Retrieve a CraftItemStack from a given ItemStack.
	 * @param bukkitItemStack - the Bukkit ItemStack to convert.
	 * @return A CraftItemStack as an ItemStack.
	 */
	public static ItemStack getBukkitItemStack(ItemStack bukkitItemStack) {
		// Delegate this task to the method that can execute it
		if (craftBukkitNMS != null)
			return getBukkitItemByMethod(bukkitItemStack);

		if (craftBukkitConstructor == null) {
			try {
				craftBukkitConstructor = getCraftItemStackClass().getConstructor(ItemStack.class);
			} catch (Exception e) {
				// See if this method works
				if (!craftItemStackFailed)
					return getBukkitItemByMethod(bukkitItemStack);

				throw new RuntimeException("Cannot find CraftItemStack(org.bukkit.inventory.ItemStack).", e);
			}
		}

		// Try to create the CraftItemStack
		try {
			return (ItemStack) craftBukkitConstructor.newInstance(bukkitItemStack);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct CraftItemStack.", e);
		}
	}

	private static ItemStack getBukkitItemByMethod(ItemStack bukkitItemStack) {
		if (craftBukkitNMS == null) {
			try {
				craftBukkitNMS = getCraftItemStackClass().getMethod("asNMSCopy", ItemStack.class);
				craftBukkitOBC = getCraftItemStackClass().getMethod("asCraftMirror", MinecraftReflection.getItemStackClass());
			} catch (Exception e) {
				craftItemStackFailed = true;
				throw new RuntimeException("Cannot find CraftItemStack.asCraftCopy(org.bukkit.inventory.ItemStack).", e);
			}
		}

		// Next, construct it
		try {
			Object nmsItemStack = craftBukkitNMS.invoke(null, bukkitItemStack);
			return (ItemStack) craftBukkitOBC.invoke(null, nmsItemStack);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct CraftItemStack.", e);
		}
	}

	/**
	 * Retrieve the Bukkit ItemStack from a given net.minecraft.server ItemStack.
	 * @param minecraftItemStack - the NMS ItemStack to wrap.
	 * @return The wrapped ItemStack.
	 */
	public static ItemStack getBukkitItemStack(Object minecraftItemStack) {
		// Delegate this task to the method that can execute it
		if (craftNMSMethod != null)
			return getBukkitItemByMethod(minecraftItemStack);

		if (craftNMSConstructor == null) {
			try {
				craftNMSConstructor = getCraftItemStackClass().getConstructor(minecraftItemStack.getClass());
			} catch (Exception e) {
				// Give it a try
				if (!craftItemStackFailed)
					return getBukkitItemByMethod(minecraftItemStack);

				throw new RuntimeException("Cannot find CraftItemStack(net.minecraft.server.ItemStack).", e);
			}
		}

		// Try to create the CraftItemStack
		try {
			return (ItemStack) craftNMSConstructor.newInstance(minecraftItemStack);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct CraftItemStack.", e);
		}
	}

	private static ItemStack getBukkitItemByMethod(Object minecraftItemStack) {
		if (craftNMSMethod == null) {
			try {
				craftNMSMethod = getCraftItemStackClass().getMethod("asCraftMirror", minecraftItemStack.getClass());
			} catch (Exception e) {
				craftItemStackFailed = true;
				throw new RuntimeException("Cannot find CraftItemStack.asCraftMirror(net.minecraft.server.ItemStack).", e);
			}
		}

		// Next, construct it
		try {
			return (ItemStack) craftNMSMethod.invoke(null, minecraftItemStack);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct CraftItemStack.", e);
		}
	}

	/**
	 * Retrieve the net.minecraft.server ItemStack from a Bukkit ItemStack.
	 * <p>
	 * By convention, item stacks that contain air are usually represented as NULL.
	 * 
	 * @param stack - the Bukkit ItemStack to convert.
	 * @return The NMS ItemStack, or NULL if the stack represents air.
	 */
	public static Object getMinecraftItemStack(ItemStack stack) {
		// Make sure this is a CraftItemStack
		if (!isCraftItemStack(stack))
			stack = getBukkitItemStack(stack);

		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		return unwrapper.unwrapItem(stack);
	}

	/**
	 * Retrieve the given class by name.
	 * @param className - name of the class.
	 * @return The class.
	 */
	private static Class<?> getClass(String className) {
		try {
			return MinecraftReflection.class.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot find class " + className, e);
		}
	}

	/**
	 * Retrieve the class object of a specific CraftBukkit class.
	 * @param className - the specific CraftBukkit class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find the given class.
	 */
	public static Class<?> getCraftBukkitClass(String className) {
		if (craftbukkitPackage == null)
			craftbukkitPackage = new CachedPackage(getCraftBukkitPackage(), getClassSource());
		return craftbukkitPackage.getPackageClass(className);
	}
	
	/**
	 * Retrieve the class object of a specific Minecraft class.
	 * @param className - the specific Minecraft class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find the given class.
	 */
	public static Class<?> getMinecraftClass(String className) {
		if (minecraftPackage == null)
			minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
		return minecraftPackage.getPackageClass(className);
	}

	/**
	 * Set the class object for the specific Minecraft class.
	 * @param className - name of the Minecraft class.
	 * @param clazz - the new class object.
	 * @return The provided clazz object.
	 */
	private static Class<?> setMinecraftClass(String className, Class<?> clazz) {
		if (minecraftPackage == null)
			minecraftPackage = new CachedPackage(getMinecraftPackage(), getClassSource());
		minecraftPackage.setPackageClass(className, clazz);
		return clazz;
	}

	/**
	 * Retrieve the current class source.
	 * @return The class source.
	 */
	private static ClassSource getClassSource() {
		ErrorReporter reporter = ProtocolLibrary.getErrorReporter();

		// Lazy pattern again
		if (classSource == null) {
			// Attempt to use MCPC
			try {
				return classSource = new RemappedClassSource().initialize();
			} catch (RemapperUnavaibleException e) {
				if (e.getReason() != Reason.MCPC_NOT_PRESENT)
					reporter.reportWarning(MinecraftReflection.class, Report.newBuilder(REPORT_CANNOT_FIND_MCPC_REMAPPER));
			} catch (Exception e) {
				reporter.reportWarning(MinecraftReflection.class, Report.newBuilder(REPORT_CANNOT_LOAD_CPC_REMAPPER));
			}

			// Just use the default class loader
			classSource = ClassSource.fromClassLoader();
		}

		return classSource;
	}

	/**
	 * Retrieve the first class that matches a specified Minecraft name.
	 * @param className - the specific Minecraft class.
	 * @param aliases - alternative names for this Minecraft class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find any of the given classes.
	 */
	public static Class<?> getMinecraftClass(String className, String... aliases) {
		try {
			// Try the main class first
			return getMinecraftClass(className);
		} catch (RuntimeException e) {
			Class<?> success = null;

			// Try every alias too
			for (String alias : aliases) {
				try {
					success = getMinecraftClass(alias);
					break;
				} catch (RuntimeException e1) {
					// e1.printStackTrace();
				}
			}

			if (success != null) {
				// Save it for later
				minecraftPackage.setPackageClass(className, success);
				return success;
			} else {
				// Hack failed
				throw new RuntimeException(String.format("Unable to find %s (%s)", className, Joiner.on(", ").join(aliases)));
			}
		}
	}

	/**
	 * Dynamically retrieve the NetworkManager name.
	 * @return Name of the NetworkManager class.
	 */
	public static String getNetworkManagerName() {
		return getNetworkManagerClass().getSimpleName();
	}

	/**
	 * Dynamically retrieve the name of the current NetLoginHandler.
	 * @return Name of the NetLoginHandler class.
	 */
	public static String getNetLoginHandlerName() {
		return getNetLoginHandlerClass().getSimpleName();
	}

	/**
	 * Retrieve an instance of the packet data serializer wrapper.
	 * @param buffer - the buffer.
	 * @return The instance.
	 */
	public static Object getPacketDataSerializer(Object buffer) {
		Class<?> packetSerializer = getPacketDataSerializerClass();

		try {
			return packetSerializer.getConstructor(getByteBufClass()).newInstance(buffer);
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct packet serializer.", e);
		}
	}
}