package com.comphenix.protocol.utility;

import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import org.bukkit.entity.Player;

/**
 * Retrieve the content of well-known fields in Minecraft.
 *
 * @author Kristian
 */
public final class MinecraftFields {

	// Cached accessors
	private static volatile FieldAccessor CONNECTION_ACCESSOR;
	private static volatile FieldAccessor NETWORK_ACCESSOR;
	private static volatile FieldAccessor CONNECTION_ENTITY_ACCESSOR;

	private MinecraftFields() {
		// Not constructable
	}

	/**
	 * Retrieve the network manager associated with a particular player.
	 *
	 * @param player - the player.
	 * @return The network manager, or NULL if no network manager has been associated yet.
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
		if (playerConnection != null) {
			return NETWORK_ACCESSOR.get(playerConnection);
		}

		return null;
	}

	/**
	 * Retrieve the PlayerConnection (or NetServerHandler) associated with a player.
	 *
	 * @param player - the player.
	 * @return The player connection.
	 */
	public static Object getPlayerConnection(Player player) {
		return getPlayerConnection(BukkitUnwrapper.getInstance().unwrapItem(player));
	}

	/**
	 * Retrieve the PlayerConnection (or NetServerHandler) associated with a player.
	 *
	 * @param nmsPlayer - the NMS player.
	 * @return The player connection.
	 */
	public static Object getPlayerConnection(Object nmsPlayer) {
		if (CONNECTION_ACCESSOR == null) {
			Class<?> connectionClass = MinecraftReflection.getPlayerConnectionClass();
			CONNECTION_ACCESSOR = Accessors.getFieldAccessor(nmsPlayer.getClass(), connectionClass, true);
		}

		return CONNECTION_ACCESSOR.get(nmsPlayer);
	}

	/**
	 * Retrieves the EntityPlayer player field from a PlayerConnection.
	 *
	 * @param playerConnection The PlayerConnection object from which to retrieve the EntityPlayer field.
	 * @return The value of the EntityPlayer field in the PlayerConnection.
	 */
	public static Object getPlayerFromConnection(Object playerConnection) {
		if (CONNECTION_ENTITY_ACCESSOR == null) {
			Class<?> connectionClass = MinecraftReflection.getPlayerConnectionClass();
			Class<?> entityPlayerClass = MinecraftReflection.getEntityPlayerClass();
			CONNECTION_ENTITY_ACCESSOR = Accessors.getFieldAccessor(connectionClass, entityPlayerClass, true);
		}

		return CONNECTION_ENTITY_ACCESSOR.get(playerConnection);
	}
}
