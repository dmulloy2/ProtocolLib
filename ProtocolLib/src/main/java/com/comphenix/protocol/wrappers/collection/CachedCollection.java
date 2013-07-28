package com.comphenix.protocol.wrappers.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

/**
 * Represents a set that will (best effort) cache elements before using 
 * an underlying set to retrieve the actual element.
 * <p>
 * The cache will be invalidated when data is removed.
 * 
 * @author Kristian
 * @param <T> - type of each element in the collection.
 */
public class CachedCollection<T> implements Collection<T> {
	protected Set<T> delegate;
	protected Object[] cache;
	
	/**
	 * Construct a cached collection with the given delegate.
	 * <p>
	 * Objects are cached before they can be extracted from this collection.
	 * @param delegate - the delegate.
	 */
	public CachedCollection(Set<T> delegate) {
		this.delegate = Preconditions.checkNotNull(delegate, "delegate cannot be NULL.");
	}
	
	/**
	 * Construct the cache if needed.
	 */
	private void initializeCache() {
		if (cache == null) {
			cache = new Object[delegate.size()];
		}
	}
	
	/**
	 * Ensure that the cache is big enough.
	 */
	private void growCache() {
		// We'll delay making the cache
		if (cache == null)
			return;
		int newLength = cache.length;
		
		// Ensure that the cache is big enoigh
		while (newLength < delegate.size()) {
			newLength *= 2;
		}
		if (newLength != cache.length) {
			cache = Arrays.copyOf(cache, newLength);
		}
	}
	
	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		final Iterator<T> source = delegate.iterator();
		initializeCache();
		
		return new Iterator<T>() {
			int currentIndex = -1;
			int iteratorIndex = -1;
			
			@Override
			public boolean hasNext() {
				return currentIndex < delegate.size() - 1;
			}

			@SuppressWarnings("unchecked")
			@Override
			public T next() {
				currentIndex++;
				
				if (cache[currentIndex] == null) {
					cache[currentIndex] = getSourceValue();
				}
				return (T) cache[currentIndex];
			}

			@Override
			public void remove() {
				// Increment iterator
				getSourceValue();
				source.remove();
			}
			
			/**
			 * Retrieve the corresponding value from the source iterator.
			 */
			private T getSourceValue() {
				T last = null;
				
				while (iteratorIndex < currentIndex) {
					iteratorIndex++;
					last = source.next();
				}
				return last;
			}
		};
	}

	@Override
	public Object[] toArray() {
		Iterators.size(iterator());
		return cache.clone();
	}

	@SuppressWarnings({"unchecked", "hiding", "rawtypes"})
	@Override
	public <T> T[] toArray(T[] a) {
		Iterators.size(iterator());
		return (T[]) Arrays.copyOf(cache, size(), (Class) a.getClass().getComponentType());
	}

	@Override
	public boolean add(T e) {
		boolean result = delegate.add(e);
		
		growCache();
		return result;
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = delegate.addAll(c);
		
		growCache();
		return result;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}
	
	@Override
	public boolean remove(Object o) {
		cache = null;
		return delegate.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		cache = null;
		return delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		cache = null;
		return delegate.retainAll(c);
	}

	@Override
	public void clear() {
		cache = null;
		delegate.clear();
	}
	
	@Override
	public int hashCode() {
        int result = 1;

        // Combine all the hashCodes()
        for (Object element : this)
            result = 31 * result + (element == null ? 0 : element.hashCode());
        return result;
	}
	
	@Override
	public String toString() {
		Iterators.size(iterator());
		StringBuilder result = new StringBuilder("[");
		
		for (T element : this) {
			if (result.length() > 1)
				result.append(", ");
			result.append(element);
		}
		return result.append("]").toString();
	}
}
