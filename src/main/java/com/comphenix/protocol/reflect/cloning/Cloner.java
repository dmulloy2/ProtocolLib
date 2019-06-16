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

package com.comphenix.protocol.reflect.cloning;

/**
 * Represents an object that is capable of cloning other objects.
 * 
 * @author Kristian
 */
public interface Cloner {
	/**
	 * Determine whether or not the current cloner can clone the given object.
	 * @param source - the object that is being considered.
	 * @return TRUE if this cloner can actually clone the given object, FALSE otherwise.
	 */
	public boolean canClone(Object source);
	
	/**
	 * Perform the clone. 
	 * <p>
	 * This method should never be called unless a corresponding {@link #canClone(Object)} returns TRUE.
	 * @param source - the value to clone.
	 * @return A cloned value.
	 * @throws IllegalArgumentException If this cloner cannot perform the clone.
	 */
	public Object clone(Object source);
}
