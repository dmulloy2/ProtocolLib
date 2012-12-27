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
 * Creates a cloner wrapper that accepts and clones NULL values.
 * 
 * @author Kristian
 */
public class NullableCloner implements Cloner {
	protected Cloner wrapped;
	
	public NullableCloner(Cloner wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public boolean canClone(Object source) {
		return true;
	}

	@Override
	public Object clone(Object source) {
		// Don't pass the NULL value to the cloner
		if (source == null)
			return null;
		else
			return wrapped.clone(source);
	}

	public Cloner getWrapped() {
		return wrapped;
	}
}
