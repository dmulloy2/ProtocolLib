/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
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
package com.comphenix.protocol.reflect.cloning;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.google.common.collect.Maps;

/**
 * Represents an object that can clone a specific list of Bukkit- and Minecraft-related objects.
 * 
 * @author Kristian
 */
public class BukkitCloner implements Cloner {
	// List of classes we support
	private final Map<Integer, Class<?>> clonableClasses = Maps.newConcurrentMap();

	public BukkitCloner() {
		addClass(0, MinecraftReflection.getItemStackClass());
		addClass(1, MinecraftReflection.getDataWatcherClass());

		// Try to add position classes
		try {
			addClass(2, MinecraftReflection.getBlockPositionClass());
		} catch (Throwable ex) {
		}

		try {
			addClass(3, MinecraftReflection.getChunkPositionClass());
		} catch (Throwable ex) {
		}

		if (MinecraftReflection.isUsingNetty()) {
			addClass(4, MinecraftReflection.getServerPingClass());
		}

		if (MinecraftReflection.watcherObjectExists()) {
			addClass(5, MinecraftReflection.getDataWatcherSerializerClass());
			addClass(6, MinecraftReflection.getMinecraftKeyClass());
		}

		try {
			addClass(7, MinecraftReflection.getIBlockDataClass());
		} catch (Throwable ex) {
		}

		try {
			addClass(8, MinecraftReflection.getNonNullListClass());
		} catch (Throwable ex) {
		}

		try {
			addClass(9, MinecraftReflection.getNBTBaseClass());
		} catch (Throwable ex) { }
	}

	private void addClass(int id, Class<?> clazz) {
		if (clazz != null)
			clonableClasses.put(id, clazz);
	}

	private int findMatchingClass(Class<?> type) {
		// See if is a subclass of any of our supported superclasses
		for (Entry<Integer, Class<?>> entry : clonableClasses.entrySet()) {
			if (entry.getValue().isAssignableFrom(type)) {
				return entry.getKey();
			}
		}

		return -1;
	}

	@Override
	public boolean canClone(Object source) {
		if (source == null)
			return false;

		return findMatchingClass(source.getClass()) >= 0;
	}

	@Override
	public Object clone(Object source) {
		if (source == null)
			throw new IllegalArgumentException("source cannot be NULL.");

		// Convert to a wrapper
		switch (findMatchingClass(source.getClass())) {
			case 0:
				return MinecraftReflection.getMinecraftItemStack(MinecraftReflection.getBukkitItemStack(source).clone());
			case 1:
				EquivalentConverter<WrappedDataWatcher> dataConverter = BukkitConverters.getDataWatcherConverter();
				return dataConverter.getGeneric(dataConverter.getSpecific(source).deepClone());
			case 2:
				EquivalentConverter<BlockPosition> blockConverter = BlockPosition.getConverter();
				return blockConverter.getGeneric(blockConverter.getSpecific(source));
			case 3:
				EquivalentConverter<ChunkPosition> chunkConverter = ChunkPosition.getConverter();
				return chunkConverter.getGeneric(chunkConverter.getSpecific(source));
			case 4:
				EquivalentConverter<WrappedServerPing> serverConverter = BukkitConverters.getWrappedServerPingConverter();
				return serverConverter.getGeneric(serverConverter.getSpecific(source).deepClone());
			case 5:
				return source;
			case 6:
				EquivalentConverter<MinecraftKey> keyConverter = MinecraftKey.getConverter();
				return keyConverter.getGeneric(keyConverter.getSpecific(source));
			case 7:
				EquivalentConverter<WrappedBlockData> blockDataConverter = BukkitConverters.getWrappedBlockDataConverter();
				return blockDataConverter.getGeneric(blockDataConverter.getSpecific(source).deepClone());
			case 8:
				return nonNullListCloner().clone(source);
			case 9:
				NbtWrapper<?> clone = (NbtWrapper<?>) NbtFactory.fromNMS(source).deepClone();
				return clone.getHandle();
			default:
				throw new IllegalArgumentException("Cannot clone objects of type " + source.getClass());
		}
	}

	private static Constructor<?> nonNullList = null;

	private static final Cloner nonNullListCloner() {
		return new Cloner() {
			@Override
			public boolean canClone(Object source) {
				return MinecraftReflection.is(MinecraftReflection.getNonNullListClass(), source);
			}

			@Override
			public Object clone(Object source) {
				StructureModifier<Object> modifier = new StructureModifier<>(source.getClass(), true).withTarget(source);
				List<?> list = (List<?>) modifier.read(0);
				Object empty = modifier.read(1);

				if (nonNullList == null) {
					try {
						nonNullList = source.getClass().getDeclaredConstructor(List.class, Object.class);
						nonNullList.setAccessible(true);
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException("Could not find NonNullList constructor", ex);
					}
				}

				try {
					return nonNullList.newInstance(new ArrayList<>(list), empty);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Could not create new NonNullList", ex);
				}
			}
		};
	}
}
