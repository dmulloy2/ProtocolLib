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

import java.io.DataOutput;

/**
 * Indicates that this NBT wraps an underlying net.minecraft.server instance.
 * <p>
 * Use {@link NbtFactory} to load or create instances.
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
