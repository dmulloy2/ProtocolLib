package com.comphenix.protocol.reflect.fuzzy;

/**
 * Used to check class equality.
 *
 * @author Kristian
 */
final class ClassTypeMatcher implements AbstractFuzzyMatcher<Class<?>> {

	/**
	 * Match any class.
	 */
	public static final ClassTypeMatcher MATCH_ALL = new ClassTypeMatcher(null, MatchVariant.MATCH_SUPER);

	private final Class<?> matcher;
	private final MatchVariant variant;

	/**
	 * Constructs a new class matcher.
	 *
	 * @param matcher - the matching class, or NULL to represent anything.
	 * @param variant - options specifying the matching rules.
	 */
	ClassTypeMatcher(Class<?> matcher, MatchVariant variant) {
		this.matcher = matcher;
		this.variant = variant;
	}

	/**
	 * Determine if a given class is equivalent.
	 * <p>
	 * If the matcher is NULL, the result will only be TRUE if we're not matching exactly.
	 *
	 * @param input  - the input class defined in the source file.
	 * @param parent - the container that holds a reference to this class.
	 * @return TRUE if input matches according to the rules in {@link #getMatchVariant()}, FALSE otherwise.
	 */
	@Override
	public boolean isMatch(Class<?> input, Object parent) {
		if (input == null) {
			throw new IllegalArgumentException("Input class cannot be NULL.");
		}

		// if no type to check against is given just ensure that we're not strict checking
		if (this.matcher == null) {
			return this.variant != MatchVariant.MATCH_EXACT;
		}

		switch (this.variant) {
			case MATCH_EXACT:
				return this.matcher.equals(input);
			case MATCH_DERIVED:
				return this.matcher.isAssignableFrom(input);
			case MATCH_SUPER:
				return input.isAssignableFrom(this.matcher);
			default:
				// unknown option?
				return false;
		}
	}

	/**
	 * Retrieve the class we're comparing against.
	 *
	 * @return Class to compare against.
	 */
	public Class<?> getMatcher() {
		return this.matcher;
	}

	/**
	 * The matching rules for this class matcher.
	 *
	 * @return The current matching option.
	 */
	public MatchVariant getMatchVariant() {
		return this.variant;
	}

	@Override
	public String toString() {
		switch (this.variant) {
			case MATCH_EXACT:
				return "{ type exactly " + this.matcher + " }";
			case MATCH_SUPER:
				return "{ type " + this.matcher + " instanceof input }";
			case MATCH_DERIVED:
				return "{ type input instanceof " + this.matcher + " }";
			default:
				throw new IllegalArgumentException("Unknown match variant " + this.variant);
		}
	}

	/**
	 * Different matching rules.
	 */
	enum MatchVariant {

		/**
		 * Match classes exactly.
		 */
		MATCH_EXACT,

		/**
		 * A match if the input class is a superclass of the matcher class, or the same class.
		 */
		MATCH_SUPER,

		/**
		 * A match if the input class is a derived class of the matcher class, or the same class.
		 */
		MATCH_DERIVED
	}
}
