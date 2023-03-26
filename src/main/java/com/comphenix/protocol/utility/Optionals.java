package com.comphenix.protocol.utility;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility methods for operating with Optionals
 */
public final class Optionals {

	/**
	 * Chains two optionals together by returning the secondary
	 * optional if the primary does not contain a value
	 * @param primary Primary optional
	 * @param secondary Supplier of secondary optional
	 * @return The resulting optional
	 * @param <T> Type
	 */
	public static <T> Optional<T> or(Optional<T> primary, Supplier<Optional<T>> secondary) {
		return primary.isPresent() ? primary : secondary.get();
	}

	/**
	 * Evaluates the provided predicate against the optional only if it is present
	 * @param optional Optional
	 * @param predicate Test to run against potential value
	 * @return True if the optional is present and the predicate passes
	 * @param <T> Type
	 */
	public static <T> boolean TestIfPresent(Optional<T> optional, Predicate<T> predicate) {
		return optional.isPresent() && predicate.test(optional.get());
	}

	/**
	 * Check if the optional has a value and its value equals the provided value
	 * @param optional Optional
	 * @param contents Contents to test for
	 * @return True if the optional has a value and that value equals the parameter
	 * @param <T> Type
	 */
	public static <T> boolean Equals(Optional<T> optional, Class<?> contents) {
		return optional.isPresent() && contents.equals(optional.get());
	}
}
