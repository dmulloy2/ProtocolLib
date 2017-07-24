/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2017 Dan Mulloy
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
 * Converts generic objects into specific objects.
 * @author Dan
 * @param <T> The specific type
 */
public interface SpecificConverter<T> {
	/**
	 * Retrieve a copy of the specific type using an instance of the generic type.
	 * <p>
	 * This is usually a wrapper type in the Bukkit API or ProtocolLib API.
	 * @param generic - the generic type.
	 * @return The new specific type.
	 */
	T getSpecific(Object generic);
}