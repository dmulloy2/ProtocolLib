package com.comphenix.protocol.concurrent;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A collection that stores elements in a sorted order based on a provided {@link Comparable},
 * while ensuring element equality is checked using their {@code equals} method.
 * <p>
 * This class uses a copy-on-write strategy for updates, ensuring that iteration over the collection
 * is safe for concurrent use, even though the collection itself is not thread-safe for modifications.
 * <p>
 * Elements are inserted into the set in a position determined by their natural ordering. If multiple elements
 * have comparables that are considered equal (i.e., {@code compareTo} returns zero), they will maintain their
 * insertion order. If an element is already present in the set (as determined by {@code equals}), it will not
 * be added again.
 * 
 * @param <E> the type of elements maintained by this set
 * @param <C> the type of the comparable used for ordering the elements
 */
@SuppressWarnings("unchecked")
public class SortedCopyOnWriteSet<E, C extends Comparable<C>> implements Iterable<E> {

	private volatile Entry<E, C>[] array = new Entry[0];

	/**
	 * Adds the specified element to this set in a sorted order based on the
	 * provided {@code Comparable}. The element will be inserted before the first
	 * position that is strictly greater than the element. This ensures that
	 * elements maintain their insertion order when their comparables are considered
	 * equal (i.e., when {@code compareTo} returns zero).
	 * <p>
	 * If the set already contains the element (as determined by {@code equals}),
	 * the element is not added again.
	 * </p>
	 *
	 * @param element    the element to be added
	 * @param comparable the comparable used to determine the element's position in
	 *                   the sorted order
	 * @return {@code true} if the element was added to the set; {@code false} if
	 *         the set already contained the element
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean add(E element, C comparable) {
		Objects.requireNonNull(element, "element cannot be null");

		// create new entry
		Entry<E, C> entry = new Entry<>(element, comparable);

		// Find correct insert index for element by compareTo, also use same loop to
		// scan for duplicate elements
		int insertIndex = -1;
		for (int index = 0; index < array.length; index++) {
			if (insertIndex == -1 && entry.compareTo(array[index]) < 0) {
				insertIndex = index;
			}

			// array already contains element, return false
			if (array[index].is(element)) {
				return false;
			}
		}

		// insert at end of array
		if (insertIndex == -1) {
			insertIndex = array.length;
		}

		// create a new array of size N+1
		Entry<E, C>[] newArray = new Entry[array.length + 1];

		// copy the old array to the new array and insert the new element
		System.arraycopy(array, 0, newArray, 0, insertIndex);
		newArray[insertIndex] = entry;
		System.arraycopy(array, insertIndex, newArray, insertIndex + 1, array.length - insertIndex);

		// copy new array to field
		array = newArray;

		return true;
	}

	/**
	 * Removes the specified element from this set if it is present.
	 *
	 * @param element the element to be removed
	 * @return {@code true} if the set contained the specified element;
	 *         {@code false} otherwise
	 * @throws NullPointerException if the specified element is null
	 */
	public boolean remove(E element) {
		Objects.requireNonNull(element, "element cannot be null");

		// find the element in array
		int removeIndex = -1;
		for (int index = 0; index < array.length; index++) {
			if (array[index].is(element)) {
				removeIndex = index;
				break;
			}
		}

		// can't find element, return false
		if (removeIndex < 0) {
			return false;
		}

		// create a new array of size N-1
		Entry<E, C>[] newArray = new Entry[array.length - 1];

		// copy the elements from the old array to the new array, excluding removed element
		System.arraycopy(array, 0, newArray, 0, removeIndex);
		System.arraycopy(array, removeIndex + 1, newArray, removeIndex, array.length - removeIndex - 1);

		// copy new array to field
		array = newArray;

		return true;
	}

	/**
	 * Returns {@code true} if this set contains no elements.
	 *
	 * @return {@code true} if this set contains no elements
	 */
	public boolean isEmpty() {
		return this.array.length == 0;
	}

	/**
	 * Returns an iterator over the elements in this set. The elements are returned
	 * in natural order.
	 *
	 * @return an iterator over the elements in this set
	 */
	@Override
	public Iterator<E> iterator() {
		return new ElementIterator(this.array);
	}

	private class ElementIterator implements Iterator<E> {

		private final Entry<E, C>[] array;
		private int cursor = 0;

		public ElementIterator(Entry<E, C>[] array) {
			this.array = array;
		}

		@Override
		public boolean hasNext() {
			return this.cursor < this.array.length;
		}

		@Override
		public E next() {
			if (this.cursor >= this.array.length) {
				throw new NoSuchElementException();
			}

			int index = this.cursor++;
			return this.array[index].getElement();
		}
	}

	private static class Entry<E, C extends Comparable<C>> implements Comparable<Entry<E, C>> {

		private E element;
		private C comperable;

		public Entry(E element, C comperable) {
			this.element = element;
			this.comperable = comperable;
		}

		public E getElement() {
			return element;
		}

		@Override
		public int compareTo(Entry<E, C> other) {
			return this.comperable.compareTo(other.comperable);
		}

		public boolean is(E element) {
			return this.element.equals(element);
		}
	}
}
