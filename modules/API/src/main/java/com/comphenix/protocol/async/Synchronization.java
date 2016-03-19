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

package com.comphenix.protocol.async;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

/**
 * Synchronization views copied from Google Guava.
 * 
 * @author Kristian
 */
class Synchronization {

	/**
	 * Create a synchronized wrapper for the given queue.
	 * <p>
	 * This wrapper cannot synchronize the iterator(). Callers are expected 
	 * to synchronize iterators manually.
	 * @param queue - the queue to synchronize.
	 * @param mutex - synchronization mutex, or NULL to use the queue.
	 * @return A synchronization wrapper.
	 */
	public static <E> Queue<E> queue(Queue<E> queue, @Nullable Object mutex) {
		return (queue instanceof SynchronizedQueue) ? 
				queue : 
					new SynchronizedQueue<E>(queue, mutex);
	}
	
	private static class SynchronizedObject implements Serializable {
		private static final long serialVersionUID = -4408866092364554628L;

		final Object delegate;
		final Object mutex;

		SynchronizedObject(Object delegate, @Nullable Object mutex) {
			this.delegate = Preconditions.checkNotNull(delegate);
			this.mutex = (mutex == null) ? this : mutex;
		}

		Object delegate() {
			return delegate;
		}

		// No equals and hashCode; see ForwardingObject for details.

		@Override
		public String toString() {
			synchronized (mutex) {
				return delegate.toString();
			}
		}
	}

	private static class SynchronizedCollection<E> extends SynchronizedObject implements Collection<E> {
		private static final long serialVersionUID = 5440572373531285692L;

		private SynchronizedCollection(Collection<E> delegate,
				@Nullable Object mutex) {
			super(delegate, mutex);
		}

		@SuppressWarnings("unchecked")
		@Override
		Collection<E> delegate() {
			return (Collection<E>) super.delegate();
		}

		@Override
		public boolean add(E e) {
			synchronized (mutex) {
				return delegate().add(e);
			}
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			synchronized (mutex) {
				return delegate().addAll(c);
			}
		}

		@Override
		public void clear() {
			synchronized (mutex) {
				delegate().clear();
			}
		}

		@Override
		public boolean contains(Object o) {
			synchronized (mutex) {
				return delegate().contains(o);
			}
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			synchronized (mutex) {
				return delegate().containsAll(c);
			}
		}

		@Override
		public boolean isEmpty() {
			synchronized (mutex) {
				return delegate().isEmpty();
			}
		}

		@Override
		public Iterator<E> iterator() {
			return delegate().iterator(); // manually synchronized
		}

		@Override
		public boolean remove(Object o) {
			synchronized (mutex) {
				return delegate().remove(o);
			}
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			synchronized (mutex) {
				return delegate().removeAll(c);
			}
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			synchronized (mutex) {
				return delegate().retainAll(c);
			}
		}

		@Override
		public int size() {
			synchronized (mutex) {
				return delegate().size();
			}
		}

		@Override
		public Object[] toArray() {
			synchronized (mutex) {
				return delegate().toArray();
			}
		}

		@Override
		public <T> T[] toArray(T[] a) {
			synchronized (mutex) {
				return delegate().toArray(a);
			}
		}

	}

	private static class SynchronizedQueue<E> extends SynchronizedCollection<E> implements Queue<E> {
		private static final long serialVersionUID = 1961791630386791902L;

		SynchronizedQueue(Queue<E> delegate, @Nullable Object mutex) {
			super(delegate, mutex);
		}

		@Override
		Queue<E> delegate() {
			return (Queue<E>) super.delegate();
		}

		@Override
		public E element() {
			synchronized (mutex) {
				return delegate().element();
			}
		}

		@Override
		public boolean offer(E e) {
			synchronized (mutex) {
				return delegate().offer(e);
			}
		}

		@Override
		public E peek() {
			synchronized (mutex) {
				return delegate().peek();
			}
		}

		@Override
		public E poll() {
			synchronized (mutex) {
				return delegate().poll();
			}
		}

		@Override
		public E remove() {
			synchronized (mutex) {
				return delegate().remove();
			}
		}
	}
}
