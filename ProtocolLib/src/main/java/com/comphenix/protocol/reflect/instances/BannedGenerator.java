package com.comphenix.protocol.reflect.instances;

import javax.annotation.Nullable;

import com.comphenix.protocol.reflect.fuzzy.AbstractFuzzyMatcher;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;

/**
 * Generator that ensures certain types will never be created.
 * 
 * @author Kristian
 */
public class BannedGenerator implements InstanceProvider {
	private AbstractFuzzyMatcher<Class<?>> classMatcher;
	
	/**
	 * Construct a generator that ensures any class that matches the given matcher is never constructed.
	 * @param classMatcher - a class matcher.
	 */
	public BannedGenerator(AbstractFuzzyMatcher<Class<?>> classMatcher) {
		this.classMatcher = classMatcher;
	}
	
	public BannedGenerator(Class<?>... classes) {
		this.classMatcher = FuzzyMatchers.matchAnyOf(classes);
	}

	@Override
	public Object create(@Nullable Class<?> type) {
		// Prevent these types from being constructed
		if (classMatcher.isMatch(type, null)) {
			throw new NotConstructableException();
		}
		return null;
	}
}
