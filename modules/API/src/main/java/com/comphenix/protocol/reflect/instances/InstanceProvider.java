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

package com.comphenix.protocol.reflect.instances;

import javax.annotation.Nullable;

/**
 * Represents a type generator for specific types.
 * 
 * @author Kristian
 */
public interface InstanceProvider {
	/**
	 * Create an instance given a type, if possible.
	 * @param type - type to create.
	 * @return The instance, or NULL if the type cannot be created.
	 * @throws NotConstructableException Thrown to indicate that this type cannot or should never be constructed.
	 */
	public abstract Object create(@Nullable Class<?> type);
}