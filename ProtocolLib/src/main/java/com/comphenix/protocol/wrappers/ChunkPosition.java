/*
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

package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;

import org.bukkit.util.Vector;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

/**
 * Copies a immutable net.minecraft.server.ChunkPosition, which represents a integer 3D vector.
 * 
 * @author Kristian
 */
public class ChunkPosition {
	
	/**
	 * Represents the null (0, 0, 0) origin.
	 */
	public static ChunkPosition ORIGIN = new ChunkPosition(0, 0, 0);
	
	private static Constructor<?> chunkPositionConstructor;

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
	 * Adds the current position and a given position together, producing a result position.
	 * @param other - the other position.
	 * @return The new result position.
	 */
	public ChunkPosition add(ChunkPosition other) {
		if (other == null)
			throw new IllegalArgumentException("other cannot be NULL");
		return new ChunkPosition(x + other.x, y + other.y, z + other.z);
	}
	
	/**
	 * Adds the current position and a given position together, producing a result position.
	 * @param other - the other position.
	 * @return The new result position.
	 */
	public ChunkPosition subtract(ChunkPosition other) {
		if (other == null)
			throw new IllegalArgumentException("other cannot be NULL");
		return new ChunkPosition(x - other.x, y - other.y, z - other.z);
	}
	
	/**
	 * Multiply each dimension in the current position by the given factor.
	 * @param factor - multiplier.
	 * @return The new result.
	 */
	public ChunkPosition multiply(int factor) {
		return new ChunkPosition(x * factor, y * factor, z * factor);
	}
	
	/**
	 * Divide each dimension in the current position by the given divisor.
	 * @param divisor - the divisor.
	 * @return The new result.
	 */
	public ChunkPosition divide(int divisor) {
		if (divisor == 0)
			throw new IllegalArgumentException("Cannot divide by null.");
		return new ChunkPosition(x / divisor, y / divisor, z / divisor);
	}
	
	/**
	 * Used to convert between NMS ChunkPosition and the wrapper instance.
	 * @return A new converter.
	 */
	public static EquivalentConverter<ChunkPosition> getConverter() {
		return new EquivalentConverter<ChunkPosition>() {
			@Override
			public Object getGeneric(Class<?> genericType, ChunkPosition specific) {
				if (chunkPositionConstructor == null) {
					try {
						chunkPositionConstructor = MinecraftReflection.getChunkPositionClass().
							getConstructor(int.class, int.class, int.class);
					} catch (Exception e) {
						throw new RuntimeException("Cannot find chunk position constructor.", e);
					}
				}
				
				// Construct the underlying ChunkPosition
				try {
					Object result = chunkPositionConstructor.newInstance(specific.x, specific.y, specific.z);
					return result;
				} catch (Exception e) {
					throw new RuntimeException("Cannot construct ChunkPosition.", e);
				}
			}
			
			@Override
			public ChunkPosition getSpecific(Object generic) {
				if (MinecraftReflection.isChunkPosition(generic)) {
					// Use a structure modifier 
					intModifier = new StructureModifier<Object>(generic.getClass(), null, false).withType(int.class);
					
					// Damn it all
					if (intModifier.size() < 3) {
						throw new IllegalStateException("Cannot read class " + generic.getClass() + " for its integer fields.");
					}
					
					if (intModifier.size() >= 3) {
						try {
							StructureModifier<Integer> instance = intModifier.withTarget(generic);
							ChunkPosition result = new ChunkPosition(instance.read(0), instance.read(1), instance.read(2));
							return result;
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

	@Override
	public String toString() {
		return "WrappedChunkPosition [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
