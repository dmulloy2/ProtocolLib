package com.comphenix.protocol.reflect.definer;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * A class definer using Lookup.defineHiddenClass which is available since Java 9.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
final class HiddenClassDefiner implements ClassDefiner {

	private static final Lookup IMPL_LOOKUP;
	private static final Object HIDDEN_CLASS_OPTIONS;
	private static final MethodAccessor DEFINE_HIDDEN_CLASS;

	static {
		Lookup implLookup;
		Object classOptions;
		MethodAccessor defineHiddenClass;

		try {
			Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");

			// get the unsafe instance
			Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
			theUnsafeField.setAccessible(true);
			Object theUnsafe = theUnsafeField.get(null);

			// the impl_lookup field we need
			Field implLookupField = Lookup.class.getDeclaredField("IMPL_LOOKUP");

			// get the accessors we need to access the object
			MethodAccessor baseAcc = Accessors.getMethodAccessor(unsafeClass, "staticFieldBase", Field.class);
			MethodAccessor offsetAcc = Accessors.getMethodAccessor(unsafeClass, "staticFieldOffset", Field.class);
			MethodAccessor objectAcc = Accessors.getMethodAccessor(unsafeClass, "getObject", Object.class, long.class);

			// get the offset values
			Object base = baseAcc.invoke(theUnsafe, implLookupField);
			Object offset = offsetAcc.invoke(theUnsafe, implLookupField);

			// get impl_lookup
			implLookup = (Lookup) objectAcc.invoke(theUnsafe, base, offset);

			// get the class option array with the value NESTMATE
			// this is a bit hacky as the class was introduced in java 9
			Class optionClass = Class.forName(Lookup.class.getName() + "$ClassOption");

			// fill the array
			classOptions = Array.newInstance(optionClass, 1);
			Array.set(classOptions, 0, Enum.valueOf(optionClass, "NESTMATE"));

			// if the method doesn't exist we're probably running on Java 8
			defineHiddenClass = Accessors.getMethodAccessor(
					Lookup.class,
					"defineHiddenClass",
					byte[].class, boolean.class, classOptions.getClass());
		} catch (Exception exception) {
			implLookup = null;
			classOptions = null;
			defineHiddenClass = null;
		}

		IMPL_LOOKUP = implLookup;
		HIDDEN_CLASS_OPTIONS = classOptions;
		DEFINE_HIDDEN_CLASS = defineHiddenClass;
	}

	@Override
	public boolean isAvailable() {
		return IMPL_LOOKUP != null && HIDDEN_CLASS_OPTIONS != null && DEFINE_HIDDEN_CLASS != null;
	}

	@Override
	public Class<?> define(Class<?> hostClass, byte[] byteCode) {
		try {
			Lookup defined = (Lookup) DEFINE_HIDDEN_CLASS.invoke(
					IMPL_LOOKUP.in(hostClass),
					byteCode, false, HIDDEN_CLASS_OPTIONS);
			return defined.lookupClass();
		} catch (Exception exception) {
			return null;
		}
	}
}
