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

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import com.google.common.primitives.Primitives;

/**
 * Used to print the content of an arbitrary class.
 * 
 * @author Kristian
 */
public class PrettyPrinter {
	
	/**
	 * How far we will recurse.
	 */
	public final static int RECURSE_DEPTH = 3;
	
	/**
	 * Print the content of an object.
	 * @param object - the object to serialize.
	 * @param stop - superclass that will stop the process.
	 * @return String representation of the class.
	 * @throws IllegalAccessException 
	 */
	public static String printObject(Object object, Class<?> start, Class<?> stop) throws IllegalAccessException {
		return printObject(object, start, stop, RECURSE_DEPTH);
	}
	
	/**
	 * Print the content of an object.
	 * @param object - the object to serialize.
	 * @param stop - superclass that will stop the process.
	 * @return String representation of the class.
	 * @throws IllegalAccessException 
	 */
	public static String printObject(Object object, Class<?> start, Class<?> stop, int hierachyDepth) throws IllegalAccessException {
		StringBuilder output = new StringBuilder();
		Set<Object> previous = new HashSet<Object>();
		
		// Start and stop
		output.append("{ ");
		printObject(output, object, start, stop, previous, hierachyDepth);
		output.append(" }");
		
		return output.toString();
	}
	
	@SuppressWarnings("rawtypes")
	private static void printIterables(StringBuilder output, Iterable iterable, Class<?> current, Class<?> stop, 
									   Set<Object> previous, int hierachyIndex) throws IllegalAccessException {	
		
		boolean first = true;
		output.append("(");
		
		for (Object value : iterable) {
			if (first) 
				first = false;
			else
				output.append(", ");
			
			// Handle exceptions
			if (value != null)
				printValue(output, value, value.getClass(), stop, previous, hierachyIndex - 1);
			else
				output.append("NULL");
		}
		
		output.append(")");
	}
	
	private static void printArray(StringBuilder output, Object array, Class<?> current, Class<?> stop, 
								   Set<Object> previous, int hierachyIndex) throws IllegalAccessException {	
		
		Class<?> component = current.getComponentType();
		boolean first = true;
		
		if (!component.isArray())
			output.append(component.getName());
		output.append("[");
		
		for (int i = 0; i < Array.getLength(array); i++) {
			if (first) 
				first = false;
			else
				output.append(", ");
			
			// Handle exceptions
			try {
				printValue(output, Array.get(array, i), component, stop, previous, hierachyIndex - 1);
			} catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
				break;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				break;
			}
		}
		
		output.append("]");
	}
	
	// Internal recursion method
	private static void printObject(StringBuilder output, Object object, Class<?> current, Class<?> stop, 
									Set<Object> previous, int hierachyIndex) throws IllegalAccessException {
		// Trickery
		boolean first = true;

		// See if we're supposed to skip this class
		if (current == Object.class || (stop != null && current.equals(stop))) {
			return;
		}

		// Don't iterate twice
		previous.add(object);
		
		// Hard coded limit
		if (hierachyIndex < 0) {
			output.append("...");
			return;
		}
		
		for (Field field : current.getDeclaredFields()) {
			int mod = field.getModifiers();
			
			// Skip a good number of the fields
			if (!Modifier.isTransient(mod) && !Modifier.isStatic(mod)) {
				Class<?> type = field.getType();
				Object value = FieldUtils.readField(field, object, true);
				
				if (first) 
					first = false;
				else
					output.append(", ");
				
				output.append(field.getName());
				output.append(" = ");
				printValue(output, value, type, stop, previous, hierachyIndex - 1);
			}
		}
		
		// Recurse
		printObject(output, object, current.getSuperclass(), stop, previous, hierachyIndex);
	}

	@SuppressWarnings("rawtypes")
	private static void printValue(StringBuilder output, Object value, Class<?> type, 
								   Class<?> stop, Set<Object> previous, int hierachyIndex) throws IllegalAccessException {
		// Just print primitive types
		if (value == null) {
			output.append("NULL");
		} else if (type.isPrimitive() || Primitives.isWrapperType(type)) {
			output.append(value);
		} else if (type == String.class || hierachyIndex <= 0) {
			output.append("\"" + value + "\"");
		} else if (type.isArray()) {
			printArray(output, value, type, stop, previous, hierachyIndex);
		} else if (Iterable.class.isAssignableFrom(type)) {
			printIterables(output, (Iterable) value, type, stop, previous, hierachyIndex);
		} else if (ClassLoader.class.isAssignableFrom(type) || previous.contains(value)) {
			// Don't print previous objects
			output.append("\"" + value + "\"");
		} else {
			output.append("{ ");
			printObject(output, value, value.getClass(), stop, previous, hierachyIndex);
			output.append(" }");
		}
	}
}
