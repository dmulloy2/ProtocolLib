package com.comphenix.protocol.wrappers.collection;

import java.util.Set;

/**
 * Represents a cached set. Enumeration of the set will use a cached inner list.
 * 
 * @author Kristian
 * @param <T> - the element type.
 */
public class CachedSet<T> extends CachedCollection<T> implements Set<T> {
	/**
	 * Construct a cached set from the given delegate.
	 * @param delegate - the set delegate.
	 */
	public CachedSet(Set<T> delegate) {
		super(delegate);
	}
}
