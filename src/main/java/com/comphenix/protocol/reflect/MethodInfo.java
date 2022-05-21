package com.comphenix.protocol.reflect;

import com.google.common.collect.Lists;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a method or a constructor.
 * 
 * @author Kristian
 */
public abstract class MethodInfo implements GenericDeclaration, Member {
	/**
	 * Wraps a method as a MethodInfo object.
	 * @param method - the method to wrap.
	 * @return The wrapped method.
	 */
	public static MethodInfo fromMethod(final Method method) {
		return new MethodInfo() {
			// @Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return method.getAnnotation(annotationClass);
			}
			// @Override
			public Annotation[] getAnnotations() {
				return method.getAnnotations();
			}
			// @Override
			public Annotation[] getDeclaredAnnotations() {
				return method.getDeclaredAnnotations();
			}
			@Override
			public String getName() {
				return method.getName();
			}
			@Override
			public Class<?>[] getParameterTypes() {
				return method.getParameterTypes();
			}
			@Override
			public Class<?> getDeclaringClass() {
				return method.getDeclaringClass();
			}
			@Override
			public Class<?> getReturnType() {
				return method.getReturnType();
			}
			@Override
			public int getModifiers() {
				return method.getModifiers();
			}
			@Override
			public Class<?>[] getExceptionTypes() {
				return method.getExceptionTypes();
			}
			@Override
			public TypeVariable<?>[] getTypeParameters() {
				return method.getTypeParameters();
			}
			@Override
			public String toGenericString() {
				return method.toGenericString();
			}
			@Override
			public String toString() {
				return method.toString();
			}
			@Override
			public boolean isSynthetic() {
				return method.isSynthetic();
			}
			@Override
			public int hashCode() {
				return method.hashCode();
			}
			@Override
			public boolean isConstructor() {
				return false;
			}
		};
	}
	
	/**
	 * Construct a list of method infos from a given array of methods.
	 * @param methods - array of methods.
	 * @return Method info list.
	 */
	public static Collection<MethodInfo> fromMethods(Method[] methods) {
		return fromMethods(Arrays.asList(methods));
	}

	/**
	 * Construct a list of method infos from a given collection of methods.
	 * @param methods - list of methods.
	 * @return Method info list.
	 */
	public static List<MethodInfo> fromMethods(Collection<Method> methods) {
		List<MethodInfo> infos = Lists.newArrayList();
		
		for (Method method : methods)
			infos.add(fromMethod(method));
		return infos;
	}
	
	/**
	 * Wraps a constructor as a method information object.
	 * @param constructor - the constructor to wrap.
	 * @return A wrapped constructor.
	 */
	public static MethodInfo fromConstructor(final Constructor<?> constructor) {
		return new MethodInfo() {
			// @Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return constructor.getAnnotation(annotationClass);
			}
			// @Override
			public Annotation[] getAnnotations() {
				return constructor.getAnnotations();
			}
			// @Override
			public Annotation[] getDeclaredAnnotations() {
				return constructor.getDeclaredAnnotations();
			}
			@Override
			public String getName() {
				return constructor.getName();
			}
			@Override
			public Class<?>[] getParameterTypes() {
				return constructor.getParameterTypes();
			}
			@Override
			public Class<?> getDeclaringClass() {
				return constructor.getDeclaringClass();
			}
			@Override
			public Class<?> getReturnType() {
				return Void.class;
			}
			@Override
			public int getModifiers() {
				return constructor.getModifiers();
			}
			@Override
			public Class<?>[] getExceptionTypes() {
				return constructor.getExceptionTypes();
			}
			@Override
			public TypeVariable<?>[] getTypeParameters() {
				return constructor.getTypeParameters();
			}
			@Override
			public String toGenericString() {
				return constructor.toGenericString();
			}
			@Override
			public String toString() {
				return constructor.toString();
			}
			@Override
			public boolean isSynthetic() {
				return constructor.isSynthetic();
			}
			@Override
			public int hashCode() {
				return constructor.hashCode();
			}
			@Override
			public boolean isConstructor() {
				return true;
			}
		};
	}
	
	/**
	 * Construct a list of method infos from a given array of constructors.
	 * @param constructors - array of constructors.
	 * @return Method info list.
	 */
	public static Collection<MethodInfo> fromConstructors(Constructor<?>[] constructors) {
		return fromConstructors(Arrays.asList(constructors));
	}
	
	/**
	 * Construct a list of method infos from a given collection of constructors.
	 * @param constructors - list of constructors.
	 * @return Method info list.
	 */
	public static List<MethodInfo> fromConstructors(Collection<Constructor<?>> constructors) {
		List<MethodInfo> infos = Lists.newArrayList();
		
		for (Constructor<?> constructor : constructors)
			infos.add(fromConstructor(constructor));
		return infos;
	}
	
	/**
	 * Returns a string describing this method or constructor
	 * @return A string representation of the object.
	 * @see Method#toString()
	 * @see Constructor#toString()
	 */
	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns a string describing this method or constructor, including type parameters.
	 * @return A string describing this Method, include type parameters
	 * @see Method#toGenericString()
	 * @see Constructor#toGenericString()
	 */
	public abstract String toGenericString();
	
	/**
	 * Returns an array of Class objects that represent the types of the exceptions declared to be thrown by the
	 * underlying method or constructor represented by this MethodInfo object.
	 * @return The exception types declared as being thrown by the method or constructor this object represents.
	 * @see Method#getExceptionTypes()
	 * @see Constructor#getExceptionTypes()
	 */
	public abstract Class<?>[] getExceptionTypes();

	/**
	 * Returns a Class object that represents the formal return type of the method or constructor
	 * represented by this MethodInfo object.
	 * <p>
	 * This is always {@link Void} for constructors.
	 * @return The return value, or Void if a constructor.
	 * @see Method#getReturnType()
	 */
	public abstract Class<?> getReturnType();

	/**
	 * Returns an array of Class objects that represent the formal parameter types, in declaration order,
	 * of the method or constructor represented by this MethodInfo object.
	 * @return The parameter types for the method or constructor this object represents.
	 * @see Method#getParameterTypes()
	 * @see Constructor#getParameterTypes()
	 */
	public abstract Class<?>[] getParameterTypes();
	
	/**
	 * Determine if this is a constructor or not.
	 * @return TRUE if this represents a constructor, FALSE otherwise.
	 */
	public abstract boolean isConstructor();
}
