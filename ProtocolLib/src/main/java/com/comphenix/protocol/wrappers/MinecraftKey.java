/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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
package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.StructureModifier;

/**
 * @author dmulloy2
 */

public class MinecraftKey {
	private final String prefix;
	private final String key;

	public MinecraftKey(String prefix, String key) {
		this.prefix = prefix;
		this.key = key;
	}

	public MinecraftKey(String key) {
		this("minecraft", key);
	}

	public static MinecraftKey fromHandle(Object handle) {
		StructureModifier<String> modifier = new StructureModifier<String>(handle.getClass()).withTarget(handle).withType(String.class);
		return new MinecraftKey(modifier.read(0), modifier.read(1));
	}

	public static MinecraftKey fromEnum(Enum<?> value) {
		return new MinecraftKey(value.name().toLowerCase().replace("_", "."));
	}

	public String getPrefix() {
		return prefix;
	}

	public String getKey() {
		return key;
	}

	public String getFullKey() {
		return prefix + ":" + key;
	}

	public String getEnumFormat() {
		return key.toUpperCase().replace(".", "_");
	}
}