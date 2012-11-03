package com.comphenix.protocol.concurrency;

import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

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
		private final Range<TKey> key;
		private EndPoint left;
		private EndPoint right;

		Entry(Range<TKey> key, EndPoint left, EndPoint right) {
			if (left == null)
				throw new IllegalAccessError("left cannot be NUll");
			if (right == null)
				throw new IllegalAccessError("right cannot be NUll");
			
			this.key = key;
			this.left = left;
			this.right = right;
		}

		@Override
		public Range<TKey> getKey() {
			return key;
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
	}
	
	/**
	 * Represents a single end point (open, close or both) of a range.
	 */
	protected class EndPoint {
		
		// Whether or not the end-point is opening a range, closing a range or both.
		public State state;
		
		// The value this range contains
		public TValue value;

		public EndPoint(State state, TValue value) {
			this.state = state;
			this.value = value;
		}	
	}
	
	// To quickly look up ranges we'll index them by endpoints
	protected NavigableMap<TKey, EndPoint> bounds = new TreeMap<TKey, EndPoint>();
	
	/**
	 * Removes every interval that intersects with the given range.
	 * @param lowerBound - lowest value to remove.
	 * @param upperBound - highest value to remove.
	 */
	public Set<Entry> remove(TKey lowerBound, TKey upperBound) {
		return remove(lowerBound, upperBound, false);
	}

	/**
	 * Removes every interval that intersects with the given range.
	 * @param lowerBound - lowest value to remove.
	 * @param upperBound - highest value to remove.
	 * @param preserveOutside - whether or not to preserve the intervals that are partially outside.
	 */
	public Set<Entry> remove(TKey lowerBound, TKey upperBound, boolean preserveDifference) {
		checkBounds(lowerBound, upperBound);
		NavigableMap<TKey, EndPoint> range = bounds.subMap(lowerBound, true, upperBound, true);
		
		boolean emptyRange = range.isEmpty();
		TKey first = !emptyRange ? range.firstKey() : null;
		TKey last = !emptyRange ? range.lastKey() : null;

		Set<Entry> resized = new HashSet<Entry>();
		Set<Entry> removed = new HashSet<Entry>();
		
		// Remove the previous element too. A close end-point must be preceded by an OPEN end-point.
		if (first != null && range.get(first).state == State.CLOSE) {
			TKey key = bounds.floorKey(first);
			EndPoint removedPoint = removeIfNonNull(key);
			
			// Add the interval back
			if (removedPoint != null && preserveDifference) {
				resized.add(putUnsafe(key, decrementKey(lowerBound), removedPoint.value));
			}
		}
		
		// Get the closing element too.
		if (last != null && range.get(last).state == State.OPEN) {
			TKey key = bounds.ceilingKey(last);
			EndPoint removedPoint = removeIfNonNull(key);
		
			if (removedPoint != null && preserveDifference) {
				resized.add(putUnsafe(incrementKey(upperBound), key, removedPoint.value));
			}
		}
		
		// Get the removed entries too
		getEntries(removed, range);
		invokeEntryRemoved(removed);
		
		if (preserveDifference) {
			invokeEntryRemoved(resized);
			invokeEntryAdded(resized);
		}
		
		// Remove the range as well
		range.clear();
		return removed;
	}
	
	// Helper
	private EndPoint removeIfNonNull(TKey key) {
		if (key != null) {
			return bounds.remove(key);
		} else {
			return null;
		}
	}
	
	// Adds a given end point
	protected EndPoint addEndPoint(TKey key, TValue value, State state) {
		EndPoint endPoint = bounds.get(key);
		
		if (endPoint != null) {
			endPoint.state = State.BOTH;
		} else {
			endPoint = new EndPoint(state, value);
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
			
			Range<TKey> range = Ranges.closed(lowerBound, upperBound);
			return new Entry(range, left, right);
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
		
		for (Map.Entry<TKey, EndPoint> entry : bounds.entrySet()) {
			switch (entry.getValue().state) {
			case BOTH:
				EndPoint point = entry.getValue();
				destination.add(new Entry(Ranges.singleton(entry.getKey()), point, point));
				break;
			case CLOSE:
				Range<TKey> range = Ranges.closed(last.getKey(), entry.getKey());
				destination.add(new Entry(range, last.getValue(), entry.getValue()));
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
			put(entry.key.lowerEndpoint(), entry.key.upperEndpoint(), entry.getValue());
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
	 * Get the end-point composite associated with this key.
	 * @param key - key to search for.
	 * @return The end point found, or NULL.
	 */
	protected EndPoint getEndPoint(TKey key) {
		EndPoint ends = bounds.get(key);
		
		if (ends != null) {
			// This is a piece of cake
			return ends;
		} else {
			
			// We need to determine if the point intersects with a range
			TKey left = bounds.floorKey(key);
			
			// We only need to check to the left
			if (left != null && bounds.get(left).state == State.OPEN) {
				return bounds.get(left);
			} else {
				return null;
			}
		}
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
