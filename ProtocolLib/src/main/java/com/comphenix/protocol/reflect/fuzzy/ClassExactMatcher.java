package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;

/**
 * Used to check class equality.
 * 
 * @author Kristian
 */
class ClassExactMatcher extends AbstractFuzzyMatcher<Class<?>> {
	/**
	 * Different matching rules.
	 */
	enum Options {
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
	
	/**
	 * Match any class.
	 */
	public static final ClassExactMatcher MATCH_ALL = new ClassExactMatcher(null, Options.MATCH_SUPER);
	
	private final Class<?> matcher;
	private final Options option;
	
	/**
	 * Constructs a new class matcher.
	 * @param matcher - the matching class, or NULL to represent anything. 
	 * @param option - options specifying the matching rules.
	 */
	ClassExactMatcher(Class<?> matcher, Options option) {
		this.matcher = matcher;
		this.option = option;
	}

	/**
	 * Determine if a given class is equivalent.
	 * <p>
	 * If the matcher is NULL, the result will only be TRUE if we're not matching exactly.
	 * @param input - the input class defined in the source file. 
	 * @param parent - the container that holds a reference to this class.
	 * @return TRUE if input matches according to the rules in {@link #getOptions()}, FALSE otherwise.
	 */
	@Override
	public boolean isMatch(Class<?> input, Object parent) {
		if (input == null)
			throw new IllegalArgumentException("Input class cannot be NULL.");
		
		// Do our checking
		if (matcher == null)
			return option != Options.MATCH_EXACT;
		else if (option == Options.MATCH_SUPER)
			return input.isAssignableFrom(matcher); // matcher instanceof input
		else if (option == Options.MATCH_DERIVED)
			return matcher.isAssignableFrom(input); // input instanceof matcher
		else
			return input.equals(matcher);
	}
	
	@Override
	protected int calculateRoundNumber() {
		return -getClassNumber(matcher);
	}
	
	/**
	 * Retrieve the number of superclasses of the specific class. 
	 * <p>
	 * Object is represented as one. All interfaces are one, unless they're derived.
	 * @param clazz - the class to test.
	 * @return The number of superclasses.
	 */
	public static int getClassNumber(Class<?> clazz) {
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
	 * The matching rules for this class matcher.
	 * @return The current matching option.
	 */
	public Options getOptions() {
		return option;
	}
	
	@Override
	public String toString() {
		if (option == Options.MATCH_SUPER)
			return matcher + " instanceof input";
		else if (option == Options.MATCH_DERIVED)
			return "input instanceof " + matcher;
		else
			return "Exact " + matcher;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(matcher, option);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof ClassExactMatcher) {
			ClassExactMatcher other = (ClassExactMatcher) obj;
			
			return Objects.equal(matcher, other.matcher) && 
					Objects.equal(option, other.option);
		}
		return false;
	}
}
