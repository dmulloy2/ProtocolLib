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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Retrieves fields and methods by signature, not just name.
 * 
 * @author Kristian
 */
public class FuzzyReflection {

	/**
	 * Matches a Minecraft object.
	 */
	public static String MINECRAFT_OBJECT = "net\\.minecraft(\\.\\w+)+";
	
	// The class we're actually representing
	private Class<?> source;

	// Whether or not to lookup private members
	private boolean forceAccess;

	public FuzzyReflection(Class<?> source, boolean forceAccess) {
		this.source = source;
		this.forceAccess = forceAccess;
	}
	
	/**
	 * Retrieves a fuzzy reflection instance from a given class.
	 * @param source - the class we'll use.
	 * @return A fuzzy reflection instance.
	 */
	public static FuzzyReflection fromClass(Class<?> source) {
		return fromClass(source, false);
	}
	
	/**
	 * Retrieves a fuzzy reflection instance from a given class.
	 * @param source - the class we'll use.
	 * @param forceAccess - whether or not to override scope restrictions.
	 * @return A fuzzy reflection instance.
	 */
	public static FuzzyReflection fromClass(Class<?> source, boolean forceAccess) {
		return new FuzzyReflection(source, forceAccess);
	}
	
	/**
	 * Retrieves a fuzzy reflection instance from an object.
	 * @param reference - the object we'll use.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static FuzzyReflection fromObject(Object reference) {
		return new FuzzyReflection(reference.getClass(), false);
	}
	
	/**
	 * Retrieves a fuzzy reflection instance from an object.
	 * @param reference - the object we'll use.
	 * @param forceAccess - whether or not to override scope restrictions.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static FuzzyReflection fromObject(Object reference, boolean forceAccess) {
		return new FuzzyReflection(reference.getClass(), forceAccess);
	}
	
	/**
	 * Retrieves the underlying class.
	 */
	public Class<?> getSource() {
		return source;
	}
	
