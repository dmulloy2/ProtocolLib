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

package com.comphenix.protocol.wrappers.collection;

import java.util.Collection;
import java.util.Set;

/**
 * Represents a set that wraps another set by transforming the items going in and out.
 * 
 * @author Kristian
 *
 * @param <VInner> - type of the element in the inner invisible set.
 * @param <VOuter> - type of the elements publically accessible in the outer set.
 */
public abstract class ConvertedSet<VInner, VOuter> extends ConvertedCollection<VInner, VOuter> implements Set<VOuter> {
	public ConvertedSet(Collection<VInner> inner) {
		super(inner);
	}
}
