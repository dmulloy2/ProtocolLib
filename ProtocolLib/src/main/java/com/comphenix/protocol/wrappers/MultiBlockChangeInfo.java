/**
 * (c) 2015 dmulloy2
 */
package com.comphenix.protocol.wrappers;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

import org.bukkit.Location;
import org.bukkit.World;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Represents a single block change.
 * 
 * @author dmulloy2
 */

public class MultiBlockChangeInfo {
	private short location;
	private WrappedBlockData data;
	private ChunkCoordIntPair chunk;

	public MultiBlockChangeInfo(short location, WrappedBlockData data, ChunkCoordIntPair chunk) {
		this.location = location;
		this.data = data;
		this.chunk = chunk;
	}

	/**
	 * Returns this block change's absolute Location in a given World.
	 * 
	 * @param world World for the location
	 * @return This block change's absolute Location
	 */
	public Location getLocation(World world) {
		return new Location(world, getX(), getY(), getZ());
	}

	/**
	 * Sets this block change's absolute Location.
	 * 
	 * @param location This block change's new location
	 */
	public void setLocation(Location location) {
		int x = location.getBlockX() - getChunkX() << 4;
		int y = location.getBlockY();
		int z = location.getBlockZ() - getChunkZ() << 4;

		this.location = (short) (x << 12 | z << 8 | y);
	}

	/**
	 * Gets this block change's absolute x coordinate
	 * 
	 * @return X coordinate
	 */
	public int getX() {
		return getChunkX() + (location >> 12 & 15);
	}

	/**
	 * Sets this block change's absolute x coordinate
	 * 
	 * @param x New x coordinate
	 */
	public void setX(int x) {
		x -= getChunkX() << 4;
		this.location = (short) (x << 12 | getZ() << 8 | getY());
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
		this.location = (short) (getX() << 12 | getZ() << 8 | y);
	}

	/**
	 * Gets this block change's absolute z coordinate.
	 * 
	 * @return Z coordinate
	 */
	public int getZ() {
		return getChunkZ() + (location >> 8 & 15);
	}

	/**
	 * Sets this block change's absolute z coordinate.
	 * 
	 * @param z New z coordinate
	 */
	public void setZ(int z) {
		z -= getChunkZ() << 4;
		this.location = (short) (getX() << 12 | getZ() << 8 | getY());
	}

	private int getChunkX() {
		return chunk.getChunkX() << 4;
	}

	private int getChunkZ() {
		return chunk.getChunkZ() << 4;
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
				StructureModifier<Object> modifier = new StructureModifier<Object>(generic.getClass(), null, false).withTarget(generic);

				StructureModifier<Short> shorts = modifier.withType(short.class);
				short location = shorts.read(0);

				StructureModifier<WrappedBlockData> dataModifier = modifier.withType(MinecraftReflection.getIBlockDataClass(),
						BukkitConverters.getWrappedBlockDataConverter());
				WrappedBlockData data = dataModifier.read(0);

				return new MultiBlockChangeInfo(location, data, chunk);
			}

			@Override
			public Object getGeneric(Class<?> genericType, MultiBlockChangeInfo specific) {
				try {
					Constructor<?> constructor = MinecraftReflection.getMultiBlockChangeInfoClass().getConstructor(
							short.class,
							MinecraftReflection.getIBlockDataClass()
					);

					return constructor.newInstance(
							specific.location,
							BukkitConverters.getWrappedBlockDataConverter().getGeneric(MinecraftReflection.getIBlockDataClass(), specific.data)
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

	public static EquivalentConverter<MultiBlockChangeInfo[]> getArrayConverter(final ChunkCoordIntPair chunk) {
		return new EquivalentConverter<MultiBlockChangeInfo[]>() {
			private final EquivalentConverter<MultiBlockChangeInfo> converter = MultiBlockChangeInfo.getConverter(chunk);

			@Override
			public MultiBlockChangeInfo[] getSpecific(Object generic) {
				Object[] array = (Object[]) generic;
				MultiBlockChangeInfo[] result = new MultiBlockChangeInfo[array.length];

				// Unwrap every item
				for (int i = 0; i < result.length; i++) {
					result[i] = converter.getSpecific(array[i]);
				}

				return result;
			}

			@Override
			public Object getGeneric(Class<?> genericType, MultiBlockChangeInfo[] specific) {
				Object[] result = (Object[]) Array.newInstance(genericType, specific.length);

				// Wrap every item
				for (int i = 0; i < result.length; i++) {
					result[i] = converter.getGeneric(genericType, specific[i]);
				}

				return result;
			}

			@Override
			public Class<MultiBlockChangeInfo[]> getSpecificType() {
				return MultiBlockChangeInfo[].class;
			}
		};
	}
}