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

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

	private static final Joiner COMMA_JOINER = Joiner.on(", ");

	// The class we're actually representing
	private final Class<?> source;
	private final boolean forceAccess;

	public FuzzyReflection(Class<?> source, boolean forceAccess) {
		this.source = source;
		this.forceAccess = forceAccess;
	}

	/**
	 * Retrieves a fuzzy reflection instance from a given class.
	 *
	 * @param source - the class we'll use.
	 * @return A fuzzy reflection instance.
	 */
	public static FuzzyReflection fromClass(Class<?> source) {
		return fromClass(source, false);
	}

	/**
	 * Retrieves a fuzzy reflection instance from a given class.
	 *
	 * @param source      - the class we'll use.
	 * @param forceAccess - whether to override scope restrictions.
	 * @return A fuzzy reflection instance.
	 */
	public static FuzzyReflection fromClass(Class<?> source, boolean forceAccess) {
		return new FuzzyReflection(source, forceAccess);
	}

	/**
	 * Retrieves a fuzzy reflection instance from an object.
	 *
	 * @param reference - the object we'll use.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static FuzzyReflection fromObject(Object reference) {
		return new FuzzyReflection(reference.getClass(), false);
	}

	/**
	 * Retrieves a fuzzy reflection instance from an object.
	 *
	 * @param reference   - the object we'll use.
	 * @param forceAccess - whether to override scope restrictions.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static FuzzyReflection fromObject(Object reference, boolean forceAccess) {
		return new FuzzyReflection(reference.getClass(), forceAccess);
	}

	/**
	 * Retrieve the value of the first field of the given type.
	 *
	 * @param <T>         Type
	 * @param instance    - the instance to retrieve from.
	 * @param fieldClass  - type of the field to retrieve.
	 * @param forceAccess - whether to look for private and protected fields.
	 * @return The value of that field.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Object instance, Class<T> fieldClass, boolean forceAccess) {
		return (T) Accessors.getFieldAccessor(instance.getClass(), fieldClass, forceAccess).get(instance);
	}

	@SafeVarargs
	public static <T> Set<T> combineArrays(T[]... arrays) {
		Set<T> result = new LinkedHashSet<>();
		for (T[] elements : arrays) {
			if (elements != null) {
				Collections.addAll(result, elements);
			}
		}

		return result;
	}

	/**
	 * Retrieves the underlying class.
	 *
	 * @return The underlying class.
	 */
	public Class<?> getSource() {
		return this.source;
	}

	/**
	 * Retrieves whether or not not to override any scope restrictions.
	 *
	 * @return TRUE if we override scope, FALSE otherwise.
	 */
	public boolean isForceAccess() {
		return this.forceAccess;
	}

	/**
	 * Retrieve the singleton instance of a class, from a method or field.
	 *
	 * @return The singleton instance.
	 * @throws IllegalStateException If the class has no singleton.
	 */
	public Object getSingleton() {
		try {
			// try a no-args method which is static and returns the same type as the target class
			Method method = this.getMethod(FuzzyMethodContract.newBuilder()
					.parameterCount(0)
					.returnDerivedOf(this.source)
					.requireModifier(Modifier.STATIC)
					.build());
			return Accessors.getMethodAccessor(method).invoke(null);
		} catch (IllegalArgumentException ignored) {
			// that method doesn't exist...
		}

		try {
			// try a field which is static and of the same type as the target class
			Field field = this.getField(FuzzyFieldContract.newBuilder()
					.typeDerivedOf(this.source)
					.requireModifier(Modifier.STATIC)
					.build());
			return Accessors.getFieldAccessor(field).get(null);
		} catch (IllegalArgumentException ignored) {
			// that field doesn't exist
		}

		// we're unable to find the field
		throw new IllegalStateException("Unable to retrieve singleton instance of " + this.source);
	}

	/**
	 * Retrieve the first method that matches.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level method.
	 *
	 * @param matcher - the matcher to use.
	 * @return The first method that satisfies the given matcher.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethod(AbstractFuzzyMatcher<MethodInfo> matcher) {
		List<Method> result = this.getMethodList(matcher);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			throw new IllegalArgumentException("Unable to find a method that matches " + matcher);
		}
	}

	/**
	 * Retrieve a method that matches. If there are multiple methods that match, the first one with the preferred name is
	 * selected.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level method.
	 *
	 * @param matcher   - the matcher to use.
	 * @param preferred - the preferred name, null for no preference.
	 * @return The first method that satisfies the given matcher.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethod(AbstractFuzzyMatcher<MethodInfo> matcher, String preferred) {
		List<Method> result = this.getMethodList(matcher);

		// if we got more than one result check for the preferred method name
		if (result.size() > 1 && preferred != null) {
			for (Method method : result) {
				if (method.getName().equals(preferred)) {
					return method;
				}
			}
		}

		if (result.size() > 0) {
			return result.get(0);
		} else {
			throw new IllegalArgumentException("Unable to find a method that matches " + matcher);
		}
	}

	/**
	 * Retrieves a method by looking at its name.
	 *
	 * @param nameRegex -  regular expression that will match method names.
	 * @return The first method that satisfies the regular expression.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByName(String nameRegex) {
		// compile the regex only once
		Pattern match = Pattern.compile(nameRegex);
		for (Method method : this.getMethods()) {
			if (match.matcher(method.getName()).matches()) {
				// Right - this is probably it.
				return method;
			}
		}

		throw new IllegalArgumentException(String.format(
				"Unable to find a method in %s that matches \"%s\"",
				this.source,
				nameRegex));
	}

	/**
	 * Retrieves a method by looking at the parameter types only.
	 *
	 * @param name - potential name of the method. Only used by the error mechanism.
	 * @param args - parameter types of the method to find.
	 * @return The first method that satisfies the parameter types.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByParameters(String name, Class<?>... args) {
		// Find the correct method to call
		for (Method method : this.getMethods()) {
			if (Arrays.equals(method.getParameterTypes(), args)) {
				return method;
			}
		}

		// That sucks
		throw new IllegalArgumentException(String.format(
				"Unable to find %s(%s) in %s",
				name,
				COMMA_JOINER.join(args),
				this.source));
	}

	/**
	 * Retrieves a method by looking at the parameter types and return type only.
	 *
	 * @param name       - potential name of the method. Only used by the error mechanism.
	 * @param returnType - return type of the method to find.
	 * @param args       - parameter types of the method to find.
	 * @return The first method that satisfies the parameter types.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Method getMethodByReturnTypeAndParameters(String name, Class<?> returnType, Class<?>... args) {
		// Find the correct method to call
		List<Method> methods = this.getMethodListByParameters(returnType, args);
		if (methods.size() > 0) {
			return methods.get(0);
		} else {
			// That sucks
			throw new IllegalArgumentException(String.format(
					"Unable to find %s(%s): %s in %s",
					name,
					COMMA_JOINER.join(args),
					returnType,
					this.source));
		}
	}

	/**
	 * Retrieve a list of every method that matches the given matcher.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level methods.
	 *
	 * @param matcher - the matcher to apply.
	 * @return List of found methods.
	 */
	public List<Method> getMethodList(AbstractFuzzyMatcher<MethodInfo> matcher) {
		// finds and adds all matching methods
		List<Method> methods = new ArrayList<>();
		for (Method method : this.getMethods()) {
			if (matcher.isMatch(MethodInfo.fromMethod(method), this.source)) {
				methods.add(method);
			}
		}

		return methods;
	}

	/**
	 * Retrieves every method that has the given parameter types and return type.
	 *
	 * @param returnType - return type of the method to find.
	 * @param args       - parameter types of the method to find.
	 * @return Every method that satisfies the given constraints.
	 */
	public List<Method> getMethodListByParameters(Class<?> returnType, Class<?>... args) {
		List<Method> methods = new ArrayList<>();
		// Find the correct method to call
		for (Method method : this.getMethods()) {
			if (method.getReturnType().equals(returnType) && Arrays.equals(method.getParameterTypes(), args)) {
				methods.add(method);
			}
		}

		return methods;
	}

	/**
	 * Retrieve the first field that matches.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level fields.
	 *
	 * @param matcher - the matcher to use.
	 * @return The first method that satisfies the given matcher.
	 * @throws IllegalArgumentException If the method cannot be found.
	 */
	public Field getField(AbstractFuzzyMatcher<Field> matcher) {
		List<Field> result = this.getFieldList(matcher);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			throw new IllegalArgumentException("Unable to find a field that matches " + matcher);
		}
	}

	/**
	 * Retrieves a field by name.
	 *
	 * @param nameRegex - regular expression that will match a field name.
	 * @return The first field to match the given expression.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public Field getFieldByName(String nameRegex) {
		// compile the pattern only once
		Pattern match = Pattern.compile(nameRegex);
		for (Field field : this.getFields()) {
			if (match.matcher(field.getName()).matches()) {
				return field;
			}
		}

		// Looks like we're outdated. Too bad.
		throw new IllegalArgumentException(String.format(
				"Unable to find a field with a name matching \"%s\" in %s",
				nameRegex,
				this.source));
	}

	/**
	 * Retrieves the first field with a type equal to or more specific to the given type.
	 *
	 * @param name - name the field probably is given. This will only be used in the error message.
	 * @param type - type of the field to find.
	 * @return The first field with a type that is an instance of the given type.
	 */
	public Field getFieldByType(String name, Class<?> type) {
		List<Field> fields = this.getFieldListByType(type);
		if (fields.size() > 0) {
			return fields.get(0);
		} else {
			// Looks like we're outdated. Too bad.
			throw new IllegalArgumentException(String.format(
					"Unable to find a field \"%s\" with the type %s in %s",
					name,
					type,
					this.source));
		}
	}

	/**
	 * Retrieves every field with a type equal to or more specific to the given type.
	 *
	 * @param type - type of the fields to find.
	 * @return Every field with a type that is an instance of the given type.
	 */
	public List<Field> getFieldListByType(Class<?> type) {
		// Field with a compatible type
		List<Field> fields = new ArrayList<>();
		for (Field field : this.getFields()) {
			if (type.isAssignableFrom(field.getType())) {
				fields.add(field);
			}
		}

		return fields;
	}

	/**
	 * Retrieves a field with a given type and parameters. This is most useful when dealing with Collections.
	 *
	 * @param fieldType Type of the field
	 * @param params    Variable length array of type parameters
	 * @return The field
	 * @throws IllegalArgumentException If the field cannot be found
	 */
	public Field getParameterizedField(Class<?> fieldType, Class<?>... params) {
		for (Field field : this.getFields()) {
			if (field.getType().equals(fieldType)) {
				Type type = field.getGenericType();
				if (type instanceof ParameterizedType) {
					if (Arrays.equals(((ParameterizedType) type).getActualTypeArguments(), params)) {
						return field;
					}
				}
			}
		}

		throw new IllegalArgumentException(String.format(
				"Unable to find a field of type %s<%s> in %s",
				fieldType,
				COMMA_JOINER.join(params),
				this.source));
	}

	/**
	 * Retrieve a list of every field that matches the given matcher.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level fields.
	 *
	 * @param matcher - the matcher to apply.
	 * @return List of found fields.
	 */
	public List<Field> getFieldList(AbstractFuzzyMatcher<Field> matcher) {
		// Add all matching fields to the list
		List<Field> fields = new ArrayList<>();
		for (Field field : this.getFields()) {
			if (matcher.isMatch(field, this.source)) {
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
	 *
	 * @param typeRegex - regular expression that will match the field type.
	 * @return The first field with a type that matches the given regular expression.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public Field getFieldByType(String typeRegex) {
		Pattern match = Pattern.compile(typeRegex);

		// Like above, only here we test the field type
		for (Field field : this.getFields()) {
			String name = field.getType().getName();
			if (match.matcher(name).matches()) {
				return field;
			}
		}

		// Looks like we're outdated. Too bad.
		throw new IllegalArgumentException(String.format(
				"Unable to find a field with a type that matches \"%s\" in %s",
				typeRegex,
				this.source));
	}

	/**
	 * Retrieve the first constructor that matches.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level constructors.
	 *
	 * @param matcher - the matcher to use.
	 * @return The first constructor that satisfies the given matcher.
	 * @throws IllegalArgumentException If the constructor cannot be found.
	 */
	public Constructor<?> getConstructor(AbstractFuzzyMatcher<MethodInfo> matcher) {
		List<Constructor<?>> result = this.getConstructorList(matcher);
		if (result.size() > 0) {
			return result.get(0);
		} else {
			throw new IllegalArgumentException("Unable to find a method that matches " + matcher);
		}
	}

	/**
	 * Retrieve a list of every constructor that matches the given matcher.
	 * <p>
	 * ForceAccess must be TRUE in order for this method to access private, protected and package level constructors.
	 *
	 * @param matcher - the matcher to apply.
	 * @return List of found constructors.
	 */
	public List<Constructor<?>> getConstructorList(AbstractFuzzyMatcher<MethodInfo> matcher) {
		// Add all matching constructors to the list
		List<Constructor<?>> constructors = new ArrayList<>();
		for (Constructor<?> constructor : this.getConstructors()) {
			if (matcher.isMatch(MethodInfo.fromConstructor(constructor), this.source)) {
				constructors.add(constructor);
			}
		}

		return constructors;
	}

	/**
	 * Retrieves all private and public fields in declared order.
	 * <p>
	 * Private, protected and package fields are ignored if forceAccess is FALSE.
	 *
	 * @return Every field.
	 */
	public Set<Field> getFields() {
		if (this.forceAccess) {
			return combineArrays(this.source.getDeclaredFields(), this.source.getFields());
		} else {
			return combineArrays(this.source.getFields());
		}
	}

	/**
	 * Retrieves all private and public fields, up until a certain superclass.
	 *
	 * @param excludeClass - the class (and its superclasses) to exclude from the search.
	 * @return Every such declared field.
	 */
	public Set<Field> getDeclaredFields(Class<?> excludeClass) {
		// we only need to do this if we include inherited fields
		if (this.forceAccess) {
			Class<?> current = this.source;
			Set<Field> fields = Sets.newLinkedHashSet();

			while (current != null && current != excludeClass) {
				fields.addAll(Arrays.asList(current.getDeclaredFields()));
				current = current.getSuperclass();
			}

			return fields;
		}

		return this.getFields();
	}

	/**
	 * Retrieves all private and public methods in declared order (after JDK 1.5).
	 * <p>
	 * Private, protected and package methods are ignored if forceAccess is FALSE.
	 *
	 * @return Every method.
	 */
	public Set<Method> getMethods() {
		if (this.forceAccess) {
			return combineArrays(this.source.getDeclaredMethods(), this.source.getMethods());
		} else {
			return combineArrays(this.source.getMethods());
		}
	}

	/**
	 * Retrieves all private and public constructors in declared order (after JDK 1.5).
	 * <p>
	 * Private, protected and package constructors are ignored if forceAccess is FALSE.
	 *
	 * @return Every constructor.
	 */
	public Set<Constructor<?>> getConstructors() {
		return combineArrays(this.forceAccess ? this.source.getDeclaredConstructors() : this.source.getConstructors());
	}
}
