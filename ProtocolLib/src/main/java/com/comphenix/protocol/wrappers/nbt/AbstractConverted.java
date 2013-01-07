package com.comphenix.protocol.wrappers.nbt;

import javax.annotation.Nullable;

import com.google.common.base.Function;

abstract class AbstractConverted<VInner, VOuter> {
	/**
	 * Convert a value from the inner map to the outer visible map.
	 * @param inner - the inner value.
	 * @return The outer value.
	 */
	protected abstract VOuter toOuter(VInner inner);
	
	/**
	 * Convert a value from the outer map to the internal inner map.
	 * @param outer - the outer value.
	 * @return The inner value.
	 */
	protected abstract VInner toInner(VOuter outer);
	
	/**
	 * Retrieve a function delegate that converts inner objects to outer objects.
	 * @return A function delegate.
	 */
	protected Function<VInner, VOuter> getOuterConverter() {
		return new Function<VInner, VOuter>() {
			@Override
			public VOuter apply(@Nullable VInner param) {
				return toOuter(param);
			}
		};
	}
}
