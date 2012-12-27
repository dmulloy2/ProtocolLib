package com.comphenix.protocol.reflect.cloning;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.UUID;

import com.google.common.primitives.Primitives;

/**
 * Detects classes that are immutable, and thus doesn't require cloning.
 * <p>
 * This ought to have no false positives, but plenty of false negatives.
 * 
 * @author Kristian
 */
public class ImmutableDetector implements Cloner {
	// Notable immutable classes we might encounter
	private static final Class<?>[] immutableClasses = { 
		StackTraceElement.class, BigDecimal.class, 
		BigInteger.class, Locale.class, UUID.class, 
		URL.class, URI.class, Inet4Address.class, 
		Inet6Address.class, InetSocketAddress.class
	};
	
	@Override
	public boolean canClone(Object source) {
		// Don't accept NULL
		if (source == null)
			return false;
		
		return isImmutable(source.getClass());
	}
	
	/**
	 * Determine if the given type is probably immutable.
	 * @param type - the type to check.
	 * @return TRUE if the type is immutable, FALSE otherwise.
	 */
	public static boolean isImmutable(Class<?> type) {
		// Cases that are definitely not true
		if (type.isArray())
			return false;
		
		// All primitive types
		if (Primitives.isWrapperType(type) || String.class.equals(type))
			return true;
		// May not be true, but if so, that kind of code is broken anyways
		if (type.isEnum())
			return true;
			
		for (Class<?> clazz : immutableClasses)
			if (clazz.equals(type))
				return true;
		
		// Probably not
		return false;
	}
	
	@Override
	public Object clone(Object source) {
		// Safe if the class is immutable
		return source;
	}
}
