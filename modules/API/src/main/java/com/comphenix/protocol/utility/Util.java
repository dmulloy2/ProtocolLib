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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
		List<E> list = new ArrayList<E>(elements.length);
		for (E element : elements) {
			list.add(element);
		}

		return list;
	}

	/**
	 * Whether or not this server is running Spigot. This works by checking
	 * the server version for the String "Spigot"
	 * @return True if it is, false if not.
	 */
	public static boolean isUsingSpigot() {
		return Bukkit.getServer().getVersion().contains("Spigot");
	}
}