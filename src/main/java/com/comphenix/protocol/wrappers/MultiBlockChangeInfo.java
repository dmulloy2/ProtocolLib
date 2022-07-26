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
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a single block change.
 * 
 * @author dmulloy2
 */

public class MultiBlockChangeInfo {
	private static Constructor<?> constructor;
	private static Class<?> nmsClass = MinecraftReflection.getMultiBlockChangeInfoClass();

	private short location;
	private WrappedBlockData data;
	private ChunkCoordIntPair chunk;

	public MultiBlockChangeInfo(short location, WrappedBlockData data, ChunkCoordIntPair chunk) {
		this.location = location;
		this.data = data;
		this.chunk = chunk;
	}

	public MultiBlockChangeInfo(Location location, WrappedBlockData data) {
		this.data = data;
		this.chunk = new ChunkCoordIntPair(location.getBlockX() >> 4, location.getBlockZ() >> 4);
		this.setLocation(location);
	}

	/**
	 * Returns this block change's absolute Location in a given World.
	 * 
	 * @param world World for the location
	 * @return This block change's absolute Location
	 */
	public Location getLocation(World world) {
		return new Location(world, getAbsoluteX(), getY(), getAbsoluteZ());
	}

	/**
	 * Sets this block change's absolute Location.
	 * 
	 * @param location This block change's new location
	 */
	public void setLocation(Location location) {
		setLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	/**
	 * Sets this block change's absolute coordinates.
	 *
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public void setLocation(int x, int y, int z) {
		x = x & 15;
		z = z & 15;

		this.location = (short) (x << 12 | z << 8 | y);
	}

	/**
	 * Gets this block change's relative x coordinate.
	 * 
	 * @return Relative X coordinate
	 */
	public int getX() {
		return location >> 12 & 15;
	}

	/**
	 * Gets this block change's absolute x coordinate.
	 *
	 * @return Absolute X coordinate
	 */
	public int getAbsoluteX() {
		return (chunk.getChunkX() << 4) + getX();
	}

	/**
	 * Sets this block change's absolute x coordinate.
	 * 
	 * @param x New x coordinate
	 */
	public void setX(int x) {
		setLocation(x, getY(), getZ());
	}

	/**
	 * Gets this block change's y coordinate.
	 * 
	 * @return Y coordinate
	 */
	public int getY() {
		return location & 255;
	}

	/**
	 * Sets this block change's y coordinate
	 * 
	 * @param y New y coordinate
	 */
	public void setY(int y) {
		setLocation(getX(), y, getZ());
	}

	/**
	 * Gets this block change's relative z coordinate.
	 * 
	 * @return Relative Z coordinate
	 */
	public int getZ() {
		return location >> 8 & 15;
	}

	/**
	 * Gets this block change's absolute z coordinate.
	 *
	 * @return Absolute Z coordinate
	 */
	public int getAbsoluteZ() {
		return (chunk.getChunkZ() << 4) + getZ();
	}

	/**
	 * Sets this block change's relative z coordinate.
	 * 
	 * @param z New z coordinate
	 */
	public void setZ(int z) {
		setLocation(getX(), getY(), z);
	}

	/**
	 * Gets this block change's block data.
	 * 
	 * @return The block data
	 */
	public WrappedBlockData getData() {
		return data;
	}

	/**
	 * Sets this block change's block data.
	 * 
	 * @param data New block data
	 */
	public void setData(WrappedBlockData data) {
		this.data = data;
	}

	/**
	 * Gets the chunk this block change occured in.
	 * 
	 * @return The chunk
	 */
	public ChunkCoordIntPair getChunk() {
		return chunk;
	}

	public static EquivalentConverter<MultiBlockChangeInfo> getConverter(final ChunkCoordIntPair chunk) {
		return new EquivalentConverter<MultiBlockChangeInfo>() {

			@Override
			public MultiBlockChangeInfo getSpecific(Object generic) {
				StructureModifier<Object> modifier = new StructureModifier<>(generic.getClass(), null, false).withTarget(generic);

				StructureModifier<Short> shorts = modifier.withType(short.class);
				short location = shorts.read(0);

				StructureModifier<WrappedBlockData> dataModifier = modifier.withType(MinecraftReflection.getIBlockDataClass(),
						BukkitConverters.getWrappedBlockDataConverter());
				WrappedBlockData data = dataModifier.read(0);

				return new MultiBlockChangeInfo(location, data, chunk);
			}

			@Override
			public Object getGeneric(MultiBlockChangeInfo specific) {
				try {
					if (constructor == null) {
						constructor = nmsClass.getConstructor(
								PacketType.Play.Server.MULTI_BLOCK_CHANGE.getPacketClass(),
								short.class,
								MinecraftReflection.getIBlockDataClass()
						);
					}

					return constructor.newInstance(
							null,
							specific.location,
							BukkitConverters.getWrappedBlockDataConverter().getGeneric(specific.data)
					);
				} catch (Throwable ex) {
					throw new RuntimeException("Failed to construct MultiBlockChangeInfo instance.", ex);
				}
			}

			@Override
			public Class<MultiBlockChangeInfo> getSpecificType() {
				return MultiBlockChangeInfo.class;
			}
		};
	}
}
