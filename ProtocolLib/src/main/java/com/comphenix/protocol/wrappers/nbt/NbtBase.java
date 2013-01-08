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
	 * <p>
	 * Is either a primitive wrapper, a list or a map.
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
	public abstract NbtBase<TType> deepClone();
}