package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Represents a field matcher.
 * 
 * @author Kristian
 */
public class FuzzyFieldContract extends AbstractFuzzyMember<Field> {
	private AbstractFuzzyMatcher<Class<?>> typeMatcher = ExactClassMatcher.MATCH_ALL;
	
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
			member.typeMatcher = ExactClassMatcher.matchExact(type);
			return this;
		}
		
		public Builder typeSuperOf(Class<?> type) {
			member.typeMatcher = ExactClassMatcher.matchSuper(type);
			return this;
		}

		@Override
		public FuzzyFieldContract build() {
			member.prepareBuild();
			return new FuzzyFieldContract(member);
		}
	}

	public static Builder newBuilder() {
		return new Builder();
	}
	
	private FuzzyFieldContract() {
		// Only allow construction through the builder
		super();
	}
	
	/**
	 * Create a new field contract from the given contract.
	 * @param other - the contract to clone.
	 */
	private FuzzyFieldContract(FuzzyFieldContract other) {
		super(other);
		this.typeMatcher = other.typeMatcher;
	}
	
	@Override
	public boolean isMatch(Field value, Object parent) {
		if (super.isMatch(value, parent)) {
			return typeMatcher.isMatch(value.getType(), value);
		}
		// No match
		return false;
	}
	
	@Override
	protected int calculateRoundNumber() {
		// Combine the two
		return combineRounds(super.calculateRoundNumber(), 
							 typeMatcher.calculateRoundNumber());
	}
}
