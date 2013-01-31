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
		private final AbstractFuzzyMatcher<Class<?>> typeMatcher;
		private final Integer indexMatch;
		
		/**
		 * Construct a new parameter class matcher. 
		 * @param typeMatcher - class type matcher.
		 */
		public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher<Class<?>> typeMatcher) {
			this(typeMatcher, null);
		}
		
		/**
		 * Construct a new parameter class matcher. 
		 * @param typeMatcher - class type matcher.
		 * @param indexMatch - parameter index to match, or NULL for anything.
		 */
		public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher<Class<?>> typeMatcher, Integer indexMatch) {
			if (typeMatcher == null)
				throw new IllegalArgumentException("Type matcher cannot be NULL.");
			
			this.typeMatcher = typeMatcher;
			this.indexMatch = indexMatch;
		}
		
		/**
		 * See if there's a match for this matcher.
		 * @param used - parameters that have been matched before.
		 * @param parent - the container (member) that holds a reference to this parameter. 
		 * @param params - the type of each parameter.
		 * @return TRUE if this matcher matches any of the given parameters, FALSE otherwise.
		 */
		public boolean isParameterMatch(Class<?> param, MethodInfo parent, int index) {
			// Make sure the index is valid (or NULL)
			if (indexMatch == null || indexMatch == index)
				return typeMatcher.isMatch(param, parent);
			else
				return false;
		}

		@Override
		public boolean isMatch(Class<?>[] value, Object parent) {
			throw new NotImplementedException("Use the parameter match instead.");
		}

		@Override
		protected int calculateRoundNumber() {
			return typeMatcher.getRoundNumber();
		}
		
		@Override
		public String toString() {
			return String.format("{Type: %s, Index: %s}", typeMatcher, indexMatch);
		}
	}
	
	// Match return value
	private AbstractFuzzyMatcher<Class<?>> returnMatcher = ExactClassMatcher.MATCH_ALL;
	
	// Handle parameters and exceptions
	private List<ParameterClassMatcher> paramMatchers = Lists.newArrayList();
	private List<ParameterClassMatcher> exceptionMatchers = Lists.newArrayList();
	
	// Expected parameter count
	private Integer paramCount;
	
	/**
	 * Represents a builder for a fuzzy method contract.
	 * 
	 * @author Kristian
	 */
	public static class Builder extends AbstractFuzzyMember.Builder<FuzzyMethodContract> {
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
		
		@Override
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
		
		/**
		 * Add a new required parameter by type for any matching method.
		 * @param type - the exact type this parameter must match.
		 * @return This builder, for chaining.
		 */
		public Builder parameterExactType(Class<?> type) {
			member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
			return this;
		}
		
		/**
		 * Add a new required parameter whose type must be a superclass of the given type.
		 * <p>
		 * If a parameter is of type Number, any derived class (Integer, Long, etc.) will match it.
		 * @param type - a type or derived type of the matching parameter.
		 * @return This builder, for chaining.
		 */
		public Builder parameterSuperOf(Class<?> type) {
			member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
			return this;
		}

		/**
		 * Add a new required parameter whose type must match the given class matcher.
		 * @param classMatcher - the class matcher.
		 * @return This builder, for chaining.
		 */
		public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			member.paramMatchers.add(new ParameterClassMatcher(classMatcher));
			return this;
		}
		
		/**
		 * Add a new required parameter by type and position for any matching method.
		 * @param type - the exact type this parameter must match.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterExactType(Class<?> type, int index) {
			member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
			return this;
		}
		
		/**
		 * Add a new required parameter whose type must be a superclass of the given type.
		 * <p>
		 * If a parameter is of type Number, any derived class (Integer, Long, etc.) will match it.
		 * @param type - a type or derived type of the matching parameter.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterSuperOf(Class<?> type, int index) {
			member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
			return this;
		}
		
		/**
		 * Add a new required parameter whose type must match the given class matcher and index.
		 * @param classMatcher - the class matcher.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> classMatcher, int index) {
			member.paramMatchers.add(new ParameterClassMatcher(classMatcher, index));
			return this;
		}
		
		/**
		 * Set the expected number of parameters in the matching method.
		 * @param expectedCount - the number of parameters to expect.
		 * @return This builder, for chaining.
		 */
		public Builder parameterCount(int expectedCount) {
			member.paramCount = expectedCount;
			return this;
		}
		
		/**
		 * Require a void method.
		 * @return This builder, for chaining. 
		 */
		public Builder returnTypeVoid() {
			return returnTypeExact(Void.TYPE);
		}
		
		/**
		 * Set the return type of a matching method exactly.
		 * @param type - the exact return type.
		 * @return This builder, for chaining.
		 */
		public Builder returnTypeExact(Class<?> type) {
			member.returnMatcher = FuzzyMatchers.matchExact(type);
			return this;
		}
		
		/**
		 * Set the expected super class of the return type for every matching method. 
		 * @param type - the return type, or a super class of it.
		 * @return This builder, for chaining.
		 */
		public Builder returnDerivedOf(Class<?> type) {
			member.returnMatcher =  FuzzyMatchers.matchDerived(type);
			return this;
		}
		
		/**
		 * Set a matcher that must match the return type of a matching method.
		 * @param classMatcher - the exact return type.
		 * @return This builder, for chaining.
		 */
		public Builder returnTypeMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			member.returnMatcher = classMatcher;
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given type exactly.
		 * @param type - exception type.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionExactType(Class<?> type) {
			member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given type or be derived.
		 * @param type - exception type.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionSuperOf(Class<?> type) {
			member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given matcher,
		 * @param classMatcher - the class matcher that must match.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			member.exceptionMatchers.add(new ParameterClassMatcher(classMatcher));
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given type exactly and index.
		 * @param type - exception type.
		 * @param index - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionExactType(Class<?> type, int index) {
			member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given type or be derived and index.
		 * @param type - exception type.
		 * @param index - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionSuperOf(Class<?> type, int index) {
			member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
			return this;
		}
		
		/**
		 * Add a throwable exception that must match the given matcher and index.
		 * @param classMatcher - the class matcher that must match.
		 * @param index - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> classMatcher, int index) {
			member.exceptionMatchers.add(new ParameterClassMatcher(classMatcher, index));
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
	
	/**
	 * Return a method contract builder.
	 * @return Method contract builder.
	 */
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
	public boolean isMatch(MethodInfo value, Object parent) {
		if (super.isMatch(value, parent)) {
			Class<?>[] params = value.getParameterTypes();
			Class<?>[] exceptions = value.getExceptionTypes();
			
			if (!returnMatcher.isMatch(value.getReturnType(), value))
				return false;
			if (paramCount != null && paramCount != value.getParameterTypes().length)
				return false;

			// Finally, check parameters and exceptions
			return matchParameters(params, value, paramMatchers) && 
				   matchParameters(exceptions, value, exceptionMatchers);
		}
		// No match
		return false;
	}
	
	private boolean matchParameters(Class<?>[] types, MethodInfo parent, List<ParameterClassMatcher> matchers) {
		boolean[] accepted = new boolean[matchers.size()];
		int count = accepted.length;
		
		// Process every parameter in turn
		for (int i = 0; i < types.length; i++) {
			int matcherIndex = processValue(types[i], parent, i, accepted, matchers);

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
	
	private int processValue(Class<?> value, MethodInfo parent, int index, boolean accepted[], List<ParameterClassMatcher> matchers) {
		// The order matters
		for (int i = 0; i < matchers.size(); i++) {
			if (!accepted[i]) {
				// See if we got jackpot
				if (matchers.get(i).isParameterMatch(value, parent, index)) {
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
		current = returnMatcher.getRoundNumber();
		
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
