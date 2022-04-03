package com.comphenix.protocol.reflect.fuzzy;

import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

/**
 * Represents a contract for matching methods or constructors.
 *
 * @author Kristian
 */
public class FuzzyMethodContract extends AbstractFuzzyMember<MethodInfo> {

	// Match return value
	private AbstractFuzzyMatcher<Class<?>> returnMatcher = ClassTypeMatcher.MATCH_ALL;
	// Handle parameters and exceptions
	private List<ParameterClassMatcher> paramMatchers;
	private List<ParameterClassMatcher> exceptionMatchers;
	// Expected parameter count
	private Integer paramCount;

	private FuzzyMethodContract() {
		// Only allow construction from the builder
		this.paramMatchers = new ArrayList<>();
		this.exceptionMatchers = new ArrayList<>();
	}

	private FuzzyMethodContract(FuzzyMethodContract other) {
		super(other);
		this.returnMatcher = other.returnMatcher;
		this.paramMatchers = other.paramMatchers;
		this.exceptionMatchers = other.exceptionMatchers;
		this.paramCount = other.paramCount;
	}

	/**
	 * Return a method contract builder.
	 *
	 * @return Method contract builder.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Construct a new immutable copy of the given method contract.
	 *
	 * @param other - the contract to clone.
	 * @return A immutable copy of the given contract.
	 */
	private static FuzzyMethodContract immutableCopy(FuzzyMethodContract other) {
		FuzzyMethodContract copy = new FuzzyMethodContract(other);

		// Ensure that the lists are immutable
		copy.paramMatchers = ImmutableList.copyOf(copy.paramMatchers);
		copy.exceptionMatchers = ImmutableList.copyOf(copy.exceptionMatchers);
		return copy;
	}

	/**
	 * Retrieve the class matcher for the return type.
	 *
	 * @return Class matcher for the return type.
	 */
	public AbstractFuzzyMatcher<Class<?>> getReturnMatcher() {
		return this.returnMatcher;
	}

	/**
	 * Retrieve an immutable list of every parameter matcher for this method.
	 *
	 * @return Immutable list of every parameter matcher.
	 */
	public ImmutableList<ParameterClassMatcher> getParamMatchers() {
		if (this.paramMatchers instanceof ImmutableList) {
			return (ImmutableList<ParameterClassMatcher>) this.paramMatchers;
		} else {
			throw new IllegalStateException("Lists haven't been sealed yet.");
		}
	}

	/**
	 * Retrieve an immutable list of every exception matcher for this method.
	 *
	 * @return Immutable list of every exception matcher.
	 */
	public List<ParameterClassMatcher> getExceptionMatchers() {
		if (this.exceptionMatchers instanceof ImmutableList) {
			return this.exceptionMatchers;
		} else {
			throw new IllegalStateException("Lists haven't been sealed yet.");
		}
	}

	/**
	 * Retrieve the expected parameter count for this method.
	 *
	 * @return Expected parameter count, or NULL if anyting goes.
	 */
	public Integer getParamCount() {
		return this.paramCount;
	}

	@Override
	public boolean isMatch(MethodInfo value, Object parent) {
		if (super.isMatch(value, parent)) {
			// check the return type first (the easiest check)
			if (!this.returnMatcher.isMatch(value.getReturnType(), value)) {
				return false;
			}

			// check for the parameter types
			Class<?>[] params = value.getParameterTypes();
			if (this.paramCount != null && this.paramCount != params.length) {
				return false;
			}

			// check parameters and exceptions
			return this.matchTypes(params, value, this.paramMatchers)
					&& this.matchTypes(value.getExceptionTypes(), value, this.exceptionMatchers);
		}

		// No match
		return false;
	}

