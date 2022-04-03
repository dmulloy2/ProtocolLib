package com.comphenix.protocol.reflect.fuzzy;

import java.util.Set;

/**
 * Represents a class matcher that checks for equality using a given set of classes.
 *
 * @author Kristian
 */
final class ClassSetMatcher implements AbstractFuzzyMatcher<Class<?>> {

	private final Set<Class<?>> classes;

	public ClassSetMatcher(Set<Class<?>> classes) {
		this.classes = classes;
	}

	@Override
	public boolean isMatch(Class<?> value, Object parent) {
		return this.classes.contains(value);
	}

	@Override
	public String toString() {
		return "{ type any of " + this.classes + " }";
	}
}
