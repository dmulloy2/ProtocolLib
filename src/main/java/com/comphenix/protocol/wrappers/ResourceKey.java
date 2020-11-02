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

import java.lang.reflect.Constructor;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a ResourceKey in 1.16
 * <p>
 * Keys are in the format of a doubled {@link MinecraftKey}
 * 
 * {@code prefix.getFullKey():key.getFullKey()}
 * 
 * @author Peng1104
 */

public class ResourceKey {
	private MinecraftKey prefix;
	private MinecraftKey key;
	
	/**
	 * Constructs a new key with a given {@link MinecraftKey} prefix and {@link MinecraftKey}.
	 * 
	 * @param prefix The {@link MinecraftKey} prefix, usually a identifier.
	 * @param key The {@link MinecraftKey} key, the part we care about
	 */
	public ResourceKey(MinecraftKey prefix, MinecraftKey key) {
		this.prefix = prefix;
		this.key = key;
	}
	
	/**
	 * Constructs a new key with a given prefix and key.
	 * 
	 * @param prefix The prefix, usually a identifier.
	 * @param key The key, the part we care about
	 */
	public ResourceKey(String prefix, String key) {
		this(new MinecraftKey(prefix), new MinecraftKey(key));
	}
	
	/**
	 * Constructs a new key with root prefix and a key.
	 * 
	 * @param key The key
	 */
	public ResourceKey(String key) {
		this(new MinecraftKey(key));
	}
	
	/**
	 * Constructs a new key with root prefix and a {@link MinecraftKey}.
	 * 
	 * @param key The {@link MinecraftKey}
	 */
	public ResourceKey(MinecraftKey key) {
		this(new MinecraftKey("root"), key);
	}
	
	/**
	 * Creates a ResourceKey wrapper from a Minecraft handle.
	 * 
	 * @param handle The handle
	 * 
	 * @return The resulting key
	 */
	public static ResourceKey fromHandle(Object handle) {
		StructureModifier<Object> modifier = new StructureModifier<Object>(handle.getClass()).withTarget(handle).withType(MinecraftReflection.getMinecraftKeyClass());
		return new ResourceKey(MinecraftKey.fromHandle(modifier.read(0)), MinecraftKey.fromHandle(modifier.read(1)));
	}
	
	/**
	 * Gets the prefix of this ResourceKey. It is minecraft:root by default.
	 * 
	 * @return The {@link MinecraftKey} prefix
	 */
	public MinecraftKey getPrefix() {
		return prefix;
	}
	
	/**
	 * Gets the {@link MinecraftKey} of this ResourceKey. It is generally the important part.
	 * 
	 * @return The {@link MinecraftKey}
	 */
	public MinecraftKey getKey() {
		return key;
	}
	
	/**
	 * Gets the full key of this ResourceKey. It is in the format of {@code prefix.getFullKey():key.getFullKey()}
	 * 
	 * @return The full key
	 */
	public String getFullKey() {
		return prefix.getFullKey() + ':' + key.getFullKey();
	}
	
	@Override
	public String toString() {
		return "ResourceKey[" + prefix.getFullKey() + " / " + key.getFullKey() + ']';
	}
	
	private static final EquivalentConverter<MinecraftKey> MINECRAFT_KEY_CONVERTER = MinecraftKey.getConverter();
	
	private static Constructor<?> constructor = null;
	
	public static EquivalentConverter<ResourceKey> getConverter() {
		return new EquivalentConverter<ResourceKey>() {
			
			@Override
			public ResourceKey getSpecific(Object generic) {
				return ResourceKey.fromHandle(generic);
			}
			
			@Override
			public Object getGeneric(ResourceKey specific) {
				if (constructor == null) {
					try {
						constructor = MinecraftReflection.getResourceKeyClass().getConstructor(MinecraftReflection.getMinecraftKeyClass(), MinecraftReflection.getMinecraftKeyClass());
						constructor.setAccessible(true);
					}
					catch (ReflectiveOperationException e) {
						throw new RuntimeException("Failed to obtain ResourceKey constructor", e);
					}
				}
				try {
					return constructor.newInstance(MINECRAFT_KEY_CONVERTER.getGeneric(specific.getPrefix()), MINECRAFT_KEY_CONVERTER.getGeneric(specific.getKey()));
				}
				catch (ReflectiveOperationException e) {
					throw new RuntimeException("Failed to create new ResourceKey", e);
				}
			}
			
			@Override
			public Class<ResourceKey> getSpecificType() {
				return ResourceKey.class;
			}
		};
	}
}