package com.comphenix.protocol.reflect.accessors;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import com.comphenix.protocol.reflect.ExactReflection;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.base.Joiner;

public final class Accessors {
	/**
	 * Represents a field accessor that synchronizes access to the underlying field.
	 * @author Kristian
	 */
	public static final class SynchronizedFieldAccessor implements FieldAccessor {
		private final FieldAccessor accessor;
		private SynchronizedFieldAccessor(FieldAccessor accessor) {
			this.accessor = accessor;
		}
		
		@Override
		public void set(Object instance, Object value) {
			Object lock = accessor.get(instance);
			
			if (lock != null) {
				synchronized (lock) {
					accessor.set(instance, value);
				}
			} else {
				accessor.set(instance, value);
			}
		}
		
		@Override
		public Object get(Object instance) {
			return accessor.get(instance);
		}
		
		@Override
		public Field getField() {
			return accessor.getField();
		}
	}
	
	/**
	 * Retrieve an accessor for the first field of the given type.
	 * @param instanceClass - the type of the instance to retrieve.
	 * @param fieldClass - type of the field to retrieve.
	 * @param forceAccess - whether or not to look for private and protected fields.
	 * @return The field accessor.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public static FieldAccessor getFieldAccessor(Class<?> instanceClass, Class<?> fieldClass, boolean forceAccess) {	
		// Get a field accessor
		Field field = FuzzyReflection.fromClass(instanceClass, forceAccess).getFieldByType(null, fieldClass);
		return Accessors.getFieldAccessor(field);
	}

	/**
	 * Retrieve an accessor (in declared order) for every field of the givne type.
	 * @param instanceClass - the type of the instance to retrieve.
	 * @param fieldClass - type of the field(s) to retrieve.
	 * @param forceAccess - whether or not to look for private and protected fields.
	 * @return The accessors.
	 */
	public static FieldAccessor[] getFieldAccessorArray(Class<?> instanceClass, Class<?> fieldClass, boolean forceAccess) {	
		List<Field> fields = FuzzyReflection.fromClass(instanceClass, forceAccess).getFieldListByType(fieldClass);
		FieldAccessor[] accessors = new FieldAccessor[fields.size()];
		
		for (int i = 0; i < accessors.length; i++) {
			accessors[i] = getFieldAccessor(fields.get(i));
		}
		return accessors;
	}
	
	/**
	 * Retrieve an accessor for the first field of the given type.
	 * @param instanceClass - the type of the instance to retrieve.
	 * @param fieldClass - type of the field to retrieve.
	 * @param forceAccess - whether or not to look for private and protected fields.
	 * @return The value of that field.
	 * @throws IllegalArgumentException If the field cannot be found.
	 */
	public static FieldAccessor getFieldAccessor(Class<?> instanceClass, String fieldName, boolean forceAccess) {	
		return Accessors.getFieldAccessor(ExactReflection.fromClass(instanceClass, true).getField(fieldName));
	}

	/**
	 * Retrieve a field accessor from a given field that uses unchecked exceptions.
	 * @param field - the field.
	 * @return The field accessor.
	 */
	public static FieldAccessor getFieldAccessor(final Field field) {
		return Accessors.getFieldAccessor(field, true);
	}

	/**
	 * Retrieve a field accessor from a given field that uses unchecked exceptions.
	 * @param field - the field.
	 * @param forceAccess - whether or not to skip Java access checking.
	 * @return The field accessor.
	 */
	public static FieldAccessor getFieldAccessor(final Field field, boolean forceAccess) {
		field.setAccessible(true);
		return new DefaultFieldAccessor(field);
	}

	/**
	 * Retrieve a field accessor where the write operation is synchronized on the current field value.
	 * @param accessor - the accessor.
	 * @return The field accessor.
	 */
	public static FieldAccessor getSynchronized(final FieldAccessor accessor) {
		// Only wrap once
		if (accessor instanceof SynchronizedFieldAccessor)
			return accessor;
		return new SynchronizedFieldAccessor(accessor);
	}
	
	/**
	 * Retrieve a method accessor for a method with the given name and signature.
	 * @param instanceClass - the parent class.
	 * @param methodName - the method name.
	 * @param parameters - the parameters.
	 * @return The method accessor.
	 */
	public static MethodAccessor getMethodAccessor(Class<?> instanceClass, String methodName, Class<?>... parameters) {
		return new DefaultMethodAccessor(ExactReflection.fromClass(instanceClass, true).getMethod(methodName, parameters));
	}

	/**
	 * Retrieve a method accessor for a particular method, avoding checked exceptions.
	 * @param method - the method to access.
	 * @return The method accessor.
	 */
	public static MethodAccessor getMethodAccessor(final Method method) {
		return new DefaultMethodAccessor(method);
	}

	/**
	 * Retrieve a constructor accessor for a constructor with the given signature.
	 * @param instanceClass - the parent class.
	 * @param parameters - the parameters.
	 * @return The constructor accessor.
	 */
	public static ConstructorAccessor getConstructorAccessor(Class<?> instanceClass, Class<?>... parameters) {
		try {
			return getConstructorAccessor(instanceClass.getDeclaredConstructor(parameters));
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(String.format(
				"Unable to find constructor %s(%s).", instanceClass, Joiner.on(",").join(parameters))
			);
		} catch (SecurityException e) {
			throw new IllegalStateException("Cannot access constructors.", e);
		}
	}

	/**
	 * Retrieve a constructor accessor for a particular constructor, avoding checked exceptions.
	 * @param constructor - the constructor to access.
	 * @return The method accessor.
	 */
	public static ConstructorAccessor getConstructorAccessor(final Constructor<?> constructor) {
		return new DefaultConstrutorAccessor(constructor);
	}
	
	// Seal this class
	private Accessors() {
	}
}
