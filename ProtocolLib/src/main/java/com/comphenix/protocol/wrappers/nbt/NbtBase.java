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


/**
 * Represents a generic container for an NBT element.
 * <p>
 * Use {@link NbtFactory} to load or create an instance.
 *
 * @author Kristian
 * @param <TType> - type of the value that is stored.
 */
public interface NbtBase<TType> {
	/**
	 * Accepts a NBT visitor.
	 * @param visitor - the hierarchical NBT visitor.
	 * @return TRUE if the parent should continue processing children at the current level, FALSE otherwise.
	 */
	public abstract boolean accept(NbtVisitor visitor);
	
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
	 * Is either a primitive {@link java.lang.Number wrapper}, {@link java.lang.String String}, 
	 * {@link java.util.List List} or a {@link java.util.Map Map}. 
	 * <p>
	 * Users are encouraged to cast an NBT compound to {@link NbtCompound} and use its put and get-methods
	 * instead of accessing its content from getValue().
	 * <p>
	 * All operations that modify collections directly, such as {@link java.util.List#add(Object) List.add(Object)} or 
	 * {@link java.util.Map#clear() Map.clear()}, are considered optional. This also include members in {@link java.util.Iterator Iterator} and 
	 * {@link java.util.ListIterator ListIterator}. Operations that are not implemented throw a 
	 * {@link java.lang.UnsupportedOperationException UnsupportedOperationException}.
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