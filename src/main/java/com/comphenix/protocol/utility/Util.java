/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.utility;

/**
 * General utility class
 *
 * @author dmulloy2
 */
public final class Util {

	private static final boolean SPIGOT = classExists("org.spigotmc.SpigotConfig");
	private static Class<?> cachedBundleClass;

	public static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	/**
	 * Whether this server is running Spigot or a Spigot fork. This works by checking if the SpigotConfig exists, which
	 * should be true of all forks.
	 *
	 * @return True if it is, false if not.
	 */
	public static boolean isUsingSpigot() {
		return SPIGOT;
	}

	/**
	 * Checks if the server is getting reloaded by walking down the current thread stack trace.
	 *
	 * @return true if the server is getting reloaded, false otherwise.
	 */
	public static boolean isCurrentlyReloading() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			String clazz = element.getClassName();
			if (clazz.startsWith("org.bukkit.craftbukkit.")
					&& clazz.endsWith(".CraftServer")
					&& element.getMethodName().equals("reload")) {
				return true;
			}
		}
		return false;
	}
}
