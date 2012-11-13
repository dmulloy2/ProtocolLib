package com.comphenix.protocol.wrappers;

import com.google.common.base.Objects;

import net.minecraft.server.ChunkCoordinates;

/**
 * Allows access to a chunk coordinate.
 * 
 * @author Kristian
 */
public class WrappedChunkCoordinate implements Comparable<WrappedChunkCoordinate> {

	/**
	 * If TRUE, NULLs should be put before non-null instances of this class.
	 */
	private static final boolean LARGER_THAN_NULL = true;
	
	protected ChunkCoordinates handle;

	public WrappedChunkCoordinate(ChunkCoordinates handle) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be NULL");
		this.handle = handle;
	}
	
	public ChunkCoordinates getHandle() {
		return handle;
	}
	
	/**
	 * Retrieve the x coordinate of the underlying coordiate.
	 * @return The x coordinate.
	 */
	public int getX() {
		return handle.x;
	}
	
	/**
	 * Set the x coordinate of the underlying coordiate.
	 * @param newX - the new x coordinate.
	 */
	public void setX(int newX) {
		handle.x = newX;
	}
	
	/**
	 * Retrieve the y coordinate of the underlying coordiate.
	 * @return The y coordinate.
	 */
	public int getY() {
		return handle.y;
	}
	
	/**
	 * Set the y coordinate of the underlying coordiate.
	 * @param newY - the new y coordinate.
	 */
	public void setY(int newY) {
		handle.y = newY;
	}
	
	/**
	 * Retrieve the z coordinate of the underlying coordiate.
	 * @return The z coordinate.
	 */
	public int getZ() {
		return handle.z;
	}
	
	/**
	 * Set the z coordinate of the underlying coordiate.
	 * @param newZ - the new z coordinate.
	 */
	public void setZ(int newZ) {
		handle.z = newZ;
	}

	@Override
	public int compareTo(WrappedChunkCoordinate other) {
		// We'll handle NULL objects too, unlike ChunkCoordinates
		if (other.handle == null)
			return LARGER_THAN_NULL ? -1 : 1;
		else
			return handle.compareTo(other.handle);
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
}
