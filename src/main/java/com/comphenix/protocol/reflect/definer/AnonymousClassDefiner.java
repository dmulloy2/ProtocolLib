package com.comphenix.protocol.reflect.definer;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import java.lang.reflect.Field;

/**
 * Class definer using Unsafe.defineAnonymousClass (available for java 8 - 16).
 */
final class AnonymousClassDefiner implements ClassDefiner {

	private static final Object THE_UNSAFE;
	private static final MethodAccessor ANONYMOUS_DEFINE_METHOD;

	static {
		Object theUnsafe;
		MethodAccessor defineMethod;

		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

			// get the unsafe instance
			Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			theUnsafe = theUnsafeField.get(null);

			// get the define method
			defineMethod = Accessors.getMethodAccessor(
					unsafeClass,
					"defineAnonymousClass",
					Class.class, byte[].class, Object[].class);
		} catch (Exception exception) {
			theUnsafe = null;
			defineMethod = null;
		}

		THE_UNSAFE = theUnsafe;
		ANONYMOUS_DEFINE_METHOD = defineMethod;
	}

	@Override
	public boolean isAvailable() {
		return THE_UNSAFE != null && ANONYMOUS_DEFINE_METHOD != null;
	}

	@Override
	public Class<?> define(Class<?> hostClass, byte[] byteCode) {
		try {
			return (Class<?>) ANONYMOUS_DEFINE_METHOD.invoke(THE_UNSAFE, hostClass, byteCode, null);
		} catch (Exception exception) {
			return null;
		}
	}
}
