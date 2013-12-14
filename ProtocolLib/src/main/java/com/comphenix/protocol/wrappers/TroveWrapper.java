package com.comphenix.protocol.wrappers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.ReadOnlyFieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.utility.ClassSource;
import com.google.common.base.Function;

/**
 * Wrap a GNU Trove Collection class with an equivalent Java Collection class.
 * @author Kristian
 */
public class TroveWrapper {
	private static final String[] TROVE_LOCATIONS = new String[] {
		"net.minecraft.util.gnu.trove",  // For Minecraft 1.7.2
		"gnu.trove" 				     // For an old version of Spigot
	};
	
	// The different Trove versions
	private static final ClassSource[] TROVE_SOURCES = {
		ClassSource.fromPackage(TROVE_LOCATIONS[0]),
		ClassSource.fromPackage(TROVE_LOCATIONS[1])
	};
	
	/**
	 * Retrieve a read-only field accessor that automatically wraps the underlying Trove instance.
	 * @param accessor - the accessor.
	 * @return The read only accessor.
	 */
	public static ReadOnlyFieldAccessor wrapMapField(final FieldAccessor accessor) {
		return wrapMapField(accessor, null);
	}
	
	/**
	 * Retrieve a read-only field accessor that automatically wraps the underlying Trove instance.
	 * @param accessor - the accessor.
	 * @param noEntryTransform - transform the no entry value, or NULL to ignore.
	 * @return The read only accessor.
	 */
	public static ReadOnlyFieldAccessor wrapMapField(final FieldAccessor accessor, final Function<Integer, Integer> noEntryTransform) {
		return new ReadOnlyFieldAccessor() {
			public Object get(Object instance) {
				Object troveMap = accessor.get(instance);
				
				// Apply transform as well
				if (noEntryTransform != null)
					TroveWrapper.transformNoEntryValue(troveMap, noEntryTransform);
				return getDecoratedMap(troveMap);
			}
			public Field getField() {
				return accessor.getField();
			}
		};
	}
	
	/**
	 * Retrieve a read-only field accessor that automatically wraps the underlying Trove instance.
	 * @param accessor - the accessor.
	 * @return The read only accessor.
	 */
	public static ReadOnlyFieldAccessor wrapSetField(final FieldAccessor accessor) {
		return new ReadOnlyFieldAccessor() {
			public Object get(Object instance) {
				return getDecoratedSet(accessor.get(instance));
			}
			public Field getField() {
				return accessor.getField();
			}
		};
	}
	
	/**
	 * Retrieve a read-only field accessor that automatically wraps the underlying Trove instance.
	 * @param accessor - the accessor.
	 * @return The read only accessor.
	 */
	public static ReadOnlyFieldAccessor wrapListField(final FieldAccessor accessor) {
		return new ReadOnlyFieldAccessor() {
			public Object get(Object instance) {
				return getDecoratedList(accessor.get(instance));
			}
			public Field getField() {
				return accessor.getField();
			}
		};
	}
	
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
	
	/**
	 * Determine if the given class is found within gnu.trove.
	 * @param clazz - the clazz.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isTroveClass(Class<?> clazz) {
		return getClassSource(clazz) != null;
	}
	
	/**
	 * Transform the no entry value in the given map.
	 * @param troveMap - the trove map.
	 * @param transform - the transform.
	 */
	public static void transformNoEntryValue(Object troveMap, Function<Integer, Integer> transform) {
		// Check for stupid no_entry_values
		try {
			Field field = FieldUtils.getField(troveMap.getClass(), "no_entry_value", true);
			int current = (Integer) FieldUtils.readField(field, troveMap, true);
			int transformed = transform.apply(current);
			
			if (current != transformed) {
				FieldUtils.writeField(field, troveMap, transformed);
			}
		} catch (IllegalArgumentException e) {
			throw new CannotFindTroveNoEntryValue(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot access reflection.", e);
		}
	}
	
	/**
	 * Retrieve the correct class source from the given class.
	 * @param clazz - the class source.
	 * @return The class source, or NULL if not found.
	 */
	private static ClassSource getClassSource(Class<?> clazz) {
		for (int i = 0; i < TROVE_LOCATIONS.length; i++) {
			if (clazz.getCanonicalName().startsWith(TROVE_LOCATIONS[i])) {
				return TROVE_SOURCES[i];
			}
		}
		return null;
	}
	
	/**
	 * Retrieve the corresponding decorator. 
	 * @param trove - the trove class.
	 * @return The wrapped trove class.
	 */
	private static Object getDecorated(@Nonnull Object trove) {
		if (trove == null)
			throw new IllegalArgumentException("trove instance cannot be non-null.");
		
		AbstractFuzzyMatcher<Class<?>> match = FuzzyMatchers.matchSuper(trove.getClass());
		Class<?> decorators = null;
		
		try {
			// Attempt to get decorator class
			decorators = getClassSource(trove.getClass()).loadClass("TDecorators");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
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
	
	public static class CannotFindTroveNoEntryValue extends RuntimeException {
		private static final long serialVersionUID = 1L;

		private CannotFindTroveNoEntryValue(Throwable inner) {
			super("Cannot correct trove map.", inner);
		}
	}
}
