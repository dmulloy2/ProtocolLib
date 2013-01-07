package com.comphenix.protocol.wrappers.nbt;

import java.io.DataOutput;

/**
 * Indicates that this NBT wraps an underlying net.minecraft.server instance.
 * 
 * @author Kristian
 * 
 * @param <TType> - type of the value that is stored.
 */
public interface NbtWrapper<TType> extends NbtBase<TType> {
	/**
	 * Retrieve the underlying net.minecraft.server instance.
	 * @return The NMS instance.
	 */
	public Object getHandle();
	
	/**
	 * Write the current NBT tag to an output stream.
	 * @param destination - the destination stream.
	 */
	public void write(DataOutput destination);
}