	/**
	 * Retrieves a method by looking at its name.
	 * @param nameRegex -  regular expression that will match method names.
	 * @return The first method that satisfies the regular expression.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByName(String nameRegex) {
		
		Pattern match = Pattern.compile(nameRegex);
		
		for (Method method : getMethods()) {
			if (match.matcher(method.getName()).matches()) {
				// Right - this is probably it. 
				return method;
			}
		}

		throw new IllegalArgumentException("Unable to find a method with the pattern " + 
									nameRegex + " in " + source.getName());
	}
	
	/**
	 * Retrieves a method by looking at the parameter types only.
	 * @param name - potential name of the method. Only used by the error mechanism.
	 * @param args - parameter types of the method to find.
	 * @return The first method that satisfies the parameter types.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByParameters(String name, Class<?>... args) {
		
		// Find the correct method to call
		for (Method method : getMethods()) {
			if (Arrays.equals(method.getParameterTypes(), args)) {
				return method;
			}
		}
		
		// That sucks
		throw new IllegalArgumentException("Unable to find " + name + " in " + source.getName());
	}
	
	/**
	 * Retrieves a method by looking at the parameter types and return type only.
	 * @param name - potential name of the method. Only used by the error mechanism.
	 * @param returnType - return type of the method to find.
	 * @param args - parameter types of the method to find.
	 * @return The first method that satisfies the parameter types.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByParameters(String name, Class<?> returnType, Class<?>[] args) {
		// Find the correct method to call
		List<Method> methods = getMethodListByParameters(returnType, args);
		
		if (methods.size() > 0) {
			return methods.get(0);
		} else {
			// That sucks
			throw new IllegalArgumentException("Unable to find " + name + " in " + source.getName());
		}
	}
	
	/**
	 * Retrieves a method by looking at the parameter types and return type only.
	 * @param name - potential name of the method. Only used by the error mechanism.
	 * @param returnTypeRegex - regular expression matching the return type of the method to find.
	 * @param argsRegex - regular expressions of the matching parameter types.
	 * @return The first method that satisfies the parameter types.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByParameters(String name, String returnTypeRegex, String[] argsRegex) {
	
		Pattern match = Pattern.compile(returnTypeRegex);
		Pattern[] argMatch = new Pattern[argsRegex.length];
		
		for (int i = 0; i < argsRegex.length; i++) {
			argMatch[i] = Pattern.compile(argsRegex[i]);
		}
		
		// Find the correct method to call
		for (Method method : getMethods()) {
			if (match.matcher(method.getReturnType().getName()).matches()) {
				if (matchParameters(argMatch, method.getParameterTypes()))
					return method;
			}
		}
		
		// That sucks
		throw new IllegalArgumentException("Unable to find " + name + " in " + source.getName());
	}
	
	private boolean matchParameters(Pattern[] parameterMatchers, Class<?>[] argTypes) {
		if (parameterMatchers.length != argTypes.length)
			throw new IllegalArgumentException("Arrays must have the same cardinality.");
		
		// Check types against the regular expressions
		for (int i = 0; i < argTypes.length; i++) {
			if (!parameterMatchers[i].matcher(argTypes[i].getName()).matches())
				return false;
		}
		
		return true;
	}
	
	/**
	 * Retrieves every method that has the given parameter types and return type.
	 * @param returnType - return type of the method to find.
	 * @param args - parameter types of the method to find.
	 * @return Every method that satisfies the given constraints.
	 */
	public List<Method> getMethodListByParameters(Class<?> returnType, Class<?>[] args) {
	
		List<Method> methods = new ArrayList<Method>();
		
		// Find the correct method to call
		for (Method method : getMethods()) {
			if (method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), args)) {
				methods.add(method);
			}
		}
		
		return methods;
	}
	
	/**
	 * Retrieves a field by name.
	 * @param nameRegex - regular expression that will match a field name.
	 * @return The first field to match the given expression.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public Field getFieldByName(String nameRegex) {
		
		Pattern match = Pattern.compile(nameRegex);
		
		for (Field field : getFields()) {
			if (match.matcher(field.getName()).matches()) {
				// Right - this is probably it. 
				return field;
			}
		}
		
		// Looks like we're outdated. Too bad.
		throw new IllegalArgumentException("Unable to find a field with the pattern " + 
									nameRegex + " in " + source.getName());
	}
	
	/**
	 * Retrieves the first field with a type equal to or more specific to the given type.
	 * @param name - name the field probably is given. This will only be used in the error message.
	 * @param type - type of the field to find.
	 * @return The first field with a type that is an instance of the given type.
	 */
	public Field getFieldByType(String name, Class<?> type) {
		
		List<Field> fields = getFieldListByType(type);
		
		if (fields.size() > 0) {
			return fields.get(0);
		} else {
			// Looks like we're outdated. Too bad.
			throw new IllegalArgumentException(String.format("Unable to find a field %s with the type %s in %s",
					name, type.getName(), source.getName())
			);
		}
	}
	
	/**
	 * Retrieves every field with a type equal to or more specific to the given type.
	 * @param type - type of the fields to find.
	 * @return Every field with a type that is an instance of the given type.
	 */
	public List<Field> getFieldListByType(Class<?> type) {
		
		List<Field> fields = new ArrayList<Field>();
		
		// Field with a compatible type
		for (Field field : getFields()) {
			// A assignable from B -> B instanceOf A
			if (type.isAssignableFrom(field.getType())) {
				fields.add(field);
			}
		}
		
		return fields;
	}
	
	/**
	 * Retrieves a field by type.
	 * <p>
	 * Note that the type is matched using the full canonical representation, i.e.: 
	 * <ul>
	 *     <li>java.util.List</li>
	 *     <li>net.comphenix.xp.ExperienceMod</li>
	 * </ul>
	 * @param typeRegex - regular expression that will match the field type.
	 * @return The first field with a type that matches the given regular expression.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public Field getFieldByType(String typeRegex) {
		
		Pattern match = Pattern.compile(typeRegex);
		
		// Like above, only here we test the field type
		for (Field field : getFields()) {
			String name = field.getType().getName();
			
			if (match.matcher(name).matches()) {
				return field;
			}
		}
		
		// Looks like we're outdated. Too bad.
		throw new IllegalArgumentException("Unable to find a field with the type " + 
										   typeRegex + " in " + source.getName());
	}
	
	/**
	 * Retrieves a field by type.
	 * <p>
	 * Note that the type is matched using the full canonical representation, i.e.: 
	 * <ul>
	 *     <li>java.util.List</li>
	 *     <li>net.comphenix.xp.ExperienceMod</li>
	 * </ul>
	 * @param typeRegex - regular expression that will match the field type.
	 * @param ignored - types to ignore.
	 * @return The first field with a type that matches the given regular expression.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	@SuppressWarnings("rawtypes")
	public Field getFieldByType(String typeRegex, Set<Class> ignored) {
		
		Pattern match = Pattern.compile(typeRegex);
		
		// Like above, only here we test the field type
		for (Field field : getFields()) {
			Class type = field.getType();
			
			if (!ignored.contains(type) && match.matcher(type.getName()).matches()) {
				return field;
			}
		}
		
		// Looks like we're outdated. Too bad.
		throw new IllegalArgumentException("Unable to find a field with the type " + 
									       typeRegex + " in " + source.getName());
	}
	
	/**
	 * Retrieves all private and public fields in declared order (after JDK 1.5).
	 * @return Every field.
	 */
	public Set<Field> getFields() {
		// We will only consider private fields in the declared class
		if (forceAccess)
			return setUnion(source.getDeclaredFields(), source.getFields());
		else
			return setUnion(source.getFields());
	}
	
	/**
	 * Retrieves all private and public methods in declared order (after JDK 1.5).
	 * @return Every method.
	 */
	public Set<Method> getMethods() {
		// We will only consider private methods in the declared class
		if (forceAccess)
			return setUnion(source.getDeclaredMethods(), source.getMethods());
		else
			return setUnion(source.getMethods());
	}
	
	// Prevent duplicate fields
	private static <T> Set<T> setUnion(T[]... array) {
		Set<T> result = new LinkedHashSet<T>();
		
		for (T[] elements : array) {
			for (T element : elements) {
				result.add(element);
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves whether or not not to override any scope restrictions.
	 * @return TRUE if we override scope, FALSE otherwise.
	 */
	public boolean isForceAccess() {
		return forceAccess;
	}

	/**
	 * Sets whether or not not to override any scope restrictions.
	 * @param forceAccess - TRUE if we override scope, FALSE otherwise.
	 */
	public void setForceAccess(boolean forceAccess) {
		this.forceAccess = forceAccess;
	}
}
