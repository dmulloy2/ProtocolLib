package com.comphenix.protocol.utility;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;

/**
 * General utility class
 * 
 * @author dmulloy2
 */

public class Util {
	private static MethodAccessor getOnlinePlayers;
	private static boolean reflectionRequired;

	static {
		try {
			Method method = Bukkit.class.getMethod("getOnlinePlayers");
			getOnlinePlayers = Accessors.getMethodAccessor(method);
			reflectionRequired = !method.getReturnType().isAssignableFrom(Collection.class);
		} catch (Throwable ex) {
			throw new RuntimeException("Failed to obtain getOnlinePlayers method.", ex);
		}
	}

	/**
	 * Gets a list of online {@link Player}s. This also provides backwards
	 * compatibility as Bukkit changed <code>getOnlinePlayers</code>.
	 *
	 * @return A list of currently online Players
	 */
	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers() {
		if (reflectionRequired) {
			return Arrays.asList((Player[]) getOnlinePlayers.invoke(null));
		}

		return (List<Player>) Bukkit.getOnlinePlayers();
	}
}