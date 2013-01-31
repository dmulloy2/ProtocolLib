package com.comphenix.protocol.reflect;

import java.lang.reflect.Member;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

/**
 * Contains factory methods for matching classes.
 * 
 * @author Kristian
 */
public class FuzzyMatchers {
	private FuzzyMatchers() {
		// Don't make this constructable
	}

	/**
	 * Construct a class matcher that matches types exactly.
	 * @param matcher - the matching class.
	 * @return A new class mathcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchExact(Class<?> matcher) {
		return new ExactClassMatcher(matcher, ExactClassMatcher.Options.MATCH_EXACT);
	}
	
	/**
	 * Construct a class matcher that matches any of the given classes exactly.
	 * @param classes - list of classes to match.
	 * @return A new class mathcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Class<?>... classes) {
		return matchAnyOf(Sets.newHashSet(classes));
	}
	
	/**
	 * Construct a class matcher that matches any of the given classes exactly.
	 * @param classes - set of classes to match.
	 * @return A new class mathcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(final Set<Class<?>> classes) {
		return new AbstractFuzzyMatcher<Class<?>>() {
			@Override
			public boolean isMatch(Class<?> value, Object parent) {
				return classes.contains(value);
			}
			
			@Override
			protected int calculateRoundNumber() {
				int roundNumber = 0;
				
				// The highest round number (except zero).
				for (Class<?> clazz : classes) {
					roundNumber = combineRounds(roundNumber, -ExactClassMatcher.getClassNumber(clazz));
				}
				return roundNumber;
			}
		};
	}

	/**
	 * Construct a class matcher that matches super types of the given class.
	 * @param matcher - the matching type must be a super class of this type.
	 * @return A new class mathcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchSuper(Class<?> matcher) {
		return new ExactClassMatcher(matcher, ExactClassMatcher.Options.MATCH_SUPER);
	}

	/**
	 * Construct a class matcher that matches derived types of the given class.
	 * @param matcher - the matching type must be a derived class of this type.
	 * @return A new class mathcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchDerived(Class<?> matcher) {
		return new ExactClassMatcher(matcher, ExactClassMatcher.Options.MATCH_DERIVED);
	}
	
	/**
	 * Construct a class matcher based on the canonical names of classes.
	 * @param regex - regular expression pattern matching class names.
	 * @param priority - the priority this matcher takes - higher is better.
	 * @return A fuzzy class matcher based on name.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchRegex(final Pattern regex, final int priority) {
		return new AbstractFuzzyMatcher<Class<?>>() {
			@Override
			public boolean isMatch(Class<?> value, Object parent) {
				if (value != null)
					return regex.matcher(value.getCanonicalName()).matches();
				else
					return false;
			}
			
			@Override
			protected int calculateRoundNumber() {
				return -priority;
			}
		};
	}
	
	/**
	 * Construct a class matcher based on the canonical names of classes.
	 * @param regex - regular expression matching class names.
	 * @param priority - the priority this matcher takes - higher is better.
	 * @return A fuzzy class matcher based on name.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchRegex(String regex, final int priority) {
		return FuzzyMatchers.matchRegex(Pattern.compile(regex), priority);
	}

	/**
	 * Match the parent class of a method, field or constructor.
	 * @return Parent matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchParent() {
		return new AbstractFuzzyMatcher<Class<?>>() {
			@Override
			public boolean isMatch(Class<?> value, Object parent) {
				if (parent instanceof Member) {
					return ((Member) parent).getDeclaringClass().equals(value);
				} else if (parent instanceof Class) {
					return parent.equals(value);
				} else {
					// Can't be a match
					return false;
				}
			}
			
			@Override
			protected int calculateRoundNumber() {
				// We match a very specific type
				return -100;
			}
		};
	}
}
