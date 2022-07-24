package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Represents a field matcher.
 *
 * @author Kristian
 */
public class FuzzyFieldContract extends AbstractFuzzyMember<Field> {

	private AbstractFuzzyMatcher<Class<?>> typeMatcher = ClassTypeMatcher.MATCH_ALL;

	private FuzzyFieldContract() {
	}

	/**
	 * Create a new field contract from the given contract.
	 *
	 * @param other - the contract to clone.
	 */
	private FuzzyFieldContract(FuzzyFieldContract other) {
		super(other);
		this.typeMatcher = other.typeMatcher;
	}

	/**
	 * Match a field by its type.
	 *
	 * @param matcher - the type to match.
	 * @return The field contract.
	 */
	public static FuzzyFieldContract matchType(AbstractFuzzyMatcher<Class<?>> matcher) {
		return newBuilder().typeMatches(matcher).build();
	}

	/**
	 * Return a new fuzzy field contract builder.
	 *
	 * @return New fuzzy field contract builder.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Retrieve the class matcher that matches the type of a field.
	 *
	 * @return The class matcher.
	 */
	public AbstractFuzzyMatcher<Class<?>> getTypeMatcher() {
		return this.typeMatcher;
	}

	@Override
	public boolean isMatch(Field value, Object parent) {
		if (super.isMatch(value, parent)) {
			return this.typeMatcher.isMatch(value.getType(), value);
		}

		// No match
		return false;
	}

	@Override
	protected Map<String, Object> getKeyValueView() {
		Map<String, Object> member = super.getKeyValueView();
		if (this.typeMatcher != ClassTypeMatcher.MATCH_ALL) {
			member.put("type", this.typeMatcher);
		}

		return member;
	}

	/**
	 * Represents a builder for a field matcher.
	 *
	 * @author Kristian
	 */
	public static class Builder extends AbstractFuzzyMember.Builder<FuzzyFieldContract> {

		@Override
		public Builder requireModifier(int modifier) {
			super.requireModifier(modifier);
			return this;
		}

		@Override
		public Builder banModifier(int modifier) {
			super.banModifier(modifier);
			return this;
		}

		@Override
		public Builder requirePublic() {
			super.requirePublic();
			return this;
		}

		@Override
		public Builder nameRegex(String regex) {
			super.nameRegex(regex);
			return this;
		}

		@Override
		public Builder nameRegex(Pattern pattern) {
			super.nameRegex(pattern);
			return this;
		}

		@Override
		public Builder nameExact(String name) {
			super.nameExact(name);
			return this;
		}

		public Builder declaringClassExactType(Class<?> declaringClass) {
			super.declaringClassExactType(declaringClass);
			return this;
		}

		@Override
		public Builder declaringClassSuperOf(Class<?> declaringClass) {
			super.declaringClassSuperOf(declaringClass);
			return this;
		}

		@Override
		public Builder declaringClassDerivedOf(Class<?> declaringClass) {
			super.declaringClassDerivedOf(declaringClass);
			return this;
		}

		@Override
		public Builder declaringClassMatching(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			super.declaringClassMatching(classMatcher);
			return this;
		}

		@Override
		@Nonnull
		protected FuzzyFieldContract initialMember() {
			return new FuzzyFieldContract();
		}

		public Builder typeExact(Class<?> type) {
			this.member.typeMatcher = FuzzyMatchers.matchExact(type);
			return this;
		}

		public Builder typeSuperOf(Class<?> type) {
			this.member.typeMatcher = FuzzyMatchers.matchSuper(type);
			return this;
		}

		public Builder typeDerivedOf(Class<?> type) {
			this.member.typeMatcher = FuzzyMatchers.matchDerived(type);
			return this;
		}

		public Builder typeMatches(AbstractFuzzyMatcher<Class<?>> matcher) {
			this.member.typeMatcher = matcher;
			return this;
		}

		@Override
		public FuzzyFieldContract build() {
			this.member.prepareBuild();
			return new FuzzyFieldContract(this.member);
		}
	}
}
