package com.comphenix.protocol.reflect.fuzzy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.MethodInfo;
import com.google.common.collect.ImmutableList;

/**
 * Determine if a given class implements a given fuzzy (duck typed) contract.
 *
 * @author Kristian
 */
public class FuzzyClassContract implements AbstractFuzzyMatcher<Class<?>> {

	private final ImmutableList<AbstractFuzzyMatcher<Field>> fieldContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> methodContracts;
	private final ImmutableList<AbstractFuzzyMatcher<MethodInfo>> constructorContracts;

	private final ImmutableList<AbstractFuzzyMatcher<Class<?>>> baseclassContracts;
	private final ImmutableList<AbstractFuzzyMatcher<Class<?>>> interfaceContracts;

	/**
	 * Constructs a new fuzzy class contract with the given contracts.
	 *
	 * @param builder - the builder that is constructing us.
	 */
	private FuzzyClassContract(Builder builder) {
		this.fieldContracts = ImmutableList.copyOf(builder.fieldContracts);
		this.methodContracts = ImmutableList.copyOf(builder.methodContracts);
		this.constructorContracts = ImmutableList.copyOf(builder.constructorContracts);
		this.baseclassContracts = ImmutableList.copyOf(builder.baseclassContracts);
		this.interfaceContracts = ImmutableList.copyOf(builder.interfaceContracts);
	}

	/**
	 * Construct a new fuzzy class contract builder.
	 *
	 * @return A new builder.
	 */
	public static Builder newBuilder() {
		return new Builder();
	}

	/**
	 * Retrieve an immutable list of every field contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 *
	 * @return List of every field contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Field>> getFieldContracts() {
		return this.fieldContracts;
	}

	/**
	 * Retrieve an immutable list of every method contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 *
	 * @return List of every method contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getMethodContracts() {
		return this.methodContracts;
	}

	/**
	 * Retrieve an immutable list of every constructor contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 *
	 * @return List of every constructor contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<MethodInfo>> getConstructorContracts() {
		return this.constructorContracts;
	}

	/**
	 * Retrieve an immutable list of every baseclass contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 *
	 * @return List of every baseclass contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getBaseclassContracts() {
		return this.baseclassContracts;
	}

	/**
	 * Retrieve an immutable list of every interface contract.
	 * <p>
	 * This list is ordered in descending order of priority.
	 *
	 * @return List of every interface contract.
	 */
	public ImmutableList<AbstractFuzzyMatcher<Class<?>>> getInterfaceContracts() {
		return this.interfaceContracts;
	}

	@Override
	public boolean isMatch(Class<?> value, Object parent) {
		FuzzyReflection reflection = FuzzyReflection.fromClass(value, true);

		// Make sure all the contracts are valid
		return this.processValue(value.getSuperclass(), parent, this.baseclassContracts)
				&& this.processContracts(Arrays.asList(value.getInterfaces()), parent, this.interfaceContracts)
				&& this.processContracts(reflection.getFields(), value, this.fieldContracts)
				&& this.processContracts(MethodInfo.fromMethods(reflection.getMethods()), value, this.methodContracts)
				&& this.processContracts(MethodInfo.fromConstructors(value.getDeclaredConstructors()), value,
				this.constructorContracts);
	}

	private <T> boolean processContracts(Collection<T> values, Object parent, List<AbstractFuzzyMatcher<T>> matchers) {
		// they all match if no values are present
		if (values.isEmpty() || matchers.isEmpty()) {
			return true;
		}

		// check if all contracts match the given objects
		int acceptingMatchers = 0;
		for (T value : values) {
			if (this.processValue(value, parent, matchers)) {
				acceptingMatchers++;
				// if all matchers found one match we're done
				if (acceptingMatchers == matchers.size()) {
					return true;
				}
			}
		}

		return false;
	}

	private <T> boolean processValue(T value, Object parent, List<AbstractFuzzyMatcher<T>> matchers) {
		// check if all given contracts match the given value
		for (AbstractFuzzyMatcher<T> matcher : matchers) {
			if (!matcher.isMatch(value, parent)) {
				return false;
			}
		}

		// they all match
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("FuzzyClassContract={\n");

		// append all subcontracts
		if (!this.fieldContracts.isEmpty()) {
			builder.append("  fields=").append(this.fieldContracts).append("\n");
		}

		if (!this.methodContracts.isEmpty()) {
			builder.append("  methods=").append(this.methodContracts).append("\n");
		}

		if (!this.constructorContracts.isEmpty()) {
			builder.append("  constructors=").append(this.constructorContracts).append("\n");
		}

		if (!this.baseclassContracts.isEmpty()) {
			builder.append("  baseClasses=").append(this.baseclassContracts).append("\n");
		}

		if (!this.interfaceContracts.isEmpty()) {
			builder.append("  interfaceClasses=").append(this.interfaceContracts).append("\n");
		}

		// finish off
		return builder.append("}").toString();
	}

	/**
	 * Represents a class contract builder.
	 *
	 * @author Kristian
	 */
	public static final class Builder {

		private final List<AbstractFuzzyMatcher<Field>> fieldContracts = new ArrayList<>();
		private final List<AbstractFuzzyMatcher<MethodInfo>> methodContracts = new ArrayList<>();
		private final List<AbstractFuzzyMatcher<MethodInfo>> constructorContracts = new ArrayList<>();

		private final List<AbstractFuzzyMatcher<Class<?>>> baseclassContracts = new ArrayList<>();
		private final List<AbstractFuzzyMatcher<Class<?>>> interfaceContracts = new ArrayList<>();

		/**
		 * Add a new field contract.
		 *
		 * @param matcher - new field contract.
		 * @return This builder, for chaining.
		 */
		public Builder field(AbstractFuzzyMatcher<Field> matcher) {
			this.fieldContracts.add(matcher);
			return this;
		}

		/**
		 * Add a new field contract via a builder.
		 *
		 * @param builder - builder for the new field contract.
		 * @return This builder, for chaining.
		 */
		public Builder field(FuzzyFieldContract.Builder builder) {
			return this.field(builder.build());
		}

		/**
		 * Add a new method contract.
		 *
		 * @param matcher - new method contract.
		 * @return This builder, for chaining.
		 */
		public Builder method(AbstractFuzzyMatcher<MethodInfo> matcher) {
			this.methodContracts.add(matcher);
			return this;
		}

		/**
		 * Add a new method contract via a builder.
		 *
		 * @param builder - builder for the new method contract.
		 * @return This builder, for chaining.
		 */
		public Builder method(FuzzyMethodContract.Builder builder) {
			return this.method(builder.build());
		}

		/**
		 * Add a new constructor contract.
		 *
		 * @param matcher - new constructor contract.
		 * @return This builder, for chaining.
		 */
		public Builder constructor(AbstractFuzzyMatcher<MethodInfo> matcher) {
			this.constructorContracts.add(matcher);
			return this;
		}

		/**
		 * Add a new constructor contract via a builder.
		 *
		 * @param builder - builder for the new constructor contract.
		 * @return This builder, for chaining.
		 */
		public Builder constructor(FuzzyMethodContract.Builder builder) {
			return this.constructor(builder.build());
		}

		/**
		 * Add a new base class contract.
		 *
		 * @param matcher - new base class contract.
		 * @return This builder, for chaining.
		 */
		public Builder baseclass(AbstractFuzzyMatcher<Class<?>> matcher) {
			this.baseclassContracts.add(matcher);
			return this;
		}

		/**
		 * Add a new base class contract.
		 *
		 * @param builder - builder for the new base class contract.
		 * @return This builder, for chaining.
		 */
		public Builder baseclass(FuzzyClassContract.Builder builder) {
			return this.baseclass(builder.build());
		}

		/**
		 * Add a new interface contract.
		 *
		 * @param matcher - new interface contract.
		 * @return This builder, for chaining.
		 */
		public Builder interfaces(AbstractFuzzyMatcher<Class<?>> matcher) {
			this.interfaceContracts.add(matcher);
			return this;
		}

		/**
		 * Add a new interface contract.
		 *
		 * @param builder - builder for the new interface contract.
		 * @return This builder, for chaining.
		 */
		public Builder interfaces(FuzzyClassContract.Builder builder) {
			return this.interfaces(builder.build());
		}

		public FuzzyClassContract build() {
			// Construct a new class matcher
			return new FuzzyClassContract(this);
		}
	}
}
