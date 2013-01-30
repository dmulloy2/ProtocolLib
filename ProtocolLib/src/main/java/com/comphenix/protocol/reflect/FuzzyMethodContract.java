package com.comphenix.protocol.reflect;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.Lists;

/**
 * Represents a contract for matching methods or constructors.
 * 
 * @author Kristian
 */
public class FuzzyMethodContract extends AbstractFuzzyMember<MethodInfo> {
	private static class ParameterClassMatcher extends AbstractFuzzyMatcher<Class<?>[]> {
		/**
		 * The expected index.
		 */
		private final ClassMatcher typeMatcher;
		private final Integer indexMatch;
		
		/**
		 * Construct a new parameter class matcher. 
		 * @param typeMatcher - class type matcher.
		 */
		public ParameterClassMatcher(@Nonnull ClassMatcher typeMatcher) {
			this(typeMatcher, null);
		}
		
		/**
		 * Construct a new parameter class matcher. 
		 * @param typeMatcher - class type matcher.
		 * @param indexMatch - parameter index to match, or NULL for anything.
		 */
		public ParameterClassMatcher(@Nonnull ClassMatcher typeMatcher, Integer indexMatch) {
			if (typeMatcher == null)
				throw new IllegalArgumentException("Type matcher cannot be NULL.");
			
			this.typeMatcher = typeMatcher;
			this.indexMatch = indexMatch;
		}
		
		/**
		 * See if there's a match for this matcher.
		 * @param used - parameters that have been matched before.
		 * @param params - the type of each parameter.
		 * @return TRUE if this matcher matches any of the given parameters, FALSE otherwise.
		 */
		public boolean isParameterMatch(Class<?> param, int index) {
			// Make sure the index is valid (or NULL)
			if (indexMatch == null || indexMatch == index)
				return typeMatcher.isClassEqual(param);
			else
				return false;
		}

		@Override
		public boolean isMatch(Class<?>[] value) {
			throw new NotImplementedException("Use the parameter match instead.");
		}

		@Override
		protected int calculateRoundNumber() {
			return -typeMatcher.getClassNumber();
		}
		
		@Override
		public String toString() {
			return String.format("{Type: %s, Index: %s}", typeMatcher, indexMatch);
		}
	}
	
	// Match return value
	private ClassMatcher returnMatcher = ClassMatcher.MATCH_ALL;
	
	// Handle parameters and exceptions
	private List<ParameterClassMatcher> paramMatchers = Lists.newArrayList();
	private List<ParameterClassMatcher> exceptionMatchers = Lists.newArrayList();
	
	// Expected parameter count
	private Integer paramCount;
	
	public static class Builder extends AbstractFuzzyMember.Builder<FuzzyMethodContract> {
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
		
		public Builder parameterExactType(Class<?> type) {
			member.paramMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, false)));
			return this;
		}
		
		public Builder parameterCanHold(Class<?> type) {
			member.paramMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, true)));
			return this;
		}
		
		public Builder parameterExactType(Class<?> type, int index) {
			member.paramMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, false), index));
			return this;
		}
		
		public Builder parameterCanHold(Class<?> type, int index) {
			member.paramMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, true), index));
			return this;
		}
		
		public Builder parameterCount(int expectedCount) {
			member.paramCount = expectedCount;
			return this;
		}
		
		public Builder returnTypeVoid() {
			return returnTypeExact(Void.TYPE);
		}
		
		public Builder returnTypeExact(Class<?> type) {
			member.returnMatcher = new ClassMatcher(type, false);
			return this;
		}
		
		public Builder returnCanHold(Class<?> type) {
			member.returnMatcher = new ClassMatcher(type, true);
			return this;
		}
		
		public Builder exceptionExactType(Class<?> type) {
			member.exceptionMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, false)));
			return this;
		}
		
		public Builder exceptionCanHold(Class<?> type) {
			member.exceptionMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, true)));
			return this;
		}
		
		public Builder exceptionExactType(Class<?> type, int index) {
			member.exceptionMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, false), index));
			return this;
		}
		
		public Builder exceptionCanHold(Class<?> type, int index) {
			member.exceptionMatchers.add(new ParameterClassMatcher(new ClassMatcher(type, true), index));
			return this;
		}
		
		@Override
		@Nonnull
		protected FuzzyMethodContract initialMember() {
			return new FuzzyMethodContract();
		}

		@Override
		public FuzzyMethodContract build() {
			member.prepareBuild();
			return new FuzzyMethodContract(member);
		}
		
	}
	
	public static Builder newBuilder() {
		return new Builder();
	}
	
	private FuzzyMethodContract() {
		// Only allow construction from the builder
	}

	private FuzzyMethodContract(FuzzyMethodContract other) {
		super(other);
		this.returnMatcher = other.returnMatcher;
		this.paramMatchers = other.paramMatchers;
		this.exceptionMatchers = other.exceptionMatchers;
		this.paramCount = other.paramCount;
	}
	
	@Override
	protected void prepareBuild() {
		super.prepareBuild();
		
		// Sort lists such that more specific tests are up front
		Collections.sort(paramMatchers);
		Collections.sort(exceptionMatchers);
	}
	
	@Override
	public boolean isMatch(MethodInfo value) {
		if (super.isMatch(value)) {
			Class<?>[] params = value.getParameterTypes();
			Class<?>[] exceptions = value.getExceptionTypes();
			
			if (!returnMatcher.isClassEqual(value.getReturnType()))
				return false;
			if (paramCount != null && paramCount != value.getParameterTypes().length)
				return false;

			// Finally, check parameters and exceptions
			return matchParameters(params, paramMatchers) && 
				   matchParameters(exceptions, exceptionMatchers);
		}
		// No match
		return false;
	}
	
	private boolean matchParameters(Class<?>[] types, List<ParameterClassMatcher> matchers) {
		boolean[] accepted = new boolean[matchers.size()];
		int count = accepted.length;
		
		// Process every parameter in turn
		for (int i = 0; i < types.length; i++) {
			int matcherIndex = processValue(types[i], i, accepted, matchers);

			if (matcherIndex >= 0) {
				accepted[matcherIndex] = true;
				count--;
			}
			
			// Break early
			if (count == 0)
				return true;
		}
		return count == 0;
	}
	
	private int processValue(Class<?> value, int index, boolean accepted[], List<ParameterClassMatcher> matchers) {
		// The order matters
		for (int i = 0; i < matchers.size(); i++) {
			if (!accepted[i]) {
				// See if we got jackpot
				if (matchers.get(i).isParameterMatch(value, index)) {
					return i;
				}
			}
		}
		
		// Failure
		return -1;
	}
	
	@Override
	protected int calculateRoundNumber() {
		int current = 0;
		
		// Consider the return value first
		current = -returnMatcher.getClassNumber();
		
		// Handle parameters
		for (ParameterClassMatcher matcher : paramMatchers) {
			current = combineRounds(current, matcher.calculateRoundNumber());
		}
		// And exceptions
		for (ParameterClassMatcher matcher : exceptionMatchers) {
			current = combineRounds(current, matcher.calculateRoundNumber());
		}
		
		return combineRounds(super.calculateRoundNumber(), current);
	}
}
