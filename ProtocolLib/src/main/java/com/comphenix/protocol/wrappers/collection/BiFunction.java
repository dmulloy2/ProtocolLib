package com.comphenix.protocol.wrappers.collection;

/**
 * Represents a function that accepts two parameters.
 * @author Kristian
 * @param <T1> - type of the first parameter.
 * @param <T2> - type of the second parameter.
 * @param <TResult> - type of the return value.
 */
public interface BiFunction<T1, T2, TResult> {
	public TResult apply(T1 arg1, T2 arg2);
}