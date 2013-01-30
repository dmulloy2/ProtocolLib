package com.comphenix.protocol.reflect;

import javax.annotation.Nonnull;

import com.google.common.primitives.Ints;

/**
 * Represents a matcher for fields, methods, constructors and classes.
 * <p>
 * This class should ideally never expose mutable state. Its round number must be immutable.
 * @author Kristian
 */
public abstract class AbstractFuzzyMatcher<T> implements Comparable<AbstractFuzzyMatcher<T>> {
	private Integer roundNumber;
	
	/**
	 * Used to check class equality.
	 * 
	 * @author Kristian
	 */
	protected static class ClassMatcher {
		/**
		 * Match any class.
		 */
		public static final ClassMatcher MATCH_ALL = new ClassMatcher(null, true);
		
		private final Class<?> matcher;
		private final boolean useAssignable;
		
		/**
		 * Constructs a new class matcher.
		 * @param matcher - the matching class, or NULL to represent anything. 
		 * @param useAssignable - whether or not its acceptible for the input type to be a superclass.
		 */
		public ClassMatcher(Class<?> matcher, boolean useAssignable) {
			this.matcher = matcher;
			this.useAssignable = useAssignable;
		}

		/**
		 * Determine if a given class is equivalent.
		 * <p>
		 * If the matcher is NULL, the result will only be TRUE if use assignable is TRUE.
		 * @param input - the input class defined in the source file. 
		 * @return TRUE if input is a matcher or a superclass of matcher, FALSE otherwise.
		 */
		public final boolean isClassEqual(@Nonnull Class<?> input) {
			if (input == null)
				throw new IllegalArgumentException("Input class cannot be NULL.");
			
			// Do our checking
			if (matcher == null)
				return useAssignable;
			else if (useAssignable)
				return input.isAssignableFrom(matcher); // matcher instanceof input
			else
				return input.equals(matcher);
		}
		
		/**
		 * Retrieve the number of superclasses of the specific class. 
		 * <p>
		 * Object is represented as one. All interfaces are one, unless they're derived.
		 * @param clazz - the class to test.
		 * @return The number of superclasses.
		 */
		public final int getClassNumber() {
			Class<?> clazz = matcher;
			int count = 0;
			
			// Move up the hierachy
			while (clazz != null) {
				count++;
				clazz = clazz.getSuperclass();
			}
			return count;
		}

		/**
		 * Retrieve the class we're comparing against.
		 * @return Class to compare against.
		 */
		public Class<?> getMatcher() {
			return matcher;
		}

		/**
		 * Whether or not its acceptible for the input type to be a superclass.
		 * @return TRUE if it is, FALSE otherwise.
		 */
		public boolean isUseAssignable() {
			return useAssignable;
		}
		
		@Override
		public String toString() {
			if (useAssignable)
				return "Any " + matcher;
			else
				return "Exact " + matcher;
		}
	}
	
	/**
	 * Determine if the given value is a match.
	 * @param value - the value to match.
	 * @return TRUE if it is a match, FALSE otherwise.
	 */
	public abstract boolean isMatch(T value);
	
	/**
	 * Calculate the round number indicating when this matcher should be applied. 
	 * <p>
	 * Matchers with a lower round number are applied before matchers with a higher round number.
	 * <p>
	 * By convention, this round number should be negative, except for zero in the case of a matcher 
	 * that accepts any value. A good implementation should return the inverted tree depth (class hierachy) 
	 * of the least specified type used in the matching. Thus {@link Integer} will have a lower round number than
	 * {@link Number}.
	 * 
	 * @return A number (positive or negative) that is used to order matchers.
	 */
	protected abstract int calculateRoundNumber();
	
	/**
	 * Retrieve the cached round number. This should never change once calculated.
	 * <p>
	 * Matchers with a lower round number are applied before matchers with a higher round number.
	 * @return The round number.
	 * @see {@link #calculateRoundNumber()}
	 */
	public final int getRoundNumber() {
		if (roundNumber == null) {
			return roundNumber = calculateRoundNumber();
		} else {
			return roundNumber;
		}
	}
	
	/**
	 * Combine two round numbers by taking the highest non-zero number, or return zero.
	 * @param roundA - the first round number.
	 * @param roundB - the second round number.
	 * @return The combined round number.
	 */
	protected final int combineRounds(int roundA, int roundB) {
		if (roundA == 0)
			return roundB;
		else if (roundB == 0)
			return roundA;
		else
			return Math.max(roundA, roundB);
	}
		
	@Override
	public int compareTo(AbstractFuzzyMatcher<T> obj) {
		if (obj instanceof AbstractFuzzyMatcher) {
			AbstractFuzzyMatcher<?> matcher = (AbstractFuzzyMatcher<?>) obj;
			return Ints.compare(getRoundNumber(), matcher.getRoundNumber());
		}
		// No match
		return -1;
	}
}
