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

package com.comphenix.protocol.reflect;

/**
 * Interface that converts generic objects into types and back.
 * 
 * @author Kristian
 * @param <TType> The specific type.
 */
public interface EquivalentConverter<TType> {
	/**
	 * Retrieve a copy of the specific type using an instance of the generic type.
	 * <p>
	 * This is usually a wrapper type in the Bukkit API.
	 * @param generic - the generic type.
	 * @return The new specific type.
	 */
	public TType getSpecific(Object generic);
	
	/**
	 * Retrieve a copy of the generic type from a specific type.
	 * <p>
	 * This is usually a native net.minecraft.server type in Minecraft.
	 * @param genericType - class or super class of the generic type.
	 * @param specific - the specific type we need to copy.
	 * @return A copy of the specific type.
	 */
	public Object getGeneric(Class<?> genericType, TType specific);
	
	/**
	 * Due to type erasion, we need to explicitly keep a reference to the specific type.
	 * @return The specific type.
	 */
	public Class<TType> getSpecificType();
}

