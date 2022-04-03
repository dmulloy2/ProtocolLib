package com.comphenix.protocol.reflect.fuzzy;

import com.google.common.collect.Maps;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Represents a matcher that matches members.
 *
 * @param <T> - type that it matches.
 * @author Kristian
 */
public abstract class AbstractFuzzyMember<T extends Member> implements AbstractFuzzyMatcher<T> {

	// Accessibility matchers
	protected int modifiersRequired;
	protected int modifiersBanned;

	protected Pattern nameRegex;
	protected AbstractFuzzyMatcher<Class<?>> declaringMatcher = ClassTypeMatcher.MATCH_ALL;

	/**
	 * Whether this contract can be modified.
	 */
	protected transient boolean sealed;

	protected AbstractFuzzyMember() {
		// Only allow construction through the builder
	}

	// Clone a given contract
	protected AbstractFuzzyMember(AbstractFuzzyMember<T> other) {
		this.modifiersRequired = other.modifiersRequired;
		this.modifiersBanned = other.modifiersBanned;
		this.nameRegex = other.nameRegex;
		this.declaringMatcher = other.declaringMatcher;
		this.sealed = true;
	}

	private static String getBitView(int value, int bits) {
		if (bits < 0 || bits > 31) {
			throw new IllegalArgumentException("Bits must be a value between 0 and 32");
		}

		// Extract our needed bits
		int snipped = value & ((1 << bits) - 1);
		return Integer.toBinaryString(snipped);
	}

	/**
	 * Called before a builder is building a member and copying its state.
	 * <p>
	 * Use this to prepare any special values.
	 */
	protected void prepareBuild() {
		// No need to prepare anything
	}

	/**
	 * Retrieve a bit field of every {@link java.lang.reflect.Modifier Modifier} that is required for the member to
	 * match.
	 *
	 * @return A required modifier bit field.
	 */
	public int getModifiersRequired() {
		return this.modifiersRequired;
	}

	/**
	 * Retrieve a bit field of every {@link java.lang.reflect.Modifier Modifier} that must not be present for the member
	 * to match.
	 *
	 * @return A banned modifier bit field.
	 */
	public int getModifiersBanned() {
		return this.modifiersBanned;
	}

	/**
	 * Retrieve the regular expression pattern that is used to match the name of a member.
	 *
	 * @return The regex matching a name, or NULL if everything matches.
	 */
	public Pattern getNameRegex() {
		return this.nameRegex;
	}

	/**
	 * Retrieve a class matcher for the declaring class of the member.
	 *
	 * @return An object matching the declaring class.
	 */
	public AbstractFuzzyMatcher<Class<?>> getDeclaringMatcher() {
		return this.declaringMatcher;
	}

	@Override
	public boolean isMatch(T value, Object parent) {
		int mods = value.getModifiers();

		// Match accessibility and name
		return (mods & this.modifiersRequired) == this.modifiersRequired
				&& (mods & this.modifiersBanned) == 0
				&& this.declaringMatcher.isMatch(value.getDeclaringClass(), value)
				&& this.isNameMatch(value.getName());
	}

	/**
	 * Determine if a given name matches the current member matcher.
	 *
	 * @param name - the name to match.
	 * @return TRUE if the name matches, FALSE otherwise.
	 */
	private boolean isNameMatch(String name) {
		if (this.nameRegex == null) {
			return true;
		} else {
			return this.nameRegex.matcher(name).matches();
		}
	}

	@Override
	public String toString() {
		return this.getKeyValueView().toString();
	}

