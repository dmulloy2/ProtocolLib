package com.comphenix.protocol.utility;

import org.bukkit.entity.Player;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.base.Preconditions;

/**
 * Retrieve the content of well-known fields in Minecraft.
 * @author Kristian
 */
public class MinecraftFields {
	// Cached accessors
	private static volatile FieldAccessor CONNECTION_ACCESSOR;
	private static volatile FieldAccessor NETWORK_ACCESSOR;
	
	private MinecraftFields() {
		// Not constructable
	}
	
	/**
	 * Retrieve the network mananger associated with a particular player.
	 * @param player - the player.
	 * @return The network manager, or NULL if no network manager has been asssociated yet.
	 */
	public static Object getNetworkManager(Player player) {
		Object nmsPlayer = BukkitUnwrapper.getInstance().unwrapItem(player);
		
		if (NETWORK_ACCESSOR == null) {
			Class<?> networkClass = MinecraftReflection.getNetworkManagerClass();
			Class<?> connectionClass = MinecraftReflection.getPlayerConnectionClass();
			NETWORK_ACCESSOR = Accessors.getFieldAccessor(connectionClass, networkClass, true);
		}
		// Retrieve the network manager
		final Object playerConnection = getPlayerConnection(nmsPlayer);
		
		if (playerConnection != null)
			return NETWORK_ACCESSOR.get(playerConnection);
		return null;
	}
	
	/**
	 * Retrieve the PlayerConnection (or NetServerHandler) associated with a player.
	 * @param player - the player.
	 * @return The player connection.
	 */
	public static Object getPlayerConnection(Player player) {
		Preconditions.checkNotNull(player, "player cannot be null!");
		return getPlayerConnection(BukkitUnwrapper.getInstance().unwrapItem(player));
	}
	
	// Retrieve player connection from a native instance
	private static Object getPlayerConnection(Object nmsPlayer) {
		Preconditions.checkNotNull(nmsPlayer, "nmsPlayer cannot be null!");

		if (CONNECTION_ACCESSOR == null) {
			Class<?> connectionClass = MinecraftReflection.getPlayerConnectionClass();
			CONNECTION_ACCESSOR = Accessors.getFieldAccessor(nmsPlayer.getClass(), connectionClass, true);
		}
		return CONNECTION_ACCESSOR.get(nmsPlayer);
	}
}
