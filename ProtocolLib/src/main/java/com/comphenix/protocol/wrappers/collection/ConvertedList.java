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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents a list that wraps another list by transforming the items going in and out.
 * 
 * @author Kristian
 *
 * @param <VInner> - type of the items in the inner invisible list.
 * @param <VOuter> - type of the items publically accessible in the outer list.
 */
public abstract class ConvertedList<VInner, VOuter> extends ConvertedCollection<VInner, VOuter> implements List<VOuter> {
	private List<VInner> inner;
	
	public ConvertedList(List<VInner> inner) {
		super(inner);
		this.inner = inner;
	}

	@Override
	public void add(int index, VOuter element) {
		inner.add(index, toInner(element));
	}

	@Override
	public boolean addAll(int index, Collection<? extends VOuter> c) {
		return inner.addAll(index, getInnerCollection(c));
	}

	@Override
	public VOuter get(int index) {
		return toOuter(inner.get(index));
	}

	@Override
	@SuppressWarnings("unchecked")
	public int indexOf(Object o) {
		return inner.indexOf(toInner((VOuter) o));
	}

	@Override
	@SuppressWarnings("unchecked")
	public int lastIndexOf(Object o) {
		return inner.lastIndexOf(toInner((VOuter) o));
	}

	@Override
	public ListIterator<VOuter> listIterator() {
		return listIterator(0);
	}

	@Override
	public ListIterator<VOuter> listIterator(int index) {
		final ListIterator<VInner> innerIterator = inner.listIterator(index);
		
		return new ListIterator<VOuter>() {
			@Override
			public void add(VOuter e) {
				innerIterator.add(toInner(e));
			}
			
			@Override
			public boolean hasNext() {
				return innerIterator.hasNext();
			}
			
			@Override
			public boolean hasPrevious() {
				return innerIterator.hasPrevious();
			}
			
			@Override
			public VOuter next() {
				return toOuter(innerIterator.next());
			}
			
			@Override
			public int nextIndex() {
				return innerIterator.nextIndex();
			}
			
			@Override
			public VOuter previous() {
				return toOuter(innerIterator.previous());
			}
			
			@Override
			public int previousIndex() {
				return innerIterator.previousIndex();
			}
			
			@Override
			public void remove() {
				innerIterator.remove();
			}
			
			@Override
			public void set(VOuter e) {
				innerIterator.set(toInner(e));
			}
		};
	}

	@Override
	public VOuter remove(int index) {
		return toOuter(inner.remove(index));
	}

	@Override
	public VOuter set(int index, VOuter element) {
		return toOuter(inner.set(index, toInner(element)));
	}

	@Override
	public List<VOuter> subList(int fromIndex, int toIndex) {
		return new ConvertedList<VInner, VOuter>(inner.subList(fromIndex, toIndex)) {
			@Override
			protected VInner toInner(VOuter outer) {
				return ConvertedList.this.toInner(outer);
			}
			
			@Override
			protected VOuter toOuter(VInner inner) {
				return ConvertedList.this.toOuter(inner);
			}
		};
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private ConvertedCollection<VOuter, VInner> getInnerCollection(Collection c) {
		return new ConvertedCollection<VOuter, VInner>(c) {
			@Override
			protected VOuter toInner(VInner outer) {
				return ConvertedList.this.toOuter(outer);
			}
			
			@Override
			protected VInner toOuter(VOuter inner) {
				return ConvertedList.this.toInner(inner);
			}
		};
	}
}
