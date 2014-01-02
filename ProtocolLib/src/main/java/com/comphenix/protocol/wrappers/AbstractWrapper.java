package com.comphenix.protocol.wrappers;

import com.google.common.base.Preconditions;

/**
 * Represents a wrapper for an NMS object.
 * @author Kristian
 */
public abstract class AbstractWrapper {
	protected Object handle;
	protected Class<?> handleType;
	
	/**
	 * Construct a new NMS wrapper.
	 * @param handle - the wrapped NMS object.
	 */
	public AbstractWrapper(Class<?> handleType) {
		this.handleType = Preconditions.checkNotNull(handleType, "handleType cannot be NULL");
	}

	/**
	 * Set the underlying NMS object.
	 * @param handle - the NMS object.
	 * @throws IllegalArgumentException If the handle is NULL.
	 * @throws IllegalArgumentException If the handle is not assignable to {@link #getHandleType()}.
	 */
	protected void setHandle(Object handle) {
		if (handle == null)
			throw new IllegalArgumentException("handle cannot be NULL.");
		if (!handleType.isAssignableFrom(handle.getClass()))
			throw new IllegalArgumentException("handle (" + handle + ") is not a " + handleType + ", but " + handle.getClass());
		this.handle = handle;
	}
	
	/**
	 * Retrieves the underlying NMS object.
	 * @return The underlying NMS object.
	 */
	public Object getHandle() {
		return handle;
	}
	
	/**
	 * Retrieve the type of the handle.
	 * @return The type of the handle.
	 */
	public Class<?> getHandleType() {
		return handleType;
	}
}
