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
import org.bukkit.util.Vector;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

/**
 * Copies a immutable net.minecraft.server.BlockPosition, which represents a integer 3D vector.
 * 
 * @author dmulloy2
 */
public class BlockPosition {
	
	/**
	 * Represents the null (0, 0, 0) origin.
	 */
	public static BlockPosition ORIGIN = new BlockPosition(0, 0, 0);
	
	private static Constructor<?> blockPositionConstructor;

	// Use protected members, like Bukkit
	protected final int x;
	protected final int y;
	protected final int z;
	
	// Used to access a BlockPosition, in case it's names are changed
	private static StructureModifier<Integer> intModifier;

	/**
	 * Construct an immutable 3D vector.
	 * @param x - x coordinate
	 * @param y - y coordinate
	 * @param z - z coordinate
	 */
	public BlockPosition(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Construct an immutable integer 3D vector from a mutable Bukkit vector.
	 * @param vector - the mutable real Bukkit vector to copy.
	 */
	public BlockPosition(Vector vector) {
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
	 * Convert this instance to an equivalent Location.
	 * @param world World for the location
	 * @return Location
	 */
	public Location toLocation(World world) {
		return new Location(world, x, y, z);
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
	public BlockPosition add(BlockPosition other) {
		if (other == null)
			throw new IllegalArgumentException("other cannot be NULL");
		return new BlockPosition(x + other.x, y + other.y, z + other.z);
	}
	
	/**
	 * Adds the current position and a given position together, producing a result position.
	 * @param other - the other position.
	 * @return The new result position.
	 */
	public BlockPosition subtract(BlockPosition other) {
		if (other == null)
			throw new IllegalArgumentException("other cannot be NULL");
		return new BlockPosition(x - other.x, y - other.y, z - other.z);
	}
	
	/**
	 * Multiply each dimension in the current position by the given factor.
	 * @param factor - multiplier.
	 * @return The new result.
	 */
	public BlockPosition multiply(int factor) {
		return new BlockPosition(x * factor, y * factor, z * factor);
	}
	
	/**
	 * Divide each dimension in the current position by the given divisor.
	 * @param divisor - the divisor.
	 * @return The new result.
	 */
	public BlockPosition divide(int divisor) {
		if (divisor == 0)
			throw new IllegalArgumentException("Cannot divide by null.");
		return new BlockPosition(x / divisor, y / divisor, z / divisor);
	}
	
	/**
	 * Used to convert between NMS ChunkPosition and the wrapper instance.
	 * @return A new converter.
	 */
	public static EquivalentConverter<BlockPosition> getConverter() {
		return new EquivalentConverter<BlockPosition>() {
			@Override
			public Object getGeneric(Class<?> genericType, BlockPosition specific) {
				if (blockPositionConstructor == null) {
					try {
						blockPositionConstructor = MinecraftReflection.getBlockPositionClass().
							getConstructor(int.class, int.class, int.class);
					} catch (Exception e) {
						throw new RuntimeException("Cannot find block position constructor.", e);
					}
				}
				
				// Construct the underlying BlockPosition
				try {
					Object result = blockPositionConstructor.newInstance(specific.x, specific.y, specific.z);
					return result;
				} catch (Exception e) {
					throw new RuntimeException("Cannot construct BlockPosition.", e);
				}
			}
			
			@Override
			public BlockPosition getSpecific(Object generic) {
				if (MinecraftReflection.isBlockPosition(generic)) {
					// Use a structure modifier
					intModifier = new StructureModifier<Object>(generic.getClass(), null, false).withType(int.class);
					
					// Damn it all
					if (intModifier.size() < 3) {
						throw new IllegalStateException("Cannot read class " + generic.getClass() + " for its integer fields.");
					}
					
					if (intModifier.size() >= 3) {
						try {
							StructureModifier<Integer> instance = intModifier.withTarget(generic);
							BlockPosition result = new BlockPosition(instance.read(0), instance.read(1), instance.read(2));
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
			public Class<BlockPosition> getSpecificType() {
				return BlockPosition.class;
			}
		};
	}
		
	@Override
	public boolean equals(Object obj) {
		// Fast checks
		if (this == obj) return true;
		if (obj == null) return false;
		
		// Only compare objects of similar type
		if (obj instanceof BlockPosition) {
			BlockPosition other = (BlockPosition) obj;
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
		return "BlockPosition [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}
