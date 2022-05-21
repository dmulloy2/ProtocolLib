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

package com.comphenix.protocol.concurrency;

import com.google.common.base.Objects;
import com.google.common.collect.Range;

import java.util.*;

/**
 * Represents a generic store of intervals and associated values. No two intervals
 * can overlap in this representation.
 * <p>
 * Note that this implementation is not thread safe.
 * 
 * @author Kristian
 *
 * @param <TKey> - type of the key. Must implement Comparable.
 * @param <TValue> - type of the value to associate.
 */
public abstract class AbstractIntervalTree<TKey extends Comparable<TKey>, TValue> {
	
	protected enum State {
		OPEN,
		CLOSE,
		BOTH
	}
	
	/**
	 * Represents a range and a value in this interval tree.
	 */
	public class Entry implements Map.Entry<Range<TKey>, TValue> {
		private EndPoint left;
		private EndPoint right;

		Entry(EndPoint left, EndPoint right) {
			if (left == null)
				throw new IllegalAccessError("left cannot be NUll");
			if (right == null)
				throw new IllegalAccessError("right cannot be NUll");
			if (left.key.compareTo(right.key) > 0)
				throw new IllegalArgumentException(
						"Left key (" + left.key + ") cannot be greater than the right key (" + right.key + ")");
			
			this.left = left;
			this.right = right;
		}

		@Override
		public Range<TKey> getKey() {
			return Range.closed(left.key, right.key);
		}

		@Override
		public TValue getValue() {
			return left.value;
		}

