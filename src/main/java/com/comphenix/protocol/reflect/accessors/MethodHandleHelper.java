package com.comphenix.protocol.reflect.accessors;

import com.comphenix.protocol.ProtocolLogger;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

import com.google.common.base.Preconditions;

final class MethodHandleHelper {

	private static final Lookup LOOKUP;

	// static fields, converted as "public Object get()" and "public void set(Object value)"
	private static final MethodType STATIC_FIELD_GETTER = MethodType.methodType(Object.class);
	private static final MethodType STATIC_FIELD_SETTER = MethodType.methodType(void.class, Object.class);
	// instance fields, converted as "public Object get(Object instance)" and "public void set(Object instance, Object value)"
	private static final MethodType VIRTUAL_FIELD_GETTER = MethodType.methodType(Object.class, Object.class);
	private static final MethodType VIRTUAL_FIELD_SETTER = MethodType.methodType(void.class, Object.class, Object.class);

	static {
		Lookup lookup;
		try {
			// get the unsafe class
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
			// get the unsafe instance
			Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			sun.misc.Unsafe unsafe = (sun.misc.Unsafe) theUnsafe.get(null);
			// get the trusted lookup field
			Field trustedLookup = Lookup.class.getDeclaredField("IMPL_LOOKUP");
			// get access to the base and offset value of it
			long offset = unsafe.staticFieldOffset(trustedLookup);
			Object baseValue = unsafe.staticFieldBase(trustedLookup);
			// get the trusted lookup instance
			lookup = (Lookup) unsafe.getObject(baseValue, offset);
		} catch (Exception exception) {
			ProtocolLogger.log(Level.SEVERE, "Unable to retrieve trusted lookup", exception);
			lookup = MethodHandles.lookup();
		}

		LOOKUP = lookup;
	}

	// sealed class
	private MethodHandleHelper() {
	}

	public static MethodAccessor getMethodAccessor(Method method) {
		Preconditions.checkNotNull(method, "method");

		try {
			MethodHandle unreflected = LOOKUP.unreflect(method);
			boolean staticMethod = Modifier.isStatic(method.getModifiers());

			MethodHandle generified = convertToGeneric(unreflected, staticMethod, false);
			return new DefaultMethodAccessor(method, generified, staticMethod);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Unable to access method " + method, ex);
		}
	}

	public static ConstructorAccessor getConstructorAccessor(Constructor<?> constructor) {
		Preconditions.checkNotNull(constructor, "constructor");

		try {
			MethodHandle unreflected = LOOKUP.unreflectConstructor(constructor);
			MethodHandle generified = convertToGeneric(unreflected, false, true);

			return new DefaultConstrutorAccessor(constructor, generified);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Unable to access constructor " + constructor, ex);
		}
	}

	public static FieldAccessor getFieldAccessor(Field field) {
		Preconditions.checkNotNull(field, "field");

		try {
			boolean staticField = Modifier.isStatic(field.getModifiers());

			// java hates us - unreflecting a trusted field always results in an exception, finding them doesn't...
			MethodHandle getter;
			MethodHandle setter;
			if (staticField) {
				getter = LOOKUP.findStaticGetter(field.getDeclaringClass(), field.getName(), field.getType());
				setter = LOOKUP.findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType());
			} else {
				getter = LOOKUP.findGetter(field.getDeclaringClass(), field.getName(), field.getType());
				setter = LOOKUP.findSetter(field.getDeclaringClass(), field.getName(), field.getType());
			}

			// generify the method type so that we don't need to worry about it when using the handles
			if (staticField) {
				getter = getter.asType(STATIC_FIELD_GETTER);
				setter = setter.asType(STATIC_FIELD_SETTER);
			} else {
				getter = getter.asType(VIRTUAL_FIELD_GETTER);
				setter = setter.asType(VIRTUAL_FIELD_SETTER);
			}

			if (getter == null) {
				throw new IllegalStateException("Unable to access field " + field + ". Could not find getter");
			}

			if (setter == null) {
				throw new IllegalStateException("Unable to access field " + field + ". Could not find setter");
			}

			return new DefaultFieldAccessor(field, setter, getter, staticField);
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			// NoSuchFieldException can never happen, the field always exists
			throw new IllegalStateException("Unable to access field " + field, ex);
		}
	}

	private static MethodHandle convertToGeneric(MethodHandle handle, boolean staticMethod, boolean ctor) {
		MethodHandle target = handle.asFixedArity();
		// special thing - we do not need the trailing array if we have 0 arguments anyway
		int paramCount = handle.type().parameterCount() - (ctor || staticMethod ? 0 : 1);
		MethodType methodType = MethodType.genericMethodType(ctor ? 0 : 1, true);
		// spread the arguments we give into the handle
		target = target.asSpreader(Object[].class, paramCount);
		// adds a leading 'this' argument which we can ignore
		if (staticMethod) {
			target = MethodHandles.dropArguments(target, 0, Object.class);
		}
		// convert the type to finish
		return target.asType(methodType);
	}
}
