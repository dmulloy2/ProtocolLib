/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General utility class
 * @author dmulloy2
 */
public class Util {

	/**
	 * Gets a list of currently online Players.
	 * @return The list
	 */
	@SuppressWarnings("unchecked")
	public static List<Player> getOnlinePlayers() {
		return (List<Player>) Bukkit.getOnlinePlayers();
	}

	/**
	 * Converts a variable argument array into a List.
	 * @param elements Array to convert
	 * @return The list
	 */
	@SafeVarargs
	public static <E> List<E> asList(E... elements) {
		List<E> list = new ArrayList<>(elements.length);
		list.addAll(Arrays.asList(elements));
		return list;
	}

	public static boolean classExists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	private static final boolean spigot = classExists("org.spigotmc.SpigotConfig");

	/**
	 * Whether or not this server is running Spigot or a Spigot fork. This works by checking
	 * if the SpigotConfig exists, which should be true of all forks.
	 * @return True if it is, false if not.
	 */
	public static boolean isUsingSpigot() {
		return spigot;
	}
}