		@Override
		public TValue setValue(TValue value) {
			TValue old = left.value;
			
			// Set both end points
			left.value = value;
			right.value = value;
			return old;
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public boolean equals(Object obj) {
			// Quick equality check
			if (obj == this) {
				return true;
			} else if (obj instanceof AbstractIntervalTree.Entry) {
				return Objects.equal(left.key, ((AbstractIntervalTree.Entry) obj).left.key) &&
					   Objects.equal(right.key, ((AbstractIntervalTree.Entry) obj).right.key) &&
					   Objects.equal(left.value, ((AbstractIntervalTree.Entry) obj).left.value);
			} else {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(left.key, right.key, left.value);
		}
		
		@Override
		public String toString() {
			return String.format("Value %s at [%s, %s]", left.value, left.key, right.key);
		}
	}
	
	/**
	 * Represents a single end point (open, close or both) of a range.
	 */
	protected class EndPoint {
		
		// Whether or not the end-point is opening a range, closing a range or both.
		public State state;
		
		// The value this range contains
		public TValue value;
		
		// The key of this end point
		public TKey key;

		public EndPoint(State state, TKey key, TValue value) {
			this.state = state;
			this.key = key;
			this.value = value;
		}
	}
	
	// To quickly look up ranges we'll index them by endpoints
	protected NavigableMap<TKey, EndPoint> bounds = new TreeMap<TKey, EndPoint>();
	
	/**
	 * Removes every interval that intersects with the given range.
	 * @param lowerBound - lowest value to remove.
	 * @param upperBound - highest value to remove.
	 * @return Intervals that were removed
	 */
	public Set<Entry> remove(TKey lowerBound, TKey upperBound) {
		return remove(lowerBound, upperBound, false);
	}
	
	/**
	 * Removes every interval that intersects with the given range.
	 * @param lowerBound - lowest value to remove.
	 * @param upperBound - highest value to remove.
	 * @param preserveDifference - whether or not to preserve the intervals that are partially outside.
	 * @return Intervals that were removed
	 */
	public Set<Entry> remove(TKey lowerBound, TKey upperBound, boolean preserveDifference) {
		checkBounds(lowerBound, upperBound);
		NavigableMap<TKey, EndPoint> range = bounds.subMap(lowerBound, true, upperBound, true);
		
		EndPoint first = getNextEndPoint(lowerBound, true);
		EndPoint last = getPreviousEndPoint(upperBound, true);
		
		// Used while resizing intervals
		EndPoint previous = null;
		EndPoint next = null;
		
		Set<Entry> resized = new HashSet<Entry>();
		Set<Entry> removed = new HashSet<Entry>();
		
		// Remove the previous element too. A close end-point must be preceded by an OPEN end-point.
		if (first != null && first.state == State.CLOSE) {
			previous = getPreviousEndPoint(first.key, false);
			
			// Add the interval back
			if (previous != null) {
				removed.add(getEntry(previous, first));
			}
		}
		
		// Get the closing element too.
		if (last != null && last.state == State.OPEN) {
			next = getNextEndPoint(last.key, false);
		
			if (next != null) {
				removed.add(getEntry(last, next));
			}
		}
		
		// Now remove both ranges
		removeEntrySafely(previous, first);
		removeEntrySafely(last, next);
		
		// Add new resized intervals
		if (preserveDifference) {
			if (previous != null) {
				resized.add(putUnsafe(previous.key, decrementKey(lowerBound), previous.value));
			}
			if (next != null) {
				resized.add(putUnsafe(incrementKey(upperBound), next.key, next.value));
			}
		}
		
		// Get the removed entries too
		getEntries(removed, range);
		invokeEntryRemoved(removed);
		
		if (preserveDifference) {
			invokeEntryAdded(resized);
		}
		
		// Remove the range as well
		range.clear();
		return removed;
	}
	
	/**
	 * Retrieve the entry from a given set of end points.
	 * @param left - leftmost end point.
	 * @param right - rightmost end point.
	 * @return The associated entry.
	 */
	protected Entry getEntry(EndPoint left, EndPoint right) {
		if (left == null)
			throw new IllegalArgumentException("left endpoint cannot be NULL.");
		if (right == null)
			throw new IllegalArgumentException("right endpoint cannot be NULL.");
		
		// Make sure the order is correct
		if (right.key.compareTo(left.key) < 0) {
			return getEntry(right, left);
		} else {
			return new Entry(left, right);
		}
	}
	
	private void removeEntrySafely(EndPoint left, EndPoint right) {
		if (left != null && right != null) {
			bounds.remove(left.key);
			bounds.remove(right.key);
		}
	}
	
	// Adds a given end point
	protected EndPoint addEndPoint(TKey key, TValue value, State state) {
		EndPoint endPoint = bounds.get(key);
		
		if (endPoint != null) {
			endPoint.state = State.BOTH;
		} else {
			endPoint = new EndPoint(state, key, value);
			bounds.put(key, endPoint);
		}
		return endPoint;
	}
	
	/**
	 * Associates a given interval of keys with a certain value. Any previous
	 * association will be overwritten in the given interval.
	 * <p>
	 * Overlapping intervals are not permitted. A key can only be associated with a single value.
	 * 
	 * @param lowerBound - the minimum key (inclusive).
	 * @param upperBound - the maximum key (inclusive).
	 * @param value - the value, or NULL to reset this range.
	 */
	public void put(TKey lowerBound, TKey upperBound, TValue value) {
		// While we don't permit overlapping intervals, we'll still allow overwriting existing intervals.
		remove(lowerBound, upperBound, true);
		invokeEntryAdded(putUnsafe(lowerBound, upperBound, value));
	}
	
	/**
	 * Associates a given interval without performing any interval checks.
	 * @param lowerBound - the minimum key (inclusive).
	 * @param upperBound - the maximum key (inclusive).
	 * @param value - the value, or NULL to reset the range.
	 */
	private Entry putUnsafe(TKey lowerBound, TKey upperBound, TValue value) {
		// OK. Add the end points now
		if (value != null) {
			EndPoint left = addEndPoint(lowerBound, value, State.OPEN);
			EndPoint right = addEndPoint(upperBound, value, State.CLOSE);

			return new Entry(left, right);
		} else {
			return null;
		}
	}
	
	/**
	 * Used to verify the validity of the given interval.
	 * @param lowerBound - lower bound (inclusive).
	 * @param upperBound - upper bound (inclusive).
	 */
	private void checkBounds(TKey lowerBound, TKey upperBound) {
		if (lowerBound == null)
			throw new IllegalAccessError("lowerbound cannot be NULL.");
		if (upperBound == null)
			throw new IllegalAccessError("upperBound cannot be NULL.");
		if (upperBound.compareTo(lowerBound) < 0)
			throw new IllegalArgumentException("upperBound cannot be less than lowerBound.");
	}
	
	/**
	 * Determines if the given key is within an interval.
	 * @param key - key to check.
	 * @return TRUE if the given key is within an interval in this tree, FALSE otherwise.
	 */
	public boolean containsKey(TKey key) {
		return getEndPoint(key) != null;
	}
	
	/**
	 * Enumerates over every range in this interval tree.
	 * @return Number of ranges.
	 */
	public Set<Entry> entrySet() {
		// Don't mind the Java noise
		Set<Entry> result = new HashSet<Entry>();
		getEntries(result, bounds);
		return result;
	}
	
	/**
	 * Remove every interval.
	 */
	public void clear() {
		if (!bounds.isEmpty()) {
			remove(bounds.firstKey(), bounds.lastKey());
		}
	}
	
	/**
	 * Converts a map of end points into a set of entries.
	 * @param destination - set of entries.
	 * @param map - a map of end points.
	 */
	private void getEntries(Set<Entry> destination, NavigableMap<TKey, EndPoint> map) {
		Map.Entry<TKey, EndPoint> last = null;
		
		for (Map.Entry<TKey, EndPoint> entry : map.entrySet()) {
			switch (entry.getValue().state) {
			case BOTH:
				EndPoint point = entry.getValue();
				destination.add(new Entry(point, point));
				break;
			case CLOSE:
				if (last != null) {
					destination.add(new Entry(last.getValue(), entry.getValue()));
				}
				break;
			case OPEN:
				// We don't know the full range yet
				last = entry;
				break;
			default:
				throw new IllegalStateException("Illegal open/close state detected.");
			}
		}
	}
	
	/**
	 * Inserts every range from the given tree into the current tree.
	 * @param other - the other tree to read from.
	 */
	public void putAll(AbstractIntervalTree<TKey, TValue> other) {
		// Naively copy every range.
		for (Entry entry : other.entrySet()) {
			put(entry.left.key, entry.right.key, entry.getValue());
		}
	}
	
	/**
	 * Retrieves the value of the range that matches the given key, or NULL if nothing was found.
	 * @param key - the level to read for.
	 * @return The correct amount of experience, or NULL if nothing was recorded.
	 */
	public TValue get(TKey key) {
		EndPoint point = getEndPoint(key);

		if (point != null)
			return point.value;
		else
			return null;
	}
	
	/**
	 * Get the left-most end-point associated with this key.
	 * @param key - key to search for.
	 * @return The end point found, or NULL.
	 */
	protected EndPoint getEndPoint(TKey key) {
		EndPoint ends = bounds.get(key);
		
		if (ends != null) {
			// Always return the end point to the left
			if (ends.state == State.CLOSE) {
				Map.Entry<TKey, EndPoint> left = bounds.floorEntry(decrementKey(key));
				return left != null ? left.getValue() : null;
				
			} else {
				return ends;
			}
			
		} else {
			// We need to determine if the point intersects with a range
			Map.Entry<TKey, EndPoint> left = bounds.floorEntry(key);
			
			// We only need to check to the left
			if (left != null && left.getValue().state == State.OPEN) {
				return left.getValue();
			} else {
				return null;
			}
		}
	}
	
	/**
	 * Get the previous end point of a given key.
	 * @param point - the point to search with.
	 * @param inclusive - whether or not to include the current point in the search.
	 * @return The previous end point of a given given key, or NULL if not found.
	 */
	protected EndPoint getPreviousEndPoint(TKey point, boolean inclusive) {
		if (point != null) {
			Map.Entry<TKey, EndPoint> previous = bounds.floorEntry(inclusive ? point : decrementKey(point));
		
			if (previous != null)
				return previous.getValue();
		}
		return null;
	}
	
	/**
	 * Get the next end point of a given key.
	 * @param point - the point to search with.
	 * @param inclusive - whether or not to include the current point in the search.
	 * @return The next end point of a given given key, or NULL if not found.
	 */
	protected EndPoint getNextEndPoint(TKey point, boolean inclusive) {
		if (point != null) {
			Map.Entry<TKey, EndPoint> next = bounds.ceilingEntry(inclusive ? point : incrementKey(point));
		
			if (next != null)
				return next.getValue();
		}
		return null;
	}

	private void invokeEntryAdded(Entry added) {
		if (added != null) {
			onEntryAdded(added);
		}
	}
	
	private void invokeEntryAdded(Set<Entry> added) {
		for (Entry entry : added) {
			onEntryAdded(entry);
		}
	}

	private void invokeEntryRemoved(Set<Entry> removed) {
		for (Entry entry : removed) {
			onEntryRemoved(entry);
		}
	}
	
	// Listeners for added or removed entries
	/**
	 * Invoked when an entry is added.
	 * @param added - the entry that was added.
	 */
	protected void onEntryAdded(Entry added) { }
	
	/**
	 * Invoked when an entry is removed.
	 * @param removed - the removed entry.
	 */
	protected void onEntryRemoved(Entry removed) { }
	
	// Helpers for decrementing or incrementing key values
	/**
	 * Decrement the given key by one unit.
	 * @param key - the key that should be decremented.
	 * @return The new decremented key.
	 */
	protected abstract TKey decrementKey(TKey key);
	
	/**
	 * Increment the given key by one unit.
	 * @param key - the key that should be incremented.
	 * @return The new incremented key.
	 */
	protected abstract TKey incrementKey(TKey key);
}
