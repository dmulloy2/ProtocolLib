package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Member;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Contains factory methods for matching classes.
 * 
 * @author Kristian
 */
public class FuzzyMatchers {
	// Constant matchers
	private static AbstractFuzzyMatcher<Class<?>> MATCH_ALL = new AbstractFuzzyMatcher<Class<?>>() {
		@Override
		public boolean isMatch(Class<?> value, Object parent) {
			return true;
		}
		
		@Override
		protected int calculateRoundNumber() {
			return 0;
		}
	};
	
	private FuzzyMatchers() {
		// Don't make this constructable
	}

	/**
	 * Construct a class matcher that matches an array with a given component matcher.
	 * @param componentMatcher - the component matcher.
	 * @return A new array matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchArray(@Nonnull final AbstractFuzzyMatcher<Class<?>> componentMatcher) {
		Preconditions.checkNotNull(componentMatcher, "componentMatcher cannot be NULL.");
		return new AbstractFuzzyMatcher<Class<?>>() {
			@Override
			public boolean isMatch(Class<?> value, Object parent) {
				return value.isArray() && componentMatcher.isMatch(value.getComponentType(), parent);
			}
			
			@Override
			protected int calculateRoundNumber() {
				// We're just above object
				return -1;
			}
		};
	}
	
	/**
	 * Retrieve a fuzzy matcher that will match any class.
	 * @return A class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchAll() {
		return MATCH_ALL;
	}
	
	/**
	 * Construct a class matcher that matches types exactly.
	 * @param matcher - the matching class.
	 * @return A new class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchExact(Class<?> matcher) {
		return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_EXACT);
	}
	
	/**
	 * Construct a class matcher that matches any of the given classes exactly.
	 * @param classes - list of classes to match.
	 * @return A new class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Class<?>... classes) {
		return matchAnyOf(Sets.newHashSet(classes));
	}
	
	/**
	 * Construct a class matcher that matches any of the given classes exactly.
	 * @param classes - set of classes to match.
	 * @return A new class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Set<Class<?>> classes) {
		return new ClassSetMatcher(classes);
	}

	/**
	 * Construct a class matcher that matches super types of the given class.
	 * @param matcher - the matching type must be a super class of this type.
	 * @return A new class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchSuper(Class<?> matcher) {
		return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_SUPER);
	}

	/**
	 * Construct a class matcher that matches derived types of the given class.
	 * @param matcher - the matching type must be a derived class of this type.
	 * @return A new class matcher.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchDerived(Class<?> matcher) {
		return new ClassExactMatcher(matcher, ClassExactMatcher.Options.MATCH_DERIVED);
	}
	
	/**
	 * Construct a class matcher based on the canonical names of classes.
	 * @param regex - regular expression pattern matching class names.
	 * @param priority - the priority this matcher takes - higher is better.
	 * @return A fuzzy class matcher based on name.
	 */
	public static AbstractFuzzyMatcher<Class<?>> matchRegex(final Pattern regex, final int priority) {
		return new ClassRegexMatcher(regex, priority);
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
			
			@Override
			public String toString() {
				return "match parent class";
			}
			
			@Override
			public int hashCode() {
				return 0;
			}
			
			@Override
			public boolean equals(Object obj) {
				// If they're the same type, then yes
				return obj != null && obj.getClass() == this.getClass();
			}
		};
	}
	
	/**
	 * Determine if two patterns are the same. 
	 * <p>
	 * Note that two patterns may be functionally the same, but nevertheless be different.
	 * @param a - the first pattern.
	 * @param b - the second pattern.
	 * @return TRUE if they are compiled from the same pattern, FALSE otherwise.
	 */
	static boolean checkPattern(Pattern a, Pattern b) {
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else
			return a.pattern().equals(b.pattern());
	}
}
