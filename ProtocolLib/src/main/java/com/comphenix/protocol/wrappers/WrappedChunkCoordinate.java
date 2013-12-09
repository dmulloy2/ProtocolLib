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

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;

/**
 * Allows access to a chunk coordinate.
 * 
 * @author Kristian
 */
public class WrappedChunkCoordinate extends AbstractWrapper implements Comparable<WrappedChunkCoordinate> {
	/**
	 * If TRUE, NULLs should be put before non-null instances of this class.
	 */
	private static final boolean LARGER_THAN_NULL = true;

	// Used to access a ChunkCoordinate
	private static StructureModifier<Integer> SHARED_MODIFIER;
	
	// The current modifier
	private StructureModifier<Integer> handleModifier;
	
	/**
	 * Create a new empty wrapper.
	 */
	public WrappedChunkCoordinate() {
		super(MinecraftReflection.getChunkCoordinatesClass());
		
		try {	
			setHandle(getHandleType().newInstance());
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct chunk coordinate.");
		}
	}
	
	/**
	 * Create a wrapper for a specific chunk coordinates.
	 * @param handle - the NMS chunk coordinates.
	 */
	@SuppressWarnings("rawtypes")
	public WrappedChunkCoordinate(Comparable handle) {
		super(MinecraftReflection.getChunkCoordinatesClass());
		setHandle(handle);
	}

	// Ensure that the structure modifier is initialized
	private StructureModifier<Integer> getModifier() {
		if (SHARED_MODIFIER == null)
			SHARED_MODIFIER = new StructureModifier<Object>(handle.getClass(), null, false).withType(int.class);
		if (handleModifier == null)
			handleModifier = SHARED_MODIFIER.withTarget(handle);
		return handleModifier;
	}
	
	/**
	 * Create a wrapper with specific values.
	 * @param x - the x coordinate.
	 * @param y - the y coordinate.
	 * @param z - the z coordinate.
	 */
	public WrappedChunkCoordinate(int x, int y, int z) {
		this();
		setX(x);
		setY(y);
		setZ(z);
	}
	
	/**
	 * Create a chunk coordinate wrapper from a given position.
	 * @param position - the given position.
	 */
	public WrappedChunkCoordinate(ChunkPosition position) {
		this(position.getX(), position.getY(), position.getZ());
	}
	
	public Object getHandle() {
		return handle;
	}
	
	/**
	 * Retrieve the x coordinate of the underlying coordinate.
	 * @return The x coordinate.
	 */
	public int getX() {
		return getModifier().read(0);
	}
	
	/**
	 * Set the x coordinate of the underlying coordinate.
	 * @param newX - the new x coordinate.
	 */
	public void setX(int newX) {
		getModifier().write(0, newX);
	}
	
	/**
	 * Retrieve the y coordinate of the underlying coordinate.
	 * @return The y coordinate.
	 */
	public int getY() {
		return getModifier().read(1);
	}
	
	/**
	 * Set the y coordinate of the underlying coordinate.
	 * @param newY - the new y coordinate.
	 */
	public void setY(int newY) {
		getModifier().write(1, newY);
	}
	
	/**
	 * Retrieve the z coordinate of the underlying coordinate.
	 * @return The z coordinate.
	 */
	public int getZ() {
		return getModifier().read(2);
	}
	
	/**
	 * Set the z coordinate of the underlying coordiate.
	 * @param newZ - the new z coordinate.
	 */
	public void setZ(int newZ) {
		getModifier().write(2, newZ);
	}
	
	/**
	 * Create an immutable chunk position from this coordinate.
	 * @return The new immutable chunk position.
	 */
	public ChunkPosition toPosition() {
		return new ChunkPosition(getX(), getY(), getZ());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(WrappedChunkCoordinate other) {
		// We'll handle NULL objects too, unlike ChunkCoordinates
		if (other.handle == null)
			return LARGER_THAN_NULL ? -1 : 1;
		else
			return ((Comparable<Object>) handle).compareTo(other.handle);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof WrappedChunkCoordinate) {
			WrappedChunkCoordinate wrapper = (WrappedChunkCoordinate) other;
			return Objects.equal(handle, wrapper.handle);
		}
		
		// It's tempting to handle the ChunkCoordinate case too, but then
		// the equals() method won't be commutative, causing a.equals(b) to
		// be different to b.equals(a).
		return false;
	}
	
	@Override
	public int hashCode() {
		return handle.hashCode();
	}

	@Override
	public String toString() {
		return String.format("ChunkCoordinate [x: %s, y: %s, z: %s]", getX(), getY(), getZ());
	}
}