	/**
	 * Generate a view of this matcher as a key-value map.
	 * <p>
	 * Used by {@link #toString()} to print a representation of this object.
	 *
	 * @return A modifiable key-value view.
	 */
	protected Map<String, Object> getKeyValueView() {
		Map<String, Object> map = Maps.newLinkedHashMap();

		// Build our representation
		if (this.modifiersRequired != Integer.MAX_VALUE || this.modifiersBanned != 0) {
			map.put("modifiers", String.format(
					"[required: %s, banned: %s]",
					getBitView(this.modifiersRequired, 16),
					getBitView(this.modifiersBanned, 16)));
		}

		if (this.nameRegex != null) {
			map.put("name", this.nameRegex.pattern());
		}

		if (this.declaringMatcher != ClassTypeMatcher.MATCH_ALL) {
			map.put("declaring", this.declaringMatcher);
		}

		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof AbstractFuzzyMember) {
			AbstractFuzzyMember<?> other = (AbstractFuzzyMember<?>) obj;
			return this.modifiersBanned == other.modifiersBanned
					&& this.modifiersRequired == other.modifiersRequired
					&& FuzzyMatchers.checkPattern(this.nameRegex, other.nameRegex)
					&& Objects.equals(this.declaringMatcher, other.declaringMatcher);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				this.modifiersBanned,
				this.modifiersRequired,
				this.nameRegex != null ? this.nameRegex.pattern() : null,
				this.declaringMatcher);
	}

	/**
	 * Represents a builder of a fuzzy member contract.
	 *
	 * @author Kristian
	 */
	public static abstract class Builder<T extends AbstractFuzzyMember<?>> {

		protected T member = this.initialMember();

		/**
		 * Add a given bit-field of required modifiers for every matching member.
		 *
		 * @param modifier - bit-field of modifiers that are required.
		 * @return This builder, for chaining.
		 */
		public Builder<T> requireModifier(int modifier) {
			this.member.modifiersRequired |= modifier;
			return this;
		}

		/**
		 * Require that every matching member is public.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder<T> requirePublic() {
			return this.requireModifier(Modifier.PUBLIC);
		}

		/**
		 * Add a given bit-field of modifers that will skip or ignore members.
		 *
		 * @param modifier - bit-field of modifiers to skip or ignore.
		 * @return This builder, for chaining.
		 */
		public Builder<T> banModifier(int modifier) {
			this.member.modifiersBanned |= modifier;
			return this;
		}

		/**
		 * Set the regular expresson that matches a members name.
		 *
		 * @param regex - new regular expression of valid names.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameRegex(String regex) {
			this.member.nameRegex = Pattern.compile(regex);
			return this;
		}

		/**
		 * Set the regular expression pattern that matches a members name.
		 *
		 * @param pattern - regular expression pattern for a valid name.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameRegex(Pattern pattern) {
			this.member.nameRegex = pattern;
			return this;
		}

		/**
		 * Set the exact name of the member we are matching.
		 * <p>
		 * This will overwrite the regular expression rule.
		 *
		 * @param name - exact name.
		 * @return This builder, for chaining.
		 */
		public Builder<T> nameExact(String name) {
			return this.nameRegex(Pattern.quote(name));
		}

		/**
		 * Require that a member is defined by this exact class.
		 *
		 * @param declaringClass - the declaring class of any matching member.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassExactType(Class<?> declaringClass) {
			this.member.declaringMatcher = FuzzyMatchers.matchExact(declaringClass);
			return this;
		}

		/**
		 * Require that a member is defined by this exact class, or any super class.
		 *
		 * @param declaringClass - the declaring class.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassSuperOf(Class<?> declaringClass) {
			this.member.declaringMatcher = FuzzyMatchers.matchSuper(declaringClass);
			return this;
		}

		/**
		 * Require that a member is defined by this exact class, or any super class.
		 *
		 * @param declaringClass - the declaring class.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassDerivedOf(Class<?> declaringClass) {
			this.member.declaringMatcher = FuzzyMatchers.matchDerived(declaringClass);
			return this;
		}

		/**
		 * Require that a member is defined by a class that matches the given matcher.
		 *
		 * @param classMatcher - class matcher.
		 * @return This builder, for chaining.
		 */
		public Builder<T> declaringClassMatching(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			this.member.declaringMatcher = classMatcher;
			return this;
		}

		/**
		 * Construct a new instance of the current type.
		 *
		 * @return New instance.
		 */
		@Nonnull
		protected abstract T initialMember();

		/**
		 * Build a new instance of this type.
		 * <p>
		 * Builders should call {@link AbstractFuzzyMember#prepareBuild()} when constructing new objects.
		 *
		 * @return New instance of this type.
		 */
		public abstract T build();
	}
}
