package com.comphenix.protocol.reflect.fuzzy;

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
	 * Determine if the given value is a match.
	 * @param value - the value to match.
	 * @param parent - the parent container, or NULL if this value is the root.
	 * @return TRUE if it is a match, FALSE otherwise.
	 */
	public abstract boolean isMatch(T value, Object parent);
	
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
	 * @see #calculateRoundNumber()
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
	
	/**
	 * Combine n round numbers by taking the highest non-zero number, or return zero.
	 * @param rounds - the round numbers.
	 * @return The combined round number.
	 */
	protected final int combineRounds(Integer... rounds) {
		if (rounds.length < 2)
			throw new IllegalArgumentException("Must supply at least two arguments.");
		
		// Get the seed
		int reduced = combineRounds(rounds[0], rounds[1]);
		
		// Aggregate it all 
		for (int i = 2; i < rounds.length; i++) {
			reduced = combineRounds(reduced, rounds[i]);
		}
		return reduced;
	}
		
	@Override
	public int compareTo(AbstractFuzzyMatcher<T> obj) {
		return Ints.compare(getRoundNumber(), obj.getRoundNumber());
	}

	/**
	 * Create a fuzzy matcher that returns the opposite result of the current matcher.
	 * @return An inverted fuzzy matcher.
	 */
	public AbstractFuzzyMatcher<T> inverted() {
		return new AbstractFuzzyMatcher<T>() {
			@Override
			public boolean isMatch(T value, Object parent) {
				return !AbstractFuzzyMatcher.this.isMatch(value, parent);
			}
			
			@Override
			protected int calculateRoundNumber() {
				return -2;
			}
		};
	}
	
	/**
	 * Require that this and the given matcher be TRUE.
	 * @param other - the other fuzzy matcher.
	 * @return A combined fuzzy matcher.
	 */
	public AbstractFuzzyMatcher<T> and(final AbstractFuzzyMatcher<T> other) {
		return new AbstractFuzzyMatcher<T>() {
			@Override
			public boolean isMatch(T value, Object parent) {
				// They both have to be true
				return AbstractFuzzyMatcher.this.isMatch(value, parent) &&
									       other.isMatch(value, parent);
			}
			
			@Override
			protected int calculateRoundNumber() {
				return combineRounds(AbstractFuzzyMatcher.this.getRoundNumber(), other.getRoundNumber());
			}
		};
	}
	
	/**
	 * Require that either this or the other given matcher be TRUE.
	 * @param other - the other fuzzy matcher.
	 * @return A combined fuzzy matcher.
	 */
	public AbstractFuzzyMatcher<T> or(final AbstractFuzzyMatcher<T> other) {
		return new AbstractFuzzyMatcher<T>() {
			@Override
			public boolean isMatch(T value, Object parent) {
				// Either can be true
				return AbstractFuzzyMatcher.this.isMatch(value, parent) ||
									       other.isMatch(value, parent);
			}
			
			@Override
			protected int calculateRoundNumber() {
				return combineRounds(AbstractFuzzyMatcher.this.getRoundNumber(), other.getRoundNumber());
			}
		};
	}
}
