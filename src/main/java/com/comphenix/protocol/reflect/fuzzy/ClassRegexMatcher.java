package com.comphenix.protocol.reflect.fuzzy;

import java.util.regex.Pattern;

/**
 * Determine if a class matches based on its name using a regular expression.
 *
 * @author Kristian
 */
final class ClassRegexMatcher implements AbstractFuzzyMatcher<Class<?>> {

	private final Pattern regex;

	public ClassRegexMatcher(Pattern regex) {
		this.regex = regex;
	}

	@Override
	public boolean isMatch(Class<?> value, Object parent) {
		if (value != null && this.regex != null) {
			return this.regex.matcher(value.getCanonicalName()).matches();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "{ type matches \"" + this.regex.pattern() + "\" }";
	}
}
