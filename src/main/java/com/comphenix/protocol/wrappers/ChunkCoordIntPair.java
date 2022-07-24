package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

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
			public Object getGeneric(ChunkCoordIntPair specific) {
				if (COORD_CONSTRUCTOR == null) {
					COORD_CONSTRUCTOR = Accessors.getConstructorAccessor(COORD_PAIR_CLASS, int.class, int.class);
				}
				
				return COORD_CONSTRUCTOR.invoke(specific.chunkX, specific.chunkZ);
			}
			
			@Override
			public ChunkCoordIntPair getSpecific(Object generic) {
				if (MinecraftReflection.isChunkCoordIntPair(generic)) {
					if (COORD_X == null || COORD_Z == null) {
						FuzzyReflection fuzzy = FuzzyReflection.fromClass(COORD_PAIR_CLASS, true);
						List<Field> fields = fuzzy.getFieldList(FuzzyFieldContract
								.newBuilder()
								.banModifier(Modifier.STATIC)
								.typeExact(int.class)
								.build());
						COORD_X = Accessors.getFieldAccessor(fields.get(0));
						COORD_Z = Accessors.getFieldAccessor(fields.get(1));
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
