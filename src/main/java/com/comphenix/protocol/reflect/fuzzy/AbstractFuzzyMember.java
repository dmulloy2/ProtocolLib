package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.regex.Pattern;

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
	protected AbstractFuzzyMatcher<Class<?>> declaringMatcher = ClassExactMatcher.MATCH_ALL;
	
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
		 * Require that every matching member is public.
		 * @return This builder, for chaining.
		 */
		public Builder<T> requirePublic() {
			return requireModifier(Modifier.PUBLIC);
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
		 * <p>
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
		// No need to prepare anything
	}
	
	// Clone a given contract
	protected AbstractFuzzyMember(AbstractFuzzyMember<T> other) {
		this.modifiersRequired = other.modifiersRequired;
		this.modifiersBanned = other.modifiersBanned;
		this.nameRegex = other.nameRegex;
		this.declaringMatcher = other.declaringMatcher;
		this.sealed = true;
	}

	/**
	 * Retrieve a bit field of every {@link java.lang.reflect.Modifier Modifier} that is required for the member to match.
	 * @return A required modifier bit field.
	 */
	public int getModifiersRequired() {
		return modifiersRequired;
	}

	/**
	 * Retrieve a bit field of every {@link java.lang.reflect.Modifier Modifier} that must not be present for the member to match.
	 * @return A banned modifier bit field.
	 */
	public int getModifiersBanned() {
		return modifiersBanned;
	}

	/**
	 * Retrieve the regular expression pattern that is used to match the name of a member.
	 * @return The regex matching a name, or NULL if everything matches.
	 */
	public Pattern getNameRegex() {
		return nameRegex;
	}

	/**
	 * Retrieve a class matcher for the declaring class of the member.
	 * @return An object matching the declaring class.
	 */
	public AbstractFuzzyMatcher<Class<?>> getDeclaringMatcher() {
		return declaringMatcher;
	}

	@Override
	public boolean isMatch(T value, Object parent) {
		int mods = value.getModifiers();
		
		// Match accessibility and name
		return (mods & modifiersRequired) == modifiersRequired &&
			   (mods & modifiersBanned) == 0 &&
			   declaringMatcher.isMatch(value.getDeclaringClass(), value) &&
			   isNameMatch(value.getName());
	}
	
	/**
	 * Determine if a given name matches the current member matcher.
	 * @param name - the name to match.
	 * @return TRUE if the name matches, FALSE otherwise.
	 */
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
	
	@Override
	public String toString() {
		return getKeyValueView().toString();
	}
	
	/**
	 * Generate a view of this matcher as a key-value map.
	 * <p>
	 * Used by {@link #toString()} to print a representation of this object.
	 * @return A modifiable key-value view.
	 */
	protected Map<String, Object> getKeyValueView() {
		Map<String, Object> map = Maps.newLinkedHashMap();
		
		// Build our representation
		if (modifiersRequired != Integer.MAX_VALUE || modifiersBanned != 0) {
			map.put("modifiers", String.format("[required: %s, banned: %s]",
				   getBitView(modifiersRequired, 16),
				   getBitView(modifiersBanned, 16))
			);
		}
		if (nameRegex != null) {
			map.put("name", nameRegex.pattern());
		}
		if (declaringMatcher != ClassExactMatcher.MATCH_ALL) {
			map.put("declaring", declaringMatcher);
		}
		
		return map;
	}
	
	private static String getBitView(int value, int bits) {
		if (bits < 0 || bits > 31)
			throw new IllegalArgumentException("Bits must be a value between 0 and 32");
		
		// Extract our needed bits
		int snipped = value & ((1 << bits) - 1);
		return Integer.toBinaryString(snipped);
	}
	
	@Override
	public boolean equals(Object obj) {
		// Immutablity is awesome
		if (this == obj) {
			return true;
		} else if (obj instanceof AbstractFuzzyMember) {
			@SuppressWarnings("unchecked")
			AbstractFuzzyMember<T> other = (AbstractFuzzyMember<T>) obj;
			
			return modifiersBanned == other.modifiersBanned &&
				   modifiersRequired == other.modifiersRequired &&
				   FuzzyMatchers.checkPattern(nameRegex, other.nameRegex) &&
				   Objects.equal(declaringMatcher, other.declaringMatcher);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hashCode(modifiersBanned, modifiersRequired,
					nameRegex != null ? nameRegex.pattern() : null, declaringMatcher);
	}
}
