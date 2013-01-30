package com.comphenix.protocol.reflect;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Determine if a given class implements a given fuzzy (duck typed) contract.
 * 
 * @author Kristian
 */
public class FuzzyClassContract extends AbstractFuzzyMatcher<Class<?>> {
	private final ImmutableList<AbstractFuzzyMatcher<Field>> fieldContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> methodContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> constructorContracts;
	
	/**
	 * Represents a class contract builder.
	 * @author Kristian
	 *
	 */
	public static class Builder {
		private List<AbstractFuzzyMatcher<Field>> fieldContracts = Lists.newArrayList();
		private List<AbstractFuzzyMatcher<MethodInfo>> methodContracts = Lists.newArrayList();
		private List<AbstractFuzzyMatcher<MethodInfo>> constructorContracts = Lists.newArrayList();
		
		/**
		 * Add a new field contract.
		 * @param matcher - new field contract.
		 * @return This builder, for chaining.
		 */
		public Builder field(AbstractFuzzyMatcher<Field> matcher) {
			fieldContracts.add(matcher);
			return this;
		}
		
		/**
		 * Add a new field contract via a builder.
		 * @param builder - builder for the new field contract.
		 * @return This builder, for chaining.
		 */
		public Builder field(FuzzyFieldContract.Builder builder) {
			return field(builder.build());
		}
		
		/**
		 * Add a new method contract.
		 * @param matcher - new method contract.
		 * @return This builder, for chaining.
		 */
		public Builder method(AbstractFuzzyMatcher<MethodInfo> matcher) {
			methodContracts.add(matcher);
			return this;
		}
		
		/**
		 * Add a new method contract via a builder.
		 * @param builder - builder for the new method contract.
		 * @return This builder, for chaining.
		 */
		public Builder method(FuzzyMethodContract.Builder builder) {
			return method(builder.build());
		}
		
		/**
		 * Add a new constructor contract.
		 * @param matcher - new constructor contract.
		 * @return This builder, for chaining.
		 */
		public Builder constructor(AbstractFuzzyMatcher<MethodInfo> matcher) {
			constructorContracts.add(matcher);
			return this;
		}
		
		/**
		 * Add a new constructor contract via a builder.
		 * @param builder - builder for the new constructor contract.
		 * @return This builder, for chaining.
		 */
		public Builder constructor(FuzzyMethodContract.Builder builder) {
			return constructor(builder.build());
		}

		public FuzzyClassContract build() {
			Collections.sort(fieldContracts);
			Collections.sort(methodContracts);
			Collections.sort(constructorContracts);
			
			// Construct a new class matcher
			return new FuzzyClassContract(
						ImmutableList.copyOf(fieldContracts),
						ImmutableList.copyOf(methodContracts),
						ImmutableList.copyOf(constructorContracts)
			);
		}
	}

	/**
	 * Construct a new fuzzy class contract builder.
	 * @return A new builder.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}
	
	/**
	 * Constructs a new fuzzy class contract with the given contracts.
	 * @param fieldContracts - field contracts.
	 * @param methodContracts - method contracts.
	 * @param constructorContracts - constructor contracts.
	 */
	private FuzzyClassContract(ImmutableList<AbstractFuzzyMatcher<Field>> fieldContracts,
							   ImmutableList<AbstractFuzzyMatcher<MethodInfo>> methodContracts,
							   ImmutableList<AbstractFuzzyMatcher<MethodInfo>> constructorContracts) {
		super();
		this.fieldContracts = fieldContracts;
		this.methodContracts = methodContracts;
		this.constructorContracts = constructorContracts;
	}

	/**
	 * Retrieve an immutable list of every field contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 * @return List of every field contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Field>> getFieldContracts() {
		return fieldContracts;
	}

	/**
	 * Retrieve an immutable list of every method contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 * @return List of every method contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getMethodContracts() {
		return methodContracts;
	}

	/**
	 * Retrieve an immutable list of every constructor contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 * @return List of every constructor contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getConstructorContracts() {
		return constructorContracts;
	}
	
	@Override
	protected int calculateRoundNumber() {
		// Find the highest round number
		return combineRounds(findHighestRound(fieldContracts), 
			   combineRounds(findHighestRound(methodContracts), 
					    	 findHighestRound(constructorContracts)));
	}
	
	private <T> int findHighestRound(Collection<AbstractFuzzyMatcher<T>> list) {
		int highest = 0;
		
		// Go through all the elements
		for (AbstractFuzzyMatcher<T> matcher : list) 
			highest = combineRounds(highest, matcher.getRoundNumber());
		return highest;
	}

	@Override
	public boolean isMatch(Class<?> value) {
		FuzzyReflection reflection = FuzzyReflection.fromClass(value);
		
		// Make sure all the contracts are valid
		return processContracts(reflection.getFields(), fieldContracts) &&
			   processContracts(MethodInfo.fromMethods(reflection.getMethods()), methodContracts) &&
			   processContracts(MethodInfo.fromConstructors(value.getDeclaredConstructors()), constructorContracts);
	}

	private <T> boolean processContracts(Collection<T> values, List<AbstractFuzzyMatcher<T>> matchers) {
		boolean[] accepted = new boolean[matchers.size()];
		int count = accepted.length;

		// Process every value in turn
		for (T value : values) {
			int index = processValue(value, accepted, matchers);
			
			// See if this worked
			if (index >= 0) {
				accepted[index] = true;
				count--;
			}
			
			// Break early
			if (count == 0)
				return true;
		}
		return count == 0;
	}
	
	private <T> int processValue(T value, boolean accepted[], List<AbstractFuzzyMatcher<T>> matchers) {
		// The order matters
		for (int i = 0; i < matchers.size(); i++) {
			if (!accepted[i]) {
				AbstractFuzzyMatcher<T> matcher = matchers.get(i);
				
				// Mark this as detected
				if (matcher.isMatch(value)) {
					return i;
				}
			}
		}
		
		// Failure
		return -1;
	}
}
