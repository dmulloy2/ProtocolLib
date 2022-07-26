package com.comphenix.protocol.reflect;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class ExactReflection {

	private static final Joiner COMMA_SEPERATED_JOINER = Joiner.on(", ");

	// The class we're actually representing
	private final Class<?> source;
	private final boolean forceAccess;

	private ExactReflection(Class<?> source, boolean forceAccess) {
		this.source = Preconditions.checkNotNull(source, "source class cannot be NULL");
		this.forceAccess = forceAccess;
	}

	/**
	 * Retrieves an exact reflection instance from a given class.
	 *
	 * @param source      - the class we'll use.
	 * @param forceAccess - whether to also search for members which are out of the allowed scope.
	 * @return A fuzzy reflection instance.
	 */
	public static ExactReflection fromClass(Class<?> source, boolean forceAccess) {
		return new ExactReflection(source, forceAccess);
	}

	/**
	 * Retrieves an exact reflection instance from an object.
	 *
	 * @param reference   - the object we'll use.
	 * @param forceAccess - whether to also search for members which are out of the allowed scope.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static ExactReflection fromObject(Object reference, boolean forceAccess) {
		return new ExactReflection(reference.getClass(), forceAccess);
	}

	/**
	 * Retrieve the first method in the class hierarchy with the given name and parameters.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for methods which are out of the caller scope.
	 *
	 * @param methodName - the name of the method to find, NULL to only search by using the given parameters.
	 * @param parameters - the parameters of the method to find.
	 * @return the first matching method.
	 * @throws IllegalArgumentException if there is no method with the given name and parameter types.
	 */
	public Method getMethod(String methodName, Class<?>... parameters) {
		Method method = this.lookupMethod(this.source, methodName, parameters);
		if (method == null) {
			throw new IllegalArgumentException(String.format(
					"Unable to find method %s(%s) in %s",
					methodName,
					COMMA_SEPERATED_JOINER.join(parameters),
					this.source.getName()));
		}

		return method;
	}

	/**
	 * Finds the first method in the class hierarchy with the given name and parameters.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for methods which are out of the caller scope.
	 *
	 * @param methodName - the name of the method to find, NULL to only search by using the given parameters.
	 * @param parameters - the parameters of the method to find.
	 * @return the first matching method, NULL if no method matches.
	 */
	public Method findMethod(String methodName, Class<?>... parameters) {
		return this.lookupMethod(this.source, methodName, parameters);
	}

	// For recursion
	private Method lookupMethod(Class<?> instanceClass, String methodName, Class<?>... parameters) {
		for (Method method : instanceClass.getDeclaredMethods()) {
			if ((this.forceAccess || Modifier.isPublic(method.getModifiers()))
					&& (methodName == null || method.getName().equals(methodName))
					&& Arrays.equals(method.getParameterTypes(), parameters)) {
				return method;
			}
		}

		// Search in every superclass
		if (instanceClass.getSuperclass() != null) {
			return this.lookupMethod(instanceClass.getSuperclass(), methodName, parameters);
		}

		return null;
	}

	/**
	 * Retrieve a field in the class hierarchy by the given name.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for fields which are out of the caller scope.
	 *
	 * @param fieldName - the name of the field to find.
	 * @return the first matching field.
	 * @throws IllegalArgumentException if no field with the given name was found.
	 */
	public Field getField(String fieldName) {
		Field field = this.lookupField(this.source, fieldName);
		if (field == null) {
			throw new IllegalArgumentException(String.format(
					"Unable to find field with name %s in %s.",
					fieldName,
					this.source.getName()));
		}

		return field;
	}

	/**
	 * Finds a field in the class hierarchy by the given name.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for fields which are out of the caller scope.
	 *
	 * @param fieldName - the name of the field to find.
	 * @return the first matching field, null if no field matches.
	 */
	public Field findField(String fieldName) {
		return this.lookupField(this.source, fieldName);
	}

	// For recursion
	private Field lookupField(Class<?> instanceClass, String fieldName) {
		for (Field field : instanceClass.getDeclaredFields()) {
			if ((this.forceAccess || Modifier.isPublic(field.getModifiers())) && field.getName().equals(fieldName)) {
				return field;
			}
		}

		// Recursively find the correct field
		if (instanceClass.getSuperclass() != null) {
			return this.lookupField(instanceClass.getSuperclass(), fieldName);
		}

		return null;
	}

	/**
	 * Retrieves the first constructor in the class hierarchy with the given parameters.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for constructors which are out of the caller scope.
	 *
	 * @param parameters - the parameters of the constructor to find.
	 * @return the first matching constructor.
	 * @throws IllegalArgumentException if no constructor with the given parameters was found.
	 */
	public Constructor<?> getConstructor(Class<?>... parameters) {
		Constructor<?> constructor = this.findConstructor(parameters);
		if (constructor == null) {
			throw new IllegalArgumentException(String.format(
					"Unable to find constructor (%s) in %s",
					COMMA_SEPERATED_JOINER.join(parameters),
					this.source.getName()));
		}

		return constructor;
	}

	/**
	 * Finds the first constructor in the class hierarchy with the given parameters.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for constructors which are out of the caller scope.
	 *
	 * @param parameters - the parameters of the constructor to find.
	 * @return the first matching constructor, NULL if no constructor matches.
	 */
	public Constructor<?> findConstructor(Class<?>... parameters) {
		try {
			Constructor<?> constructor = this.source.getDeclaredConstructor(parameters);
			return this.forceAccess || Modifier.isPublic(constructor.getModifiers()) ? constructor : null;
		} catch (NoSuchMethodException exception) {
			return null;
		}
	}

	/**
	 * Retrieve an {@link ExactReflection} object where scope restrictions are ignored.
	 *
	 * @return A copy of the current object.
	 */
	public ExactReflection forceAccess() {
		return new ExactReflection(this.source, true);
	}

	/**
	 * Determine if we are overriding scope restrictions and will also find private, protected or package members.
	 *
	 * @return TRUE if we are, FALSE otherwise.
	 */
	public boolean isForceAccess() {
		return this.forceAccess;
	}

	/**
	 * Retrieve the source class we are searching.
	 *
	 * @return The source.
	 */
	public Class<?> getSource() {
		return this.source;
	}
}
