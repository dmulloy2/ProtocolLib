package com.comphenix.protocol.utility;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Utility methods relating to Bukkit.
 * @author dmulloy2
 */

public class BukkitUtil {

	private static Method getOnlinePlayers;

	/**
	 * Gets a list of online {@link Player}s. This also provides backwards
	 * compatibility as Bukkit changed <code>getOnlinePlayers</code>.
	 *
	 * @return A list of online Players
	 */
	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers() {
		try {
			if (getOnlinePlayers == null)
				getOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers");
			if (getOnlinePlayers.getReturnType() != Collection.class)
				return Arrays.asList((Player[]) getOnlinePlayers.invoke(null));
		} catch (Throwable ex) {
		}
		return (List<Player>) Bukkit.getOnlinePlayers();
	}
}