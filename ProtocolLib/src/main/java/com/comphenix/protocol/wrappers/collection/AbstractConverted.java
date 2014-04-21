/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.wrappers.collection;

import javax.annotation.Nullable;

import com.google.common.base.Function;

/**
 * Represents an object that transform elements of type VInner to type VOuter and back again.
 * 
 * @author Kristian
 *
 * @param <VInner> - the first type.
 * @param <VOuter> - the second type.
 */
public abstract class AbstractConverted<VInner, VOuter> {
	// Inner conversion
	private Function<VOuter, VInner> innerConverter = new Function<VOuter, VInner>() {
		@Override
		public VInner apply(@Nullable VOuter param) {
			return toInner(param);
		}
	};
	
	// Outer conversion
	private Function<VInner, VOuter> outerConverter = new Function<VInner, VOuter>() {
		@Override
		public VOuter apply(@Nullable VInner param) {
			return toOuter(param);
		}
	};
	
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
	 * Retrieve a function delegate that converts outer objects to inner objects.
	 * @return A function delegate.
	 */
	protected Function<VOuter, VInner> getInnerConverter() {
		return innerConverter;
	}
	
	/**
	 * Retrieve a function delegate that converts inner objects to outer objects.
	 * @return A function delegate.
	 */
	protected Function<VInner, VOuter> getOuterConverter() {
		return outerConverter;
	}
}
