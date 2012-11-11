package com.comphenix.protocol.wrappers;

import org.bukkit.util.Vector;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.base.Objects;

/**
 * Wraps a immutable net.minecraft.server.ChunkPosition, which represents a integer 3D vector.
 * 
 * @author Kristian
 */
public class ChunkPosition {
	// Use protected members, like Bukkit
	protected final int x;
	protected final int y;
	protected final int z;
	
	// Used to access a ChunkPosition, in case it's names are changed
	private static StructureModifier<Integer> intModifier;

	/**
	 * Construct an immutable 3D vector.
	 */
	public ChunkPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Construct an immutable integer 3D vector from a mutable Bukkit vector.
	 * @param vector - the mutable real Bukkit vector to copy.
	 */
	public ChunkPosition(Vector vector) {
		if (vector == null)
			throw new IllegalArgumentException("Vector cannot be NULL.");
		this.x = vector.getBlockX();
		this.y = vector.getBlockY();
		this.z = vector.getBlockZ();
	}

	/**
	 * Convert this instance to an equivalent real 3D vector.
	 * @return Real 3D vector.
	 */
	public Vector toVector() {
		return new Vector(x, y, z);
	}
	
	/**
	 * Retrieve the x-coordinate.
	 * @return X coordinate.
	 */
	public int getX() {
		return x;
	}
	
	/**
	 * Retrieve the y-coordinate.
	 * @return Y coordinate.
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Retrieve the z-coordinate.
	 * @return Z coordinate.
	 */
	public int getZ() {
		return z;
	}
	
	/**
	 * Used to convert between NMS ChunkPosition and the wrapper instance.
	 * @return
	 */
	public static EquivalentConverter<ChunkPosition> getConverter() {
		return new EquivalentConverter<ChunkPosition>() {
			@Override
			public Object getGeneric(ChunkPosition specific) {
				return new net.minecraft.server.ChunkPosition(specific.x, specific.z, specific.z);
			}
			
			@Override
			public ChunkPosition getSpecific(Object generic) {
				if (generic instanceof net.minecraft.server.ChunkPosition) {
					net.minecraft.server.ChunkPosition other = (net.minecraft.server.ChunkPosition) generic;
					
					try {
						if (intModifier == null)
							return new ChunkPosition(other.x, other.y, other.z);
					} catch (LinkageError e) {
						// It could happen. If it does, use a structure modifier instead
						intModifier = new StructureModifier<Object>(other.getClass(), null, false).withType(int.class);
						
						// Damn it all
						if (intModifier.size() < 3) {
							throw new IllegalStateException("Cannot read class " + other.getClass() + " for its integer fields.");
						}
					}
					
					if (intModifier.size() >= 3) {
						try {
							return new ChunkPosition(intModifier.read(0), intModifier.read(1), intModifier.read(2));
						} catch (FieldAccessException e) {
							// This is an exeptional work-around, so we don't want to burden the caller with the messy details
							throw new RuntimeException("Field access error.", e);
						} 
					}
				}
				
				// Otherwise, return NULL
				return null;
			}

			// Thanks Java Generics!
			@Override
			public Class<ChunkPosition> getSpecificType() {
				return ChunkPosition.class;
			}
		};
	}
		
	@Override
	public boolean equals(Object obj) {
		// Fast checks
		if (this == obj) return true;
		if (obj == null) return false;
		
		// Only compare objects of similar type
		if (obj instanceof ChunkPosition) {
			ChunkPosition other = (ChunkPosition) obj;
			return x == other.x && y == other.y && z == other.z;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(x, y, z);
	}
}
