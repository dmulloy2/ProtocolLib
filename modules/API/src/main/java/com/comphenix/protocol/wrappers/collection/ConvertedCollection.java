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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Represents a collection that wraps another collection by transforming the elements going in and out.
 * 
 * @author Kristian
 *
 * @param <VInner> - type of the element in the inner invisible collection.
 * @param <VOuter> - type of the elements publically accessible in the outer collection.
 */
public abstract class ConvertedCollection<VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Collection<VOuter> {
	// Inner collection
	private Collection<VInner> inner;

	public ConvertedCollection(Collection<VInner> inner) {
		this.inner = inner;
	}

	@Override
	public boolean add(VOuter e) {
		return inner.add(toInner(e));
	}

	@Override
	public boolean addAll(Collection<? extends VOuter> c) {
		boolean modified = false;
		
		for (VOuter outer : c)
			modified |= add(outer);
		return modified;
	}

	@Override
	public void clear() {
		inner.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean contains(Object o) {
		return inner.contains(toInner((VOuter) o));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object outer : c) {
			if (!contains(outer))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return inner.isEmpty();
	}

	@Override
	public Iterator<VOuter> iterator() {
		return Iterators.transform(inner.iterator(), getOuterConverter());
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object o) {
		return inner.remove(toInner((VOuter) o));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean modified = false;
		
		for (Object outer : c)
			modified |= remove(outer);
		return modified;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean retainAll(Collection<?> c) {
		List<VInner> innerCopy = Lists.newArrayList();
		
		// Convert all the elements
		for (Object outer : c)
			innerCopy.add(toInner((VOuter) outer));
		return inner.retainAll(innerCopy);
	}

	@Override
	public int size() {
		return inner.size();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object[] toArray() {
		Object[] array = inner.toArray();
		
		for (int i = 0; i < array.length; i++)
			array[i] = toOuter((VInner) array[i]);
		return array;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		T[] array = a;
		int index = 0;
		
		if (array.length < size()) {
			array = (T[]) Array.newInstance(a.getClass().getComponentType(), size());
		}
		
		// Build the output array
		for (VInner innerValue : inner) 
			array[index++] = (T) toOuter(innerValue);
		return array;
	}
}
