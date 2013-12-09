package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

public class ExactReflection {
	// The class we're actually representing
	private Class<?> source;
	private boolean forceAccess;

	private ExactReflection(Class<?> source, boolean forceAccess) {
		this.source = Preconditions.checkNotNull(source, "source class cannot be NULL");
		this.forceAccess = forceAccess;
	}
	
	/**
	 * Retrieves an exact reflection instance from a given class.
	 * @param source - the class we'll use.
	 * @return A fuzzy reflection instance.
	 */
	public static ExactReflection fromClass(Class<?> source) {
		return fromClass(source, false);
	}
	
	/**
	 * Retrieves an exact reflection instance from a given class.
	 * @param source - the class we'll use.
	 * @param forceAccess - whether or not to override scope restrictions.
	 * @return A fuzzy reflection instance.
	 */
	public static ExactReflection fromClass(Class<?> source, boolean forceAccess) {
		return new ExactReflection(source, forceAccess);
	}
	
	/**
	 * Retrieves an exact reflection instance from an object.
	 * @param reference - the object we'll use.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static ExactReflection fromObject(Object reference) {
		return new ExactReflection(reference.getClass(), false);
	}
	
	/**
	 * Retrieves an exact reflection instance from an object.
	 * @param reference - the object we'll use.
	 * @param forceAccess - whether or not to override scope restrictions.
	 * @return A fuzzy reflection instance that uses the class of the given object.
	 */
	public static ExactReflection fromObject(Object reference, boolean forceAccess) {
		return new ExactReflection(reference.getClass(), forceAccess);
	}
	
	/**
	 * Retrieve the first method in the class hierachy with the given name and parameters.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for protected and private methods.
	 * @param methodName - the method name to find, or NULL to look for everything.
	 * @param parameters - the parameters.
	 * @return The first matched method.
	 * @throws IllegalArgumentException If we cannot find a method by this name.
	 */
	public Method getMethod(String methodName, Class<?>... parameters) {
		return getMethod(source,  methodName, parameters);
	}
	
	// For recursion
	private Method getMethod(Class<?> instanceClass, String methodName, Class<?>... parameters) {
        for (Method method : instanceClass.getDeclaredMethods()) {
            if ((forceAccess  		|| Modifier.isPublic(method.getModifiers())) &&
            	(methodName == null || method.getName().equals(methodName)) && 
                 Arrays.equals(method.getParameterTypes(), parameters)) {
                
                method.setAccessible(true);
                return method;
            }
        }
        // Search in every superclass
        if (instanceClass.getSuperclass() != null)
            return getMethod(instanceClass.getSuperclass(), methodName, parameters);
        throw new IllegalArgumentException(String.format(
            "Unable to find method %s (%s) in %s.", methodName, Arrays.asList(parameters), source));
	}
	
	/**
	 * Retrieve a field in the class hierachy by the given name.
	 * <p>
	 * If {@link #isForceAccess()} is TRUE, we will also search for protected and private fields.
	 * @param fieldName - the field name. Cannot be NULL.
	 * @return The first matched field.
	 */
	public Field getField(String fieldName) {
		return getField(source, fieldName);
	}
	
	// For recursion
	private Field getField(Class<?> instanceClass, @Nonnull String fieldName) {
        // Ignore access rules
        for (Field field : instanceClass.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }
        
        // Recursively fild the correct field
        if (instanceClass.getSuperclass() != null)
            return getField(instanceClass.getSuperclass(), fieldName);
        throw new IllegalArgumentException(String.format(
                "Unable to find field %s in %s.", fieldName, source));
	}
	
	/**
	 * Retrieve an {@link ExactReflection} object where scope restrictions are ignored.
	 * @return A copy of the current object.
	 */
	public ExactReflection forceAccess() {
		return new ExactReflection(source, true);
	}
	
	/**
	 * Determine if we are overriding scope restrictions and will also find 
	 * private, protected or package members.
	 * @return TRUE if we are, FALSE otherwise.
	 */
	public boolean isForceAccess() {
		return forceAccess;
	}
	
	/** 
	 * Retrieve the source class we are searching.
	 * @return The source.
	 */
	public Class<?> getSource() {
		return source;
	}
 }
