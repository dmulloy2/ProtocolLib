package com.comphenix.protocol.reflect.accessors;

import com.comphenix.protocol.reflect.ExactReflection;
import com.comphenix.protocol.reflect.FuzzyReflection;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// TODO: at some point we should make everything nullable to make updates easier

public final class Accessors {

	// Seal this class
	private Accessors() {
	}

	/**
	 * Retrieve an accessor (in declared order) for every field of the givne type.
	 *
	 * @param clazz       - the type of the instance to retrieve.
	 * @param fieldClass  - type of the field(s) to retrieve.
	 * @param forceAccess - whether to look for private and protected fields.
	 * @return The accessors.
	 */
	public static FieldAccessor[] getFieldAccessorArray(Class<?> clazz, Class<?> fieldClass, boolean forceAccess) {
		return FuzzyReflection.fromClass(clazz, forceAccess).getFieldListByType(fieldClass).stream()
				.map(Accessors::getFieldAccessor)
				.toArray(FieldAccessor[]::new);
	}

	/**
	 * Retrieve an accessor for the first field of the given type.
	 *
	 * @param instanceClass - the type of the instance to retrieve.
	 * @param fieldName     - name of the field to retrieve.
	 * @param forceAccess   - whether to look for private and protected fields.
	 * @return The value of that field.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	//public static FieldAccessor getFieldAccessor(Class<?> instanceClass, String fieldName, boolean forceAccess) {
	//	return Accessors.getFieldAccessor(ExactReflection.fromClass(instanceClass, forceAccess).getField(fieldName));
	//}

	/**
	 * Retrieve an accessor for the first field of the given type.
	 *
	 * @param instanceClass - the type of the instance to retrieve.
	 * @param fieldClass    - type of the field to retrieve.
	 * @param forceAccess   - whether to look for private and protected fields.
	 * @return The field accessor.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public static FieldAccessor getFieldAccessor(Class<?> instanceClass, Class<?> fieldClass, boolean forceAccess) {
		// Get a field accessor
		Field field = FuzzyReflection.fromClass(instanceClass, forceAccess).getFieldByType(null, fieldClass);
		return Accessors.getFieldAccessor(field);
	}

	/**
	 * Retrieve a field accessor for a field with the given name and equivalent type, or NULL.
	 *
	 * @param clazz     - the declaration class.
	 * @param fieldName - the field name.
	 * @param fieldType - assignable field type.
	 * @return The field accessor, or NULL if not found.
	 */
	public static FieldAccessor getFieldAccessorOrNull(Class<?> clazz, String fieldName, Class<?> fieldType) {
		Field field = ExactReflection.fromClass(clazz, true).findField(fieldName);
		if (field != null && (fieldType == null || fieldType.isAssignableFrom(field.getType()))) {
			return Accessors.getFieldAccessor(field);
		}

		// no matching field found
		return null;
	}

	/**
	 * Retrieve a field accessor from a given field that uses unchecked exceptions.
	 *
	 * @param field - the field.
	 * @return The field accessor.
	 */
	public static FieldAccessor getFieldAccessor(Field field) {
		return MethodHandleHelper.getFieldAccessor(field);
	}

	/**
	 * Retrieve a field accessor that will cache the content of the field.
	 * <p>
	 * Note that we don't check if the underlying field has changed after the value has been cached, so it's best to use
	 * this on final fields.
	 *
	 * @param inner - the accessor.
	 * @return A cached field accessor.
	 */
	public static FieldAccessor getMemorizing(FieldAccessor inner) {
		return new MemorizingFieldAccessor(inner);
	}

	/**
	 * Retrieve a method accessor for a method with the given name and signature.
	 *
	 * @param instanceClass - the parent class.
	 * @param methodName    - the method name.
	 * @param parameters    - the parameters.
	 * @return The method accessor.
	 */
	public static MethodAccessor getMethodAccessor(Class<?> instanceClass, String methodName, Class<?>... parameters) {
		Method method = ExactReflection.fromClass(instanceClass, true).getMethod(methodName, parameters);
		return Accessors.getMethodAccessor(method);
	}

	/**
	 * Retrieve a method accessor for a field with the given name and equivalent type, or NULL.
	 *
	 * @param clazz      - the declaration class.
	 * @param methodName - the method name.
	 * @return The method accessor, or NULL if not found.
	 */
	public static MethodAccessor getMethodAccessorOrNull(Class<?> clazz, String methodName, Class<?>... parameters) {
		Method method = ExactReflection.fromClass(clazz, true).findMethod(methodName, parameters);
		return method == null ? null : Accessors.getMethodAccessor(method);
	}

	/**
	 * Retrieve a method accessor for a particular method, avoding checked exceptions.
	 *
	 * @param method - the method to access.
	 * @return The method accessor.
	 */
	public static MethodAccessor getMethodAccessor(Method method) {
		return MethodHandleHelper.getMethodAccessor(method);
	}

	/**
	 * Retrieve a constructor accessor for a constructor with the given signature.
	 *
	 * @param instanceClass - the parent class.
	 * @param parameters    - the parameters.
	 * @return The constructor accessor.
	 * @throws IllegalArgumentException If we cannot find this constructor.
	 */
	public static ConstructorAccessor getConstructorAccessor(Class<?> instanceClass, Class<?>... parameters) {
		Constructor<?> constructor = ExactReflection.fromClass(instanceClass, true).findConstructor(parameters);
		return Accessors.getConstructorAccessor(constructor);
	}

	/**
	 * Find a specific constructor in a class.
	 *
	 * @param clazz      - the class.
	 * @param parameters - the signature of the constructor to find.
	 * @return The constructor, or NULL if not found.
	 */
	public static ConstructorAccessor getConstructorAccessorOrNull(Class<?> clazz, Class<?>... parameters) {
		Constructor<?> constructor = ExactReflection.fromClass(clazz, true).findConstructor(parameters);
		return constructor == null ? null : Accessors.getConstructorAccessor(constructor);
	}

	/**
	 * Retrieve a constructor accessor for a particular constructor, avoding checked exceptions.
	 *
	 * @param constructor - the constructor to access.
	 * @return The method accessor.
	 */
	public static ConstructorAccessor getConstructorAccessor(final Constructor<?> constructor) {
		return MethodHandleHelper.getConstructorAccessor(constructor);
	}
}
