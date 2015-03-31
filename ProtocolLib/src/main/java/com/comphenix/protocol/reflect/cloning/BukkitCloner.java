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

import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.ChunkPosition;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedServerPing;
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
				return dataConverter.getGeneric(clonableClasses.get(1), dataConverter.getSpecific(source).deepClone());
			case 2:
				EquivalentConverter<BlockPosition> blockConverter = BlockPosition.getConverter();
				return blockConverter.getGeneric(clonableClasses.get(2), blockConverter.getSpecific(source));
			case 3:
				EquivalentConverter<ChunkPosition> chunkConverter = ChunkPosition.getConverter();
				return chunkConverter.getGeneric(clonableClasses.get(3), chunkConverter.getSpecific(source));
			case 4:
				EquivalentConverter<WrappedServerPing> serverConverter = BukkitConverters.getWrappedServerPingConverter();
				return serverConverter.getGeneric(clonableClasses.get(4), serverConverter.getSpecific(source).deepClone());
			default:
				throw new IllegalArgumentException("Cannot clone objects of type " + source.getClass());
		}
	}
}