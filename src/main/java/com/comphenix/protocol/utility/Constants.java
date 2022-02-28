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

/**
 * @author dmulloy2
 */

public final class Constants {
	public static final String PACKAGE_VERSION = "v1_18_R2";
	public static final String NMS = "net.minecraft";
	public static final String OBC = "org.bukkit.craftbukkit." + PACKAGE_VERSION;
	public static final MinecraftVersion CURRENT_VERSION = MinecraftVersion.CAVES_CLIFFS_2;

	public static void init() {
		MinecraftReflection.setMinecraftPackage(NMS, OBC);
		MinecraftVersion.setCurrentVersion(CURRENT_VERSION);
	}
}
