package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

/**
 * Represents a ChunkCoordIntPair.
 * @author Kristian
 */
public class ChunkCoordIntPair {
	private static Class<?> COORD_PAIR_CLASS = MinecraftReflection.getChunkCoordIntPair();
	private static ConstructorAccessor COORD_CONSTRUCTOR;
	private static FieldAccessor COORD_X;
	private static FieldAccessor COORD_Z;
	
	// Use protected members, like Bukkit
	protected final int chunkX;
	protected final int chunkZ;
	
	/**
	 * Construct a new chunk coord int pair.
	 * @param x - the x index of the chunk.
	 * @param z - the z index of the chunk.
	 */
	public ChunkCoordIntPair(int x, int z) {
		this.chunkX = x;
		this.chunkZ = z;
	}
	
	/**
	 * Retrieve the equivalent chunk position.
	 * @param y - the y position.
	 * @return The chunk position.
	 */
	public ChunkPosition getPosition(int y) {
		return new ChunkPosition((chunkX << 4) + 8, y, (chunkZ << 4) + 8);
	}
	
	/**
	 * Retrieve the chunk index in the x-dimension.
	 * <p>
	 * This is the number of adjacent chunks to (0, 0), not a block coordinate.
	 * @return The x chunk index.
	 */
	public int getChunkX() {
		return chunkX;
	}
	
	/**
	 * Retrieve the chunk index in the z-dimension.
	 * <p>
	 * This is the number of adjacent chunks to (0, 0), not a block coordinate.
	 * @return The z chunk index.
	 */
	public int getChunkZ() {
		return chunkZ;
	}
	
	/**
	 * Used to convert between NMS ChunkPosition and the wrapper instance.
	 * @return A new converter.
	 */
	public static EquivalentConverter<ChunkCoordIntPair> getConverter() {
		return new EquivalentConverter<ChunkCoordIntPair>() {
			@Override
			public Object getGeneric(Class<?> genericType, ChunkCoordIntPair specific) {
				if (COORD_CONSTRUCTOR == null) {
					COORD_CONSTRUCTOR = Accessors.getConstructorAccessor(COORD_PAIR_CLASS, int.class, int.class);
				}
				
				return COORD_CONSTRUCTOR.invoke(specific.chunkX, specific.chunkZ);
			}
			
			@Override
			public ChunkCoordIntPair getSpecific(Object generic) {
				if (MinecraftReflection.isChunkCoordIntPair(generic)) {
					if (COORD_X == null || COORD_Z == null) {
						FieldAccessor[] ints = Accessors.getFieldAccessorArray(COORD_PAIR_CLASS, int.class, true);
						COORD_X = ints[0];
						COORD_Z = ints[1];
					}
					return new ChunkCoordIntPair((Integer) COORD_X.get(generic), (Integer) COORD_Z.get(generic));
				}
				
				// Otherwise, return NULL
				return null;
			}

			@Override
			public Class<ChunkCoordIntPair> getSpecificType() {
				return ChunkCoordIntPair.class;
			}
		};
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		// Only compare objects of similar type
		if (obj instanceof ChunkCoordIntPair) {
			ChunkCoordIntPair other = (ChunkCoordIntPair) obj;
			return chunkX == other.chunkX && chunkZ == other.chunkZ;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(chunkX, chunkZ);
	}

	@Override
	public String toString() {
		return "ChunkCoordIntPair [x=" + chunkX + ", z=" + chunkZ + "]";
	}
}
