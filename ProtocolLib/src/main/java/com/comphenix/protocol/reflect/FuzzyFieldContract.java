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
	private ClassMatcher typeMatcher = ClassMatcher.MATCH_ALL;
	
	public static class Builder extends AbstractFuzzyMember.Builder<FuzzyFieldContract> {
		public Builder requireModifier(int modifier) {
			super.requireModifier(modifier); 
			return this;
		}
		
		public Builder banModifier(int modifier) {
			super.banModifier(modifier); 
			return this;
		}
		
		public Builder nameRegex(String regex) {
			super.nameRegex(regex); 
			return this;
		}
		
		public Builder nameRegex(Pattern pattern) {
			super.nameRegex(pattern); 
			return this;
		}
		
		public Builder nameExact(String name) {
			super.nameExact(name); 
			return this;
		}
		
		public Builder declaringClassExactType(Class<?> declaringClass) {
			super.declaringClassExactType(declaringClass); 
			return this;
		}
		
		public Builder declaringClassCanHold(Class<?> declaringClass) {
			super.declaringClassCanHold(declaringClass); 
			return this;
		}
		
		@Override
		@Nonnull
		protected FuzzyFieldContract initialMember() {
			return new FuzzyFieldContract();
		}
		
		public Builder typeExact(Class<?> type) {
			member.typeMatcher = new ClassMatcher(type, false);
			return this;
		}
		
		public Builder typeCanHold(Class<?> type) {
			member.typeMatcher = new ClassMatcher(type, true);
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
	public boolean isMatch(Field value) {
		if (super.isMatch(value)) {
			return typeMatcher.isClassEqual(value.getType());
		}
		// No match
		return false;
	}
	
	@Override
	protected int calculateRoundNumber() {
		int current = -typeMatcher.getClassNumber();
		
		return combineRounds(super.calculateRoundNumber(), current);
	}
}
