package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.fuzzy.ClassTypeMatcher.MatchVariant;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Contains factory methods for matching classes.
 *
 * @author Kristian
 */
public class FuzzyMatchers {

    // Constant matchers
    private static final AbstractFuzzyMatcher<Class<?>> MATCH_ALL = (value, parent) -> true;

    private FuzzyMatchers() {
        // Don't make this constructable
    }

    /**
     * Construct a class matcher that matches an array with a given component matcher.
     *
     * @param componentMatcher - the component matcher.
     * @return A new array matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchArray(AbstractFuzzyMatcher<Class<?>> componentMatcher) {
        return (value, parent) -> value.isArray() && componentMatcher.isMatch(value.getComponentType(), parent);
    }

    public static AbstractFuzzyMatcher<Class<?>> except(Class<?> clazz) {
        return (value, parent) -> !clazz.isAssignableFrom(value);
    }

    public static AbstractFuzzyMatcher<Class<?>> assignable(Class<?> clazz) {
        return (value, parent) -> clazz.isAssignableFrom(value);
    }

    @SafeVarargs
    public static AbstractFuzzyMatcher<Class<?>> and(AbstractFuzzyMatcher<Class<?>>... matchers) {
        return (value, parent) -> {
            for (AbstractFuzzyMatcher<Class<?>> matcher : matchers) {
                if (!matcher.isMatch(value, parent)) {
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * Retrieve a fuzzy matcher that will match any class.
     *
     * @return A class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchAll() {
        return MATCH_ALL;
    }

    /**
     * Construct a class matcher that matches types exactly.
     *
     * @param matcher - the matching class.
     * @return A new class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchExact(Class<?> matcher) {
        return new ClassTypeMatcher(matcher, MatchVariant.MATCH_EXACT);
    }

    /**
     * Construct a class matcher that matches any of the given classes exactly.
     *
     * @param classes - list of classes to match.
     * @return A new class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Class<?>... classes) {
        return matchAnyOf(Sets.newHashSet(classes));
    }

    /**
     * Construct a class matcher that matches any of the given classes exactly.
     *
     * @param classes - set of classes to match.
     * @return A new class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchAnyOf(Set<Class<?>> classes) {
        return new ClassSetMatcher(classes);
    }

    /**
     * Construct a class matcher that matches super types of the given class.
     *
     * @param matcher - the matching type must be a super class of this type.
     * @return A new class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchSuper(Class<?> matcher) {
        return new ClassTypeMatcher(matcher, MatchVariant.MATCH_SUPER);
    }

    /**
     * Construct a class matcher that matches derived types of the given class.
     *
     * @param matcher - the matching type must be a derived class of this type.
     * @return A new class matcher.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchDerived(Class<?> matcher) {
        return new ClassTypeMatcher(matcher, MatchVariant.MATCH_DERIVED);
    }

    /**
     * Construct a class matcher based on the canonical names of classes.
     *
     * @param regex - regular expression pattern matching class names.
     * @return A fuzzy class matcher based on name.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchRegex(final Pattern regex) {
        return new ClassRegexMatcher(regex);
    }

    /**
     * Construct a class matcher based on the canonical names of classes.
     *
     * @param regex - regular expression matching class names.
     * @return A fuzzy class matcher based on name.
     */
    public static AbstractFuzzyMatcher<Class<?>> matchRegex(String regex) {
        return FuzzyMatchers.matchRegex(Pattern.compile(regex));
    }

    /**
     * Determine if two patterns are the same.
     * <p>
     * Note that two patterns may be functionally the same, but nevertheless be different.
     *
     * @param a - the first pattern.
     * @param b - the second pattern.
     * @return TRUE if they are compiled from the same pattern, FALSE otherwise.
     */
    static boolean checkPattern(Pattern a, Pattern b) {
        if (a == null) {
            return b == null;
        } else if (b == null) {
            return false;
        } else if (a == b) {
            return true;
        } else {
            return a.pattern().equals(b.pattern());
        }
    }
}
