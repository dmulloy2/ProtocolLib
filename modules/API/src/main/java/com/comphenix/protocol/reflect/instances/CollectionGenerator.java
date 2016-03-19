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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Nullable;

/**
 * Provides simple constructors for collection interfaces.
 * @author Kristian
 */
public class CollectionGenerator implements InstanceProvider {

	/**
	 * Shared instance of this generator.
	 */
	public static final CollectionGenerator INSTANCE = new CollectionGenerator();
	
	@Override
	public Object create(@Nullable Class<?> type) {
		// Standard collection types
		if (type != null && type.isInterface()) {
			if (type.equals(Collection.class) || type.equals(List.class))
				return new ArrayList<Object>();
			else if (type.equals(Set.class))
				return new HashSet<Object>();
			else if (type.equals(Map.class))
				return new HashMap<Object, Object>();
			else if (type.equals(SortedSet.class))
				return new TreeSet<Object>();
			else if (type.equals(SortedMap.class))
				return new TreeMap<Object, Object>();
			else if (type.equals(Queue.class))
				return new LinkedList<Object>();
		}
		
		// Cannot provide an instance
		return null;
	}
}