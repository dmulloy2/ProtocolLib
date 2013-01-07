package com.comphenix.protocol.wrappers.nbt;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

abstract class ConvertedCollection<VInner, VOuter> extends AbstractConverted<VInner, VOuter> implements Collection<VOuter> {
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
