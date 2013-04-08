package com.comphenix.protocol.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;

/**
 * Wrap a GNU Trove Collection class with an equivalent Java Collection class.
 * @author Kristian
 */
public class TroveWrapper {
	private volatile static Class<?> decorators;
	
	/**
	 * Retrieve a Java wrapper for the corresponding Trove map.
	 * @param troveMap - the trove map to wrap.
	 * @return The wrapped GNU Trove map.
	 * @throws IllegalStateException If GNU Trove cannot be found in the class map.
	 * @throws IllegalArgumentException If troveMap is NULL.
	 * @throws FieldAccessException Error in wrapper method or lack of reflection permissions.
	 */
	public static <TKey, TValue> Map<TKey, TValue> getDecoratedMap(@Nonnull Object troveMap) {
		@SuppressWarnings("unchecked")
		Map<TKey, TValue> result = (Map<TKey, TValue>) getDecorated(troveMap);
		return result;
	}
	
	/**
	 * Retrieve a Java wrapper for the corresponding Trove set.
	 * @param troveSet - the trove set to wrap.
	 * @return The wrapped GNU Trove set.
	 * @throws IllegalStateException If GNU Trove cannot be found in the class map.
	 * @throws IllegalArgumentException If troveSet is NULL.
	 * @throws FieldAccessException Error in wrapper method or lack of reflection permissions.
	 */
	public static <TValue> Set<TValue> getDecoratedSet(@Nonnull Object troveSet) {
		@SuppressWarnings("unchecked")
		Set<TValue> result = (Set<TValue>) getDecorated(troveSet);
		return result;
	}
	
	/**
	 * Retrieve a Java wrapper for the corresponding Trove list.
	 * @param troveList - the trove list to wrap.
	 * @return The wrapped GNU Trove list.
	 * @throws IllegalStateException If GNU Trove cannot be found in the class map.
	 * @throws IllegalArgumentException If troveList is NULL.
	 * @throws FieldAccessException Error in wrapper method or lack of reflection permissions.
	 */
	public static <TValue> List<TValue> getDecoratedList(@Nonnull Object troveList) {
		@SuppressWarnings("unchecked")
		List<TValue> result = (List<TValue>) getDecorated(troveList);
		return result;
	}
	
	private static Object getDecorated(@Nonnull Object trove) {
		if (trove == null)
			throw new IllegalArgumentException("trove instance cannot be non-null.");
		
		AbstractFuzzyMatcher<Class<?>> match = FuzzyMatchers.matchSuper(trove.getClass());
		
		if (decorators == null) {
			try {
				// Attempt to get decorator class
				decorators = TroveWrapper.class.getClassLoader().loadClass("gnu.trove.TDecorators");
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("Cannot find TDecorators in Gnu Trove.", e);
			}
		}
		
		// Find an appropriate wrapper method in TDecorators
		for (Method method : decorators.getMethods()) {
			Class<?>[] types = method.getParameterTypes();
			
			if (types.length == 1 && match.isMatch(types[0], null)) {
				try {
					Object result = method.invoke(null, trove);
					
					if (result == null)
						throw new FieldAccessException("Wrapper returned NULL.");
					else
						return result;
					
				} catch (IllegalArgumentException e) {
					throw new FieldAccessException("Cannot invoke wrapper method.", e);
				} catch (IllegalAccessException e) {
					throw new FieldAccessException("Illegal access.", e);
				} catch (InvocationTargetException e) {
					throw new FieldAccessException("Error in invocation.", e);
				}
			}
		}
		
		throw new IllegalArgumentException("Cannot find decorator for " + trove + " (" + trove.getClass() + ")");
	}
}
