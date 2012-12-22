package com.comphenix.protocol.utility;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.google.common.base.Joiner;

/**
 * Methods and constants specifically used in conjuction with reflecting Minecraft object.
 * 
 * @author Kristian
 */
public class MinecraftReflection {
	/**
	 * Regular expression that matches a Minecraft object.
	 */
	public static final String MINECRAFT_OBJECT = "net\\.minecraft(\\.\\w+)+";
	
	/**
	 * The package name of all the classes that belongs to the native code in Minecraft.
	 */
	private static String MINECRAFT_PREFIX_PACKAGE = "net.minecraft.server";

	private static String MINECRAFT_FULL_PACKAGE = null;
	private static String CRAFTBUKKIT_PACKAGE = null;
	
	private static CachedPackage minecraftPackage;
	private static CachedPackage craftbukkitPackage;
	
	// org.bukkit.craftbukkit
	private static Constructor<?> craftNMSConstructor;
	private static Constructor<?> craftBukkitConstructor;
	
	// New in 1.4.5
	private static Method craftNMSMethod;
	private static Method craftBukkitMethod;
	private static boolean craftItemStackFailed;
	
	// net.minecraft.server
	private static Class<?> itemStackArrayClass;

	/**
	 * Retrieve the name of the Minecraft server package.
	 * @return Full canonical name of the Minecraft server package.
	 */
	public static String getMinecraftPackage() {
		// Speed things up
		if (MINECRAFT_FULL_PACKAGE != null)
			return MINECRAFT_FULL_PACKAGE;
		
		Server craftServer = Bukkit.getServer();
		
		// This server should have a "getHandle" method that we can use
		if (craftServer != null) {
			try {
				Class<?> craftClass = craftServer.getClass();
				Method getHandle = craftClass.getMethod("getHandle");
				
				Class<?> returnType = getHandle.getReturnType();
				String returnName = returnType.getCanonicalName();
				
				// The return type will tell us the full package, regardless of formating
				CRAFTBUKKIT_PACKAGE = getPackage(craftClass.getCanonicalName());
				MINECRAFT_FULL_PACKAGE = getPackage(returnName);
				return MINECRAFT_FULL_PACKAGE;
						
			} catch (SecurityException e) {
				throw new RuntimeException("Security violation. Cannot get handle method.", e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("Cannot find getHandle() method on server. Is this a modified CraftBukkit version?", e);
			}
			
		} else {
			throw new IllegalStateException("Could not find Bukkit. Is it running?");
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
	}
	
	/**
	 * Retrieve the name of the root CraftBukkit package.
	 * @return Full canonical name of the root CraftBukkit package.
	 */
	public static String getCraftBukkitPackage() {
		// Ensure it has been initialized
		getMinecraftPackage();
		return CRAFTBUKKIT_PACKAGE;
	}
	
	/**
	 * Retrieve the package name from a given canonical Java class name.
	 * @param fullName - full Java class name.
	 * @return The package name.
	 */
	private static String getPackage(String fullName) {
		return fullName.substring(0, fullName.lastIndexOf("."));
	}
	
	/**
	 * Determine if a given object can be found within the package net.minecraft.server.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isMinecraftObject(@Nonnull Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("Cannot determine the type of a null object.");
		
		// Doesn't matter if we don't check for the version here
		return obj.getClass().getName().startsWith(MINECRAFT_PREFIX_PACKAGE);
	}

	/**
	 * Determine if a given object is found in net.minecraft.server, and has the given name.
	 * @param obj - the object to test.
	 * @param className - the class name to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isMinecraftObject(@Nonnull Object obj, String className) {
		if (obj == null)
			throw new IllegalArgumentException("Cannot determine the type of a null object.");
		
		String javaName = obj.getClass().getName();
		return javaName.startsWith(MINECRAFT_PREFIX_PACKAGE) && javaName.endsWith(className);
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
			return nmsObject.getClass().getMethod("getBukkitEntity").invoke(nmsObject);
		} catch (Exception e) {
			throw new RuntimeException("Cannot get Bukkit entity from " + nmsObject, e);
		}
	}
	
	/**
	 * Determine if a given object is a ChunkPosition.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isChunkPosition(Object obj) {
		return getChunkPositionClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if a given object is a ChunkCoordinate.
	 * @param obj - the object to test.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public static boolean isChunkCoordinates(Object obj) {
		return getChunkCoordinatesClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is actually a Minecraft packet.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isPacketClass(Object obj) {
		return getPacketClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is a NetLoginHandler.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isLoginHandler(Object obj) {
		return getNetLoginHandlerClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is actually a Minecraft packet.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isMinecraftEntity(Object obj) {
		return getEntityClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is a NMS ItemStack.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isItemStack(Object value) {
		return getItemStackClass().isAssignableFrom(value.getClass());
	}
	
	/**
	 * Determine if the given object is a Minecraft player entity.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isMinecraftPlayer(Object obj) {
		return getEntityPlayerClass().isAssignableFrom(obj.getClass());
	}

	/**
	 * Determine if the given object is a watchable object.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isWatchableObject(Object obj) {
		return getWatchableObjectClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is a data watcher object.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isDataWatcher(Object obj) {
		return getDataWatcherClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Determine if the given object is a CraftItemStack instancey.
	 * @param obj - the given object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isCraftItemStack(Object obj) {
		return getCraftItemStackClass().isAssignableFrom(obj.getClass());
	}
	
	/**
	 * Retrieve the EntityPlayer (NMS) class.
	 * @return The entity class.
	 */
	public static Class<?> getEntityPlayerClass() {
		return getMinecraftClass("EntityPlayer");
	}
	
	/**
	 * Retrieve the entity (NMS) class.
	 * @return The entity class.
	 */
	public static Class<?> getEntityClass() {
		return getMinecraftClass("Entity");
	}
	
	/**
	 * Retrieve the packet class.
	 * @return The packet class.
	 */
	public static Class<?> getPacketClass() {
		return getMinecraftClass("Packet");
	}
	
	/**
	 * Retrieve the NetLoginHandler class.
	 * @return The NetLoginHandler class.
	 */
	public static Class<?> getNetLoginHandlerClass() {
		return getMinecraftClass("NetLoginHandler", "PendingConnection");
	}
	
	/**
	 * Retrieve the NetServerHandler class.
	 * @return The NetServerHandler class.
	 */
	public static Class<?> getNetServerHandlerClass() {
		return getMinecraftClass("NetServerHandler", "PlayerConnection");
	}
	
	/**
	 * Retrieve the NetworkManager class.
	 * @return The NetworkManager class.
	 */
	public static Class<?> getNetworkManagerClass() {
		return getMinecraftClass("NetworkManager");
	}
	
	/**
	 * Retrieve the NetHandler class.
	 * @return The NetHandler class.
	 */
	public static Class<?> getNetHandlerClass() {
		return getMinecraftClass("NetHandler", "Connection");
	}
	
	/**
	 * Retrieve the NMS ItemStack class.
	 * @return The ItemStack class.
	 */
	public static Class<?> getItemStackClass() {
		return getMinecraftClass("ItemStack");
	}
		
	/**
	 * Retrieve the WorldType class.
	 * @return The WorldType class.
	 */
	public static Class<?> getWorldTypeClass() {
		return getMinecraftClass("WorldType");
	}
	
	/**
	 * Retrieve the MinecraftServer class.
	 * @return MinecraftServer class.
	 */
	public static Class<?> getMinecraftServerClass() {
		return getMinecraftClass("MinecraftServer");
	}
	
	/**
	 * Retrieve the DataWatcher class.
	 * @return The DataWatcher class.
	 */
	public static Class<?> getDataWatcherClass() {
		return getMinecraftClass("DataWatcher");
	}
	
	/**
	 * Retrieve the ChunkPosition class.
	 * @return The ChunkPosition class.
	 */
	public static Class<?> getChunkPositionClass() {
		return getMinecraftClass("ChunkPosition");
	}
	
	/**
	 * Retrieve the ChunkPosition class.
	 * @return The ChunkPosition class.
	 */
	public static Class<?> getChunkCoordinatesClass() {
		return getMinecraftClass("ChunkCoordinates");
	}
	
	/**
	 * Retrieve the WatchableObject class.
	 * @return The WatchableObject class.
	 */
	public static Class<?> getWatchableObjectClass() {
		return getMinecraftClass("WatchableObject");
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
	 * Retrieve a CraftItemStack from a given ItemStack.
	 * @param bukkitItemStack - the Bukkit ItemStack to convert.
	 * @return A CraftItemStack as an ItemStack.
	 */
	public static ItemStack getBukkitItemStack(ItemStack bukkitItemStack) {
		// Delegate this task to the method that can execute it
		if (craftBukkitMethod != null)
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
		if (craftBukkitMethod == null) {
			try {
				craftBukkitMethod = getCraftItemStackClass().getMethod("asCraftCopy", ItemStack.class);
			} catch (Exception e) {
				craftItemStackFailed = true;
				throw new RuntimeException("Cannot find CraftItemStack.asCraftCopy(org.bukkit.inventory.ItemStack).", e);
			}
		}
		
		// Next, construct it
		try {
			return (ItemStack) craftBukkitMethod.invoke(null, bukkitItemStack);
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
				
				throw new RuntimeException("Cannot find CraftItemStack(net.mineraft.server.ItemStack).", e);
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
				throw new RuntimeException("Cannot find CraftItemStack.asCraftMirror(net.mineraft.server.ItemStack).", e);
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
	 * @param stack - the Bukkit ItemStack to convert.
	 * @return The NMS ItemStack.
	 */
	public static Object getMinecraftItemStack(ItemStack stack) {
		// Make sure this is a CraftItemStack
		if (!isCraftItemStack(stack))
			stack = getBukkitItemStack(stack);
		
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		return unwrapper.unwrapItem(stack);
	}
	
	/**
	 * Retrieve the class object of a specific CraftBukkit class.
	 * @param className - the specific CraftBukkit class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find the given class.
	 */
	@SuppressWarnings("rawtypes")
	public static Class getCraftBukkitClass(String className) {
		if (craftbukkitPackage == null)
			craftbukkitPackage = new CachedPackage(getCraftBukkitPackage());
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
			minecraftPackage = new CachedPackage(getMinecraftPackage());
		return minecraftPackage.getPackageClass(className);
	}
	
	/**
	 * Retrieve the first class that matches a specified Minecraft name.
	 * @param classes - the specific Minecraft class.
	 * @return Class object.
	 * @throws RuntimeException If we are unable to find any of the given classes.
	 */
	public static Class<?> getMinecraftClass(String className, String... aliases) {
		try {
			// Try the main class first
			return getMinecraftClass(className);
		} catch (RuntimeException e1) {
			Class<?> success = null;
			
			// Try every alias too
			for (String alias : aliases) {
				try {
					success = getMinecraftClass(alias);
					break;
				} catch (RuntimeException e2) {
					// Swallov
				}
			}
			
			if (success != null) {
				// Save it for later
				minecraftPackage.setPackageClass(className, success);
				return success;
			} else {
				// Hack failed
				throw new RuntimeException(
					String.format("Unable to find %s (%s)",
								  className,
								  Joiner.on(", ").join(aliases))
					);
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
}
