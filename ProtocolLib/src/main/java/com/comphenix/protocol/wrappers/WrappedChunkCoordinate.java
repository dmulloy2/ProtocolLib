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

	/**
	 * Create a new empty wrapper.
	 */
	public WrappedChunkCoordinate() {
		this(new ChunkCoordinates());
	}
	
	/**
	 * Create a wrapper for a specific chunk coordinates.
	 * @param handle - the NMS chunk coordinates.
	 */
	public WrappedChunkCoordinate(ChunkCoordinates handle) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be NULL");
		this.handle = handle;
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
	
	public ChunkCoordinates getHandle() {
		return handle;
	}
	
	/**
	 * Retrieve the x coordinate of the underlying coordinate.
	 * @return The x coordinate.
	 */
	public int getX() {
		return handle.x;
	}
	
	/**
	 * Set the x coordinate of the underlying coordinate.
	 * @param newX - the new x coordinate.
	 */
	public void setX(int newX) {
		handle.x = newX;
	}
	
	/**
	 * Retrieve the y coordinate of the underlying coordinate.
	 * @return The y coordinate.
	 */
	public int getY() {
		return handle.y;
	}
	
	/**
	 * Set the y coordinate of the underlying coordinate.
	 * @param newY - the new y coordinate.
	 */
	public void setY(int newY) {
		handle.y = newY;
	}
	
	/**
	 * Retrieve the z coordinate of the underlying coordinate.
	 * @return The z coordinate.
	 */
	public int getZ() {
		return handle.z;
	}
	
	/**
	 * Create an immutable chunk position from this coordinate.
	 * @return The new immutable chunk position.
	 */
	public ChunkPosition toPosition() {
		return new ChunkPosition(getX(), getY(), getZ());
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
