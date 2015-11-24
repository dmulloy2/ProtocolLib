package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Determine if a given class implements a given fuzzy (duck typed) contract.
 * 
 * @author Kristian
 */
public class FuzzyClassContract extends AbstractFuzzyMatcher<Class<?>> {
	private final ImmutableList<AbstractFuzzyMatcher<Field>> fieldContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> methodContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> constructorContracts;
	
	private final ImmutableList<AbstractFuzzyMatcher<Class<?>>> baseclassContracts;
	private final ImmutableList<AbstractFuzzyMatcher<Class<?>>> interfaceContracts;
	
	/**
	 * Represents a class contract builder.
	 * @author Kristian
	 *
	 */
	public static class Builder {
		private List<AbstractFuzzyMatcher<Field>> fieldContracts = Lists.newArrayList();
		private List<AbstractFuzzyMatcher<MethodInfo>> methodContracts = Lists.newArrayList();
		private List<AbstractFuzzyMatcher<MethodInfo>> constructorContracts = Lists.newArrayList();
		
		private List<AbstractFuzzyMatcher<Class<?>>> baseclassContracts = Lists.newArrayList();
		private List<AbstractFuzzyMatcher<Class<?>>> interfaceContracts = Lists.newArrayList();
		
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
		
		/**
		 * Add a new base class contract.
		 * @param matcher - new base class contract.
		 * @return This builder, for chaining.
		 */
		public Builder baseclass(AbstractFuzzyMatcher<Class<?>> matcher) {
			baseclassContracts.add(matcher);
			return this;
		}
		
		/**
		 * Add a new base class contract.
		 * @param builder - builder for the new base class contract.
		 * @return This builder, for chaining.
		 */
		public Builder baseclass(FuzzyClassContract.Builder builder) {
			return baseclass(builder.build());
		}
		
		/**
		 * Add a new interface contract.
		 * @param matcher - new interface contract.
		 * @return This builder, for chaining.
		 */
		public Builder interfaces(AbstractFuzzyMatcher<Class<?>> matcher) {
			interfaceContracts.add(matcher);
			return this;
		}
		
		/**
		 * Add a new interface contract.
		 * @param builder - builder for the new interface contract.
		 * @return This builder, for chaining.
		 */
		public Builder interfaces(FuzzyClassContract.Builder builder) {
			return interfaces(builder.build());
		}

		public FuzzyClassContract build() {
			Collections.sort(fieldContracts);
			Collections.sort(methodContracts);
			Collections.sort(constructorContracts);
			Collections.sort(baseclassContracts);
			Collections.sort(interfaceContracts);
			
			// Construct a new class matcher
			return new FuzzyClassContract(this);
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
	 * @param builder - the builder that is constructing us.
	 */
	private FuzzyClassContract(Builder builder) {
		super();
		this.fieldContracts = ImmutableList.copyOf(builder.fieldContracts);
		this.methodContracts = ImmutableList.copyOf(builder.methodContracts);
		this.constructorContracts = ImmutableList.copyOf(builder.constructorContracts);
		this.baseclassContracts = ImmutableList.copyOf(builder.baseclassContracts);
		this.interfaceContracts = ImmutableList.copyOf(builder.interfaceContracts);
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
	
	/**
	 * Retrieve an immutable list of every baseclass contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 * @return List of every baseclass contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getBaseclassContracts() {
		return baseclassContracts;
	}
	
	/**
	 * Retrieve an immutable list of every interface contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 * @return List of every interface contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getInterfaceContracts() {
		return interfaceContracts;
	}
	
	@Override
	protected int calculateRoundNumber() {
		// Find the highest round number
		return combineRounds(findHighestRound(fieldContracts), 
			    		     findHighestRound(methodContracts), 
					    	 findHighestRound(constructorContracts),
					    	 findHighestRound(interfaceContracts),
					    	 findHighestRound(baseclassContracts));
	}
	
	private <T> int findHighestRound(Collection<AbstractFuzzyMatcher<T>> list) {
		int highest = 0;
		
		// Go through all the elements
		for (AbstractFuzzyMatcher<T> matcher : list) 
			highest = combineRounds(highest, matcher.getRoundNumber());
		return highest;
	}

	@Override
	public boolean isMatch(Class<?> value, Object parent) {
		FuzzyReflection reflection = FuzzyReflection.fromClass(value, true);
		
		// Make sure all the contracts are valid
		return (fieldContracts.size() == 0 || 
					processContracts(reflection.getFields(), value, fieldContracts)) &&
			   (methodContracts.size() == 0 ||
			   	 	processContracts(MethodInfo.fromMethods(reflection.getMethods()), value, methodContracts)) &&
			   (constructorContracts.size() == 0 || 
			   		processContracts(MethodInfo.fromConstructors(value.getDeclaredConstructors()), value, constructorContracts)) &&
			   (baseclassContracts.size() == 0 || 
			   		processValue(value.getSuperclass(), parent, baseclassContracts)) &&
			   (interfaceContracts.size() == 0 ||
			    	processContracts(Arrays.asList(value.getInterfaces()), parent, interfaceContracts));
	}

	private <T> boolean processContracts(Collection<T> values, Object parent, List<AbstractFuzzyMatcher<T>> matchers) {
		boolean[] accepted = new boolean[matchers.size()];
		int count = accepted.length;

		// Process every value in turn
		for (T value : values) {
			int index = processValue(value, parent, accepted, matchers);
			
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
	
	private <T> boolean processValue(T value, Object parent, List<AbstractFuzzyMatcher<T>> matchers) {
		for (int i = 0; i < matchers.size(); i++) {
			if (matchers.get(i).isMatch(value, parent)) {
				return true;
			}
		}
		
		// No match
		return false;
	}
	
	private <T> int processValue(T value, Object parent, boolean accepted[], List<AbstractFuzzyMatcher<T>> matchers) {
		// The order matters
		for (int i = 0; i < matchers.size(); i++) {
			if (!accepted[i]) {
				AbstractFuzzyMatcher<T> matcher = matchers.get(i);
				
				// Mark this as detected
				if (matcher.isMatch(value, parent)) {
					return i;
				}
			}
		}
		
		// Failure
		return -1;
	}
	
	@Override
	public String toString() {
		Map<String, Object> params = Maps.newLinkedHashMap();
		
		if (fieldContracts.size() > 0) {
			params.put("fields", fieldContracts);
		}
		if (methodContracts.size() > 0) {
			params.put("methods", methodContracts);
		}
		if (constructorContracts.size() > 0) {
			params.put("constructors", constructorContracts);
		}
		if (baseclassContracts.size() > 0) {
			params.put("baseclasses", baseclassContracts);
		}
		if (interfaceContracts.size() > 0) {
			params.put("interfaces", interfaceContracts);
		}
		return "{\n  " + Joiner.on(", \n  ").join(params.entrySet()) + "\n}";
	}
}
