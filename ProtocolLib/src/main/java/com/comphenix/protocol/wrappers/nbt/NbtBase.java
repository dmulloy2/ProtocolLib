package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtType;

/**
 * Represents a generic container for an NBT element.
 * @author Kristian
 *
 * @param <TType> - type of the value that is stored.
 */
public interface NbtBase<TType> {
	/**
	 * Retrieve the type of this NBT element.
	 * @return The type of this NBT element.
	 */
	public abstract NbtType getType();

	/**
	 * Retrieve the name of this NBT tag.
	 * <p>
	 * This will be an empty string if the NBT tag is stored in a list.
	 * @return Name of the tag.
	 */
	public abstract String getName();

	/**
	 * Set the name of this NBT tag.
	 * <p>
	 * This will be ignored if the NBT tag is stored in a list.
	 * @param name - name of the tag.
	 */
	public abstract void setName(String name);

	/**
	 * Retrieve the value of this NBT tag.
	 * @return Value of this tag.
	 */
	public abstract TType getValue();

	/**
	 * Set the value of this NBT tag.
	 * @param newValue - the new value of this tag.
	 */
	public abstract void setValue(TType newValue);
		
	/**
	 * Clone the current NBT tag.
	 * @return The cloned tag.
	 */
	public abstract NbtBase<TType> clone();
}