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

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

import java.lang.reflect.Constructor;
import java.util.Locale;

/**
 * Represents a MinecraftKey in 1.9.
 * <p>
 * Keys are in the format {@code prefix:key}
 * 
 * @author dmulloy2
 */

public class MinecraftKey {
	private final String prefix;
	private final String key;

	/**
	 * Constructs a new key with a given prefix and key.
	 * 
	 * @param prefix The prefix, usually minecraft.
	 * @param key The key, the part we care about
	 */
	public MinecraftKey(String prefix, String key) {
		this.prefix = prefix;
		this.key = key;
	}

	/**
	 * Constructs a new key with minecraft prefix and a key.
	 * @param key The key
	 */
	public MinecraftKey(String key) {
		this("minecraft", key);
	}

	/**
	 * Creates a MinecraftKey wrapper from a Minecraft handle.
	 * @param handle The handle
	 * @return The resulting key
	 */
	public static MinecraftKey fromHandle(Object handle) {
		StructureModifier<String> modifier = new StructureModifier<String>(handle.getClass()).withTarget(handle).withType(String.class);
		return new MinecraftKey(modifier.read(0), modifier.read(1));
	}

	/**
	 * Creates a MinecraftKey wrapper from an Enum constant. The resulting key
	 * is lower case, with underscores replaced by periods.
	 * @param value The value
	 * @return The resulting key
	 * @deprecated This isn't accurate in all cases
	 */
	@Deprecated
	public static MinecraftKey fromEnum(Enum<?> value) {
		return new MinecraftKey(value.name().toLowerCase(Locale.ENGLISH).replace("_", "."));
	}

	/**
	 * Gets the prefix of this MinecraftKey. It is minecraft by default.
	 * @return The prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Gets the key of this MinecraftKey. It is generally the important part.
	 * @return The key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the full key of this MinecraftKey. It is in the format of
	 * {@code prefix:key}
	 * @return The full key
	 */
	public String getFullKey() {
		return prefix + ":" + key;
	}

	/**
	 * Returns this key back into Enum format, upper case with periods replaced
	 * by underscores.
	 * @return The enum format
	 * @deprecated This isn't accurate in all cases
	 */
	@Deprecated
	public String getEnumFormat() {
		return key.toUpperCase(Locale.ENGLISH).replace(".", "_");
	}

	private static Constructor<?> constructor = null;

	public static EquivalentConverter<MinecraftKey> getConverter() {
		return new EquivalentConverter<MinecraftKey>() {
			@Override
			public MinecraftKey getSpecific(Object generic) {
				return MinecraftKey.fromHandle(generic);
			}

			@Override
			public Object getGeneric(MinecraftKey specific) {
				if (constructor == null) {
					try {
						constructor = MinecraftReflection.getMinecraftKeyClass().getConstructor(String.class, String.class);
					} catch (ReflectiveOperationException e) {
						throw new RuntimeException("Failed to obtain MinecraftKey constructor", e);
					}
				}

				try {
					return constructor.newInstance(specific.getPrefix(), specific.getKey());
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Failed to create new MinecraftKey", e);
				}
			}

			@Override
			public Class<MinecraftKey> getSpecificType() {
				return MinecraftKey.class;
			}
		};
	}
}
