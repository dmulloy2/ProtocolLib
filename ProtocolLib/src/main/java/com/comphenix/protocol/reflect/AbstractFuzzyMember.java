package com.comphenix.protocol.reflect;

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
	private int modifiersRequired;
	private int modifiersBanned;
	private Pattern nameRegex;
	private ClassMatcher declaringMatcher = ClassMatcher.MATCH_ALL;
	
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

		public Builder<T> requireModifier(int modifier) {
			member.modifiersRequired |= modifier;
			return this;
		}
		
		public Builder<T> banModifier(int modifier) {
			member.modifiersBanned |= modifier;
			return this;
		}
		
		public Builder<T> nameRegex(String regex) {
			member.nameRegex = Pattern.compile(regex);
			return this;
		}
		
		public Builder<T> nameRegex(Pattern pattern) {
			member.nameRegex = pattern;
			return this;
		}
		
		public Builder<T> nameExact(String name) {
			return nameRegex(Pattern.quote(name));
		}
		
		public Builder<T> declaringClassExactType(Class<?> declaringClass) {
			member.declaringMatcher = new ClassMatcher(declaringClass, false);
			return this;
		}
		
		public Builder<T> declaringClassCanHold(Class<?> declaringClass) {
			member.declaringMatcher = new ClassMatcher(declaringClass, true);
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
	public boolean isMatch(T value) {
		int mods = value.getModifiers();
		
		// Match accessibility and name
		return (mods & modifiersRequired) != 0 &&
			   (mods & modifiersBanned) == 0 &&
			   declaringMatcher.isClassEqual(value.getDeclaringClass()) &&
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
		return -declaringMatcher.getClassNumber();
	}
}
