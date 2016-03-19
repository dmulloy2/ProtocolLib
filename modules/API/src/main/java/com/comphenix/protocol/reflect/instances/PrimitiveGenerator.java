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

import java.lang.reflect.Array;

import javax.annotation.Nullable;

import com.google.common.base.Defaults;
import com.google.common.primitives.Primitives;

/**
 * Provides constructors for primtive types, wrappers, arrays and strings.
 * @author Kristian
 */
public class PrimitiveGenerator implements InstanceProvider {
	
	/**
	 * Default value for Strings.
	 */
	public static final String STRING_DEFAULT = "";
	
	/**
	 * Shared instance of this generator.
	 */
	public static PrimitiveGenerator INSTANCE = new PrimitiveGenerator(STRING_DEFAULT);

	// Our default string value
	private String stringDefault;
	
	public PrimitiveGenerator(String stringDefault) {
		this.stringDefault = stringDefault;
	}
	
	/**
	 * Retrieve the string default.
	 * @return Default instance of a string.
	 */
	public String getStringDefault() {
		return stringDefault;
	}

	@Override
	public Object create(@Nullable Class<?> type) {
		if (type == null) {
			return null;
		} else if (type.isPrimitive()) {
			return Defaults.defaultValue(type);
		} else if (Primitives.isWrapperType(type)) {
			return Defaults.defaultValue(Primitives.unwrap(type));
		} else if (type.isArray()) {
			Class<?> arrayType = type.getComponentType();
			return Array.newInstance(arrayType, 0);
		} else if (type.isEnum()) {
			Object[] values = type.getEnumConstants();
			if (values != null && values.length > 0)
				return values[0];
		} else if (type.equals(String.class)) {
			return stringDefault;
		} 
		
		// Cannot handle this type
		return null;
	}	
}