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
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * Represents a more modern object-based enum.
 * <p>
 * This is useful if you want the flexibility of a modern Java enum, but don't
 * want to prevent the creation of additional members dynamically.
 * @author Kristian
 */
public class ObjectEnum<T> implements Iterable<T> {
	// Used to convert between IDs and names
	protected BiMap<T, String> members = HashBiMap.create();
	
	/**
	 * Registers every declared integer field.
	 */
	public ObjectEnum(Class<T> fieldType) {
		registerAll(fieldType);
	}
	
	/**
	 * Registers every public assignable static field as a member.
	 */
	@SuppressWarnings("unchecked")
	protected void registerAll(Class<T> fieldType) {
		try {
			// Register every int field
			for (Field entry : this.getClass().getFields()) {
				if (Modifier.isStatic(entry.getModifiers()) && fieldType.isAssignableFrom(entry.getType())) {
					T value = (T) entry.get(null);
					
					if (value == null)
						throw new IllegalArgumentException("Field " + entry + " was NULL. Remember to " + 
								"construct the object after the field has been declared.");
					registerMember(value, entry.getName());
				}
			}
		
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a member if its not present.
	 * @param instance - member instance.
	 * @param name - name of member.
	 * @return TRUE if the member was registered, FALSE otherwise.
	 */
	public boolean registerMember(T instance, String name) {
		if (!members.containsKey(instance)) {
			members.put(instance, name);	
			return true;
		}
		return false;
	}
	
	/**
	 * Determines whether or not the given member has been registered to this enum.
	 * @param member - the member to check.
	 * @return TRUE if the given member has been registered, FALSE otherwise.
	 */
	public boolean hasMember(T member) {
		return members.containsKey(member);
	}
	
	/**
	 * Retrieve a member by name,
	 * @param name - name of member to retrieve.
	 * @return The member, or NULL if not found.
	 */
	public T valueOf(String name) {
		return members.inverse().get(name);
	}
	
	/**
	 * Retrieve the name of the given member.
	 * @param member - the member to retrieve.
	 * @return Declared name of the member, or NULL if not found.
	 */
	public String getDeclaredName(T member) {
		return members.get(member);
	}
	
	/**
	 * Retrieve every registered member.
	 * @return Enumeration of every value.
	 */
	public Set<T> values() {
		return new HashSet<T>(members.keySet());
	}

	@Override
	public Iterator<T> iterator() {
		return members.keySet().iterator();
	}
}
