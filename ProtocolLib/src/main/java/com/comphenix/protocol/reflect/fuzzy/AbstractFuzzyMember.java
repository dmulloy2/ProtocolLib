package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Member;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Represents a matcher that matches members.
 * 
 * @author Kristian
 * @param <T> - type that it matches.
 */
public abstract class AbstractFuzzyMember<T extends Member> extends AbstractFuzzyMatcher<T> {
	// Accessibility matchers
	protected int modifiersRequired;
	protected int modifiersBanned;
	
	protected Pattern nameRegex;
	protected AbstractFuzzyMatcher<Class<?>> declaringMatcher = ExactClassMatcher.MATCH_ALL;
	
	/**
	 * Whether or not this contract can be modified.
	 */
	protected transient boolean sealed;
	
	/**
	 * Represents a builder of a fuzzy member contract.
	 * 
	 * @author Kristian
	 */
	public static abstract class Builder<T extends AbstractFuzzyMember<?>> {
		protected T member = initialMember();

		/**
		 * Add a given bit-field of required modifiers for every matching member.
		 * @param modifier - bit-field of modifiers that are required.
		 * @return This builder, for chaining.
		 */
		public Builder<T> requireModifier(int modifier) {
			member.modifiersRequired |= modifier;
			return this;
		}
		
		/**
		 * Add a given bit-field of modifers that will skip or ignore members.
		 * @param modifier - bit-field of modifiers to skip or ignore.
		 * @return This builder, for chaining.
		 */
		public Builder<T> banModifier(int modifier) {
			member.modifiersBanned |= modifier;
			return this;
		}
		
		/**
		 * Set the regular expresson that matches a members name.
		 * @param regex - new regular expression of valid names.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameRegex(String regex) {
			member.nameRegex = Pattern.compile(regex);
			return this;
		}
		
		/**
		 * Set the regular expression pattern that matches a members name.
		 * @param pattern - regular expression pattern for a valid name.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameRegex(Pattern pattern) {
			member.nameRegex = pattern;
			return this;
		}
		
		/**
		 * Set the exact name of the member we are matching.
		 * <p<
		 * This will overwrite the regular expression rule.
		 * @param name - exact name.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameExact(String name) {
			return nameRegex(Pattern.quote(name));
		}
		
		/**
		 * Require that a member is defined by this exact class.
		 * @param declaringClass - the declaring class of any matching member.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassExactType(Class<?> declaringClass) {
			member.declaringMatcher = FuzzyMatchers.matchExact(declaringClass);
			return this;
		}
		
		/**
		 * Require that a member is defined by this exact class, or any super class.
		 * @param declaringClass - the declaring class.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassSuperOf(Class<?> declaringClass) {
			member.declaringMatcher = FuzzyMatchers.matchSuper(declaringClass);
			return this;
		}
		
		/**
		 * Require that a member is defined by this exact class, or any super class.
		 * @param declaringClass - the declaring class.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassDerivedOf(Class<?> declaringClass) {
			member.declaringMatcher = FuzzyMatchers.matchDerived(declaringClass);
			return this;
		}
		
		/**
		 * Require that a member is defined by a class that matches the given matcher.
		 * @param classMatcher - class matcher.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassMatching(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			member.declaringMatcher = classMatcher;
			return this;
		}
		
		/**
		 * Construct a new instance of the current type.		
		 * @return New instance.
		 */
		@Nonnull
		protected abstract T initialMember();
		
		/**
		 * Build a new instance of this type.
		 * <p>
		 * Builders should call {@link AbstractFuzzyMember#prepareBuild()} when constructing new objects.
		 * @return New instance of this type.
		 */
		public abstract T build();
	}
	
	protected AbstractFuzzyMember() {
		// Only allow construction through the builder
	}
	
	/**
	 * Called before a builder is building a member and copying its state.
	 * <p>
	 * Use this to prepare any special values.
	 */
	protected void prepareBuild() {
		// Permit any modifier if we havent's specified anything
		if (modifiersRequired == 0) {
			modifiersRequired = Integer.MAX_VALUE;
		}
	}
	
	// Clone a given contract
	protected AbstractFuzzyMember(AbstractFuzzyMember<T> other) {
		this.modifiersRequired = other.modifiersRequired;
		this.modifiersBanned = other.modifiersBanned;
		this.nameRegex = other.nameRegex;
		this.declaringMatcher = other.declaringMatcher;
		this.sealed = true;
	}

	@Override
	public boolean isMatch(T value, Object parent) {
		int mods = value.getModifiers();
		
		// Match accessibility and name
		return (mods & modifiersRequired) != 0 &&
			   (mods & modifiersBanned) == 0 &&
			   declaringMatcher.isMatch(value.getDeclaringClass(), value) &&
			   isNameMatch(value.getName());
	}
	
	private boolean isNameMatch(String name) {
		if (nameRegex == null)
			return true;
		else
			return nameRegex.matcher(name).matches();
	}

	@Override
	protected int calculateRoundNumber() {
		// Sanity check
		if (!sealed) 
			throw new IllegalStateException("Cannot calculate round number during construction.");
		
		// NULL is zero
		return declaringMatcher.getRoundNumber();
	}
}
