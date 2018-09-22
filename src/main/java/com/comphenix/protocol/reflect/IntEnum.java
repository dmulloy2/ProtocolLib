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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Represents a traditional int field enum.
 * 
 * @author Kristian
 */
public class IntEnum {

	// Used to convert between IDs and names
	protected BiMap<Integer, String> members = HashBiMap.create();
	
	/**
	 * Registers every declared integer field.
	 */
	public IntEnum() {
		registerAll();
	}
	
	/**
	 * Registers every public int field as a member.
	 */
	protected void registerAll() {
		try {
			// Register every int field
			for (Field entry : this.getClass().getFields()) {
				if (entry.getType().equals(int.class)) {
					registerMember(entry.getInt(this), entry.getName());
				}
			}
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a member.
	 * @param id - id of member.
	 * @param name - name of member.
	 */
	protected void registerMember(int id, String name) {
		members.put(id, name);
	}
	
	/**
	 * Determines whether or not the given member exists.
	 * @param id - the ID of the member to find.
	 * @return TRUE if a member with the given ID exists, FALSE otherwise.
	 */
	public boolean hasMember(int id) {
		return members.containsKey(id);
	}
	
	/**
	 * Retrieve the ID of the member with the given name.
	 * @param name - name of member to retrieve.
	 * @return ID of the member, or NULL if not found.
	 */
	public Integer valueOf(String name) {
		return members.inverse().get(name);
	}
	
	/**
	 * Retrieve the name of the member with the given id.
	 * @param id - id of the member to retrieve.
	 * @return Declared name of the member, or NULL if not found.
	 */
	public String getDeclaredName(Integer id) {
		return members.get(id);
	}
	
	/**
	 * Retrieve the ID of every registered member.
	 * @return Enumeration of every value.
	 */
	public Set<Integer> values() {
		return new HashSet<Integer>(members.keySet());
	}
}