	private boolean matchTypes(Class<?>[] types, MethodInfo parent, List<ParameterClassMatcher> matchers) {
		if (matchers.isEmpty()) {
			// no matchers - no show
			return true;
		}

		// the amount of matchers which are ok with the parameter types
		int acceptingMatchers = 0;
		for (int i = 0; i < types.length; i++) {
			if (this.processValue(types[i], parent, i, matchers)) {
				acceptingMatchers++;
				// if all matchers accepted one type we are done
				if (acceptingMatchers == matchers.size()) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean processValue(Class<?> value, MethodInfo parent, int index, List<ParameterClassMatcher> matchers) {
		// The order matters
		for (ParameterClassMatcher matcher : matchers) {
			// See if we got jackpot
			if (matcher.isParameterMatch(value, parent, index)) {
				return true;
			}
		}

		// Failure
		return false;
	}

	@Override
	protected Map<String, Object> getKeyValueView() {
		Map<String, Object> member = super.getKeyValueView();

		// Only add fields that are actual constraints
		if (this.returnMatcher != ClassTypeMatcher.MATCH_ALL) {
			member.put("return", this.returnMatcher);
		}

		if (!this.paramMatchers.isEmpty()) {
			member.put("params", this.paramMatchers);
		}

		if (!this.exceptionMatchers.isEmpty()) {
			member.put("exceptions", this.exceptionMatchers);
		}

		if (this.paramCount != null) {
			member.put("paramCount", this.paramCount);
		}

		return member;
	}

	private static final class ParameterClassMatcher implements AbstractFuzzyMatcher<Class<?>[]> {

		/**
		 * The expected index.
		 */
		private final AbstractFuzzyMatcher<Class<?>> typeMatcher;
		private final Integer indexMatch;

		/**
		 * Construct a new parameter class matcher.
		 *
		 * @param typeMatcher - class type matcher.
		 */
		public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher<Class<?>> typeMatcher) {
			this(typeMatcher, null);
		}

		/**
		 * Construct a new parameter class matcher.
		 *
		 * @param typeMatcher - class type matcher.
		 * @param indexMatch  - parameter index to match, or NULL for anything.
		 */
		public ParameterClassMatcher(@Nonnull AbstractFuzzyMatcher<Class<?>> typeMatcher, Integer indexMatch) {
			this.typeMatcher = typeMatcher;
			this.indexMatch = indexMatch;
		}

		/**
		 * See if there's a match for this matcher.
		 *
		 * @param param  - the type to match.
		 * @param parent - the container (member) that holds a reference to this parameter.
		 * @param index  - the index of the current parameter.
		 * @return TRUE if this matcher matches any of the given parameters, FALSE otherwise.
		 */
		public boolean isParameterMatch(Class<?> param, MethodInfo parent, int index) {
			// Make sure the index is valid (or NULL)
			if (this.indexMatch == null || this.indexMatch == index) {
				return this.typeMatcher.isMatch(param, parent);
			} else {
				return false;
			}
		}

		@Override
		public boolean isMatch(Class<?>[] value, Object parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return String.format("{ Parameter Type: %s, Index: %s }", this.typeMatcher, this.indexMatch);
		}
	}

	/**
	 * Represents a builder for a fuzzy method contract.
	 *
	 * @author Kristian
	 */
	public static final class Builder extends AbstractFuzzyMember.Builder<FuzzyMethodContract> {

		@Override
		public Builder requireModifier(int modifier) {
			super.requireModifier(modifier);
			return this;
		}

		@Override
		public Builder requirePublic() {
			super.requirePublic();
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
		 *
		 * @param type - the exact type this parameter must match.
		 * @return This builder, for chaining.
		 */
		public Builder parameterExactType(Class<?> type) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
			return this;
		}

		/**
		 * Add a new required parameter whose type must be a superclass of the given type.
		 * <p>
		 * If a method parameter is of type Number, then any derived class here (Integer, Long, etc.) will match it.
		 *
		 * @param type - a type or less derived type of the matching parameter.
		 * @return This builder, for chaining.
		 */
		public Builder parameterSuperOf(Class<?> type) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
			return this;
		}

		/**
		 * Add a new required parameter whose type must be a derived class of the given class.
		 * <p>
		 * If the method parameter has the type Integer, then the class Number here will match it.
		 *
		 * @param type - a type or more derived type of the matching parameter.
		 * @return This builder, for chaining.
		 */
		public Builder parameterDerivedOf(Class<?> type) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchDerived(type)));
			return this;
		}

		/**
		 * Add a new required parameter whose type must match the given class matcher.
		 *
		 * @param classMatcher - the class matcher.
		 * @return This builder, for chaining.
		 */
		public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			this.member.paramMatchers.add(new ParameterClassMatcher(classMatcher));
			return this;
		}

		/**
		 * Add a new required parameter by type and position for any matching method.
		 *
		 * @param type  - the exact type this parameter must match.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterExactType(Class<?> type, int index) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
			return this;
		}

		/**
		 * Add a new required parameters by type and order for any matching method.
		 *
		 * @param types - the types of every parameters in order.
		 * @return This builder, for chaining.
		 */
		public Builder parameterExactArray(Class<?>... types) {
			this.parameterCount(types.length);
			for (int i = 0; i < types.length; i++) {
				this.parameterExactType(types[i], i);
			}

			return this;
		}

		/**
		 * Add a new required parameter whose type must be a superclass of the given type.
		 * <p>
		 * If a parameter is of type Number, any derived class (Integer, Long, etc.) will match it.
		 *
		 * @param type  - a type or derived type of the matching parameter.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterSuperOf(Class<?> type, int index) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
			return this;
		}

		/**
		 * Add a new required parameter whose type must be a derived class of the given class.
		 * <p>
		 * If the method parameter has the type Integer, then the class Number here will match it.
		 *
		 * @param type  - a type or more derived type of the matching parameter.
		 * @param index - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterDerivedOf(Class<?> type, int index) {
			this.member.paramMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchDerived(type), index));
			return this;
		}

		/**
		 * Add a new required parameter whose type must match the given class matcher and index.
		 *
		 * @param classMatcher - the class matcher.
		 * @param index        - the expected position in the parameter list.
		 * @return This builder, for chaining.
		 */
		public Builder parameterMatches(AbstractFuzzyMatcher<Class<?>> classMatcher, int index) {
			this.member.paramMatchers.add(new ParameterClassMatcher(classMatcher, index));
			return this;
		}

		/**
		 * Set the expected number of parameters in the matching method.
		 *
		 * @param expectedCount - the number of parameters to expect.
		 * @return This builder, for chaining.
		 */
		public Builder parameterCount(int expectedCount) {
			this.member.paramCount = expectedCount;
			return this;
		}

		/**
		 * Require a void method.
		 *
		 * @return This builder, for chaining.
		 */
		public Builder returnTypeVoid() {
			return this.returnTypeExact(Void.TYPE);
		}

		/**
		 * Set the return type of a matching method exactly.
		 *
		 * @param type - the exact return type.
		 * @return This builder, for chaining.
		 */
		public Builder returnTypeExact(Class<?> type) {
			this.member.returnMatcher = FuzzyMatchers.matchExact(type);
			return this;
		}

		/**
		 * Set the expected super class of the return type for every matching method.
		 *
		 * @param type - the return type, or a super class of it.
		 * @return This builder, for chaining.
		 */
		public Builder returnDerivedOf(Class<?> type) {
			this.member.returnMatcher = FuzzyMatchers.matchDerived(type);
			return this;
		}

		/**
		 * Set a matcher that must match the return type of a matching method.
		 *
		 * @param classMatcher - the exact return type.
		 * @return This builder, for chaining.
		 */
		public Builder returnTypeMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			this.member.returnMatcher = classMatcher;
			return this;
		}

		/**
		 * Add a throwable exception that must match the given type exactly.
		 *
		 * @param type - exception type.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionExactType(Class<?> type) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type)));
			return this;
		}

		/**
		 * Add a throwable exception that must match the given type or be derived.
		 *
		 * @param type - exception type.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionSuperOf(Class<?> type) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type)));
			return this;
		}

		/**
		 * Add a throwable exception that must match the given matcher,
		 *
		 * @param classMatcher - the class matcher that must match.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> classMatcher) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(classMatcher));
			return this;
		}

		/**
		 * Add a throwable exception that must match the given type exactly and index.
		 *
		 * @param type  - exception type.
		 * @param index - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionExactType(Class<?> type, int index) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchExact(type), index));
			return this;
		}

		/**
		 * Add a throwable exception that must match the given type or be derived and index.
		 *
		 * @param type  - exception type.
		 * @param index - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionSuperOf(Class<?> type, int index) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(FuzzyMatchers.matchSuper(type), index));
			return this;
		}

		/**
		 * Add a throwable exception that must match the given matcher and index.
		 *
		 * @param classMatcher - the class matcher that must match.
		 * @param index        - the position in the throwable list.
		 * @return This builder, for chaining.
		 */
		public Builder exceptionMatches(AbstractFuzzyMatcher<Class<?>> classMatcher, int index) {
			this.member.exceptionMatchers.add(new ParameterClassMatcher(classMatcher, index));
			return this;
		}

		@Override
		@Nonnull
		protected FuzzyMethodContract initialMember() {
			// With mutable lists
			return new FuzzyMethodContract();
		}

		@Override
		public FuzzyMethodContract build() {
			this.member.prepareBuild();
			return immutableCopy(this.member);
		}
	}
}
