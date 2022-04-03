package com.comphenix.protocol.reflect.fuzzy;

/**
 * Represents a matcher for fields, methods, constructors and classes.
 * <p>
 * This class should ideally never expose mutable state. Its round number must be immutable.
 *
 * @author Kristian
 */
@FunctionalInterface
public interface AbstractFuzzyMatcher<T> {

	/**
	 * Determine if the given value is a match.
	 *
	 * @param value  - the value to match.
	 * @param parent - the parent container, or NULL if this value is the root.
	 * @return TRUE if it is a match, FALSE otherwise.
	 */
	boolean isMatch(T value, Object parent);

	/**
	 * Create a fuzzy matcher that returns the opposite result of the current matcher.
	 *
	 * @return An inverted fuzzy matcher.
	 */
	default AbstractFuzzyMatcher<T> inverted() {
		return (value, parent) -> !this.isMatch(value, parent);
	}

	/**
	 * Require that this and the given matcher be TRUE.
	 *
	 * @param other - the other fuzzy matcher.
	 * @return A combined fuzzy matcher.
	 */
	default AbstractFuzzyMatcher<T> and(final AbstractFuzzyMatcher<T> other) {
		// They both have to be true
		return (value, parent) -> this.isMatch(value, parent) && other.isMatch(value, parent);
	}

	/**
	 * Require that either this or the other given matcher be TRUE.
	 *
	 * @param other - the other fuzzy matcher.
	 * @return A combined fuzzy matcher.
	 */
	default AbstractFuzzyMatcher<T> or(final AbstractFuzzyMatcher<T> other) {
		// Either can be true
		return (value, parent) -> this.isMatch(value, parent) || other.isMatch(value, parent);
	}
}
