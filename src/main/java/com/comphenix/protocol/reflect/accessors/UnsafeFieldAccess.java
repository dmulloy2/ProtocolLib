package com.comphenix.protocol.reflect.accessors;

import com.comphenix.protocol.ProtocolLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.logging.Level;

final class UnsafeFieldAccess {

	private static final Lookup TRUSTED_LOOKUP;

	static {
		Lookup trusted = null;
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
			trusted = (Lookup) unsafe.getObject(baseValue, offset);
		} catch (Exception exception) {
			ProtocolLogger.log(Level.SEVERE, "Unable to retrieve trusted lookup", exception);
		}

		TRUSTED_LOOKUP = trusted;
	}

	public static boolean hasTrustedLookup() {
		return TRUSTED_LOOKUP != null;
	}

	public static MethodHandle findSetter(Field field) throws ReflectiveOperationException {
		if (Modifier.isStatic(field.getModifiers())) {
			return TRUSTED_LOOKUP.findStaticSetter(field.getDeclaringClass(), field.getName(), field.getType());
		} else {
			return TRUSTED_LOOKUP.findSetter(field.getDeclaringClass(), field.getName(), field.getType());
		}
	}
}
