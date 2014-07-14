package com.comphenix.protocol.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

/**
 * Represents a hash map where each association may expire after a given time has elapsed.
 * <p>
 * Note that replaced key-value associations are only collected once the original expiration time has elapsed. 
 * 
 * @author Kristian Stangeland
 *
 * @param <K> - type of the keys.
 * @param <V> - type of the values.
 */
public class ExpireHashMap<K, V> {
	private class ExpireEntry implements Comparable<ExpireEntry> {
		public final long expireTime;
		public final K expireKey;
		public final V expireValue;
		
		public ExpireEntry(long expireTime, K expireKey, V expireValue) {
			this.expireTime = expireTime;
			this.expireKey = expireKey;
			this.expireValue = expireValue;
		}

		@Override
		public int compareTo(ExpireEntry o) {
			return Longs.compare(expireTime, o.expireTime);
		}

		@Override
		public String toString() {
			return "ExpireEntry [expireTime=" + expireTime + ", expireKey=" + expireKey
					+ ", expireValue=" + expireValue + "]";
		}
	}
	
	private Map<K, ExpireEntry> keyLookup = new HashMap<K, ExpireEntry>();
	private PriorityQueue<ExpireEntry> expireQueue =  new PriorityQueue<ExpireEntry>();
	
	// View of keyLookup with direct values
	private Map<K, V> valueView = Maps.transformValues(keyLookup, new Function<ExpireEntry, V>() {
		@Override
		public V apply(ExpireEntry entry) {
			return entry.expireValue;
		}
	});
	
	// Supplied by the constructor
	private Ticker ticker;
	
	/**
	 * Construct a new hash map where each entry may expire at a given time.
	 */
	public ExpireHashMap() {
		this(Ticker.systemTicker());
	}
	
	/**
	 * Construct a new hash map where each entry may expire at a given time.
	 * @param ticker - supplier of the current time.
	 */
	public ExpireHashMap(Ticker ticker) {
		this.ticker = ticker;
	}
	
	/**
	 * Retrieve the value associated with the given key, if it has not expired.
	 * @param key - the key.
	 * @return The value, or NULL if not found or it has expired.
	 */
	public V get(K key) {
		evictExpired();
		
		ExpireEntry entry = keyLookup.get(key);
		return entry != null ? entry.expireValue : null;
	}
	
	/**
	 * Associate the given key with the given value, until the expire delay have elapsed.
	 * @param key - the key.
	 * @param value - the value.
	 * @param expireDelay - the amount of time until this association expires. Must be greater than zero.
	 * @param expireUnit - the unit of the expiration.
	 * @return Any previously unexpired association with this key, or NULL.
	 */
	public V put(K key, V value, long expireDelay, TimeUnit expireUnit) {
		Preconditions.checkNotNull(expireUnit, "expireUnit cannot be NULL");
		Preconditions.checkState(expireDelay > 0, "expireDelay cannot be equal or less than zero.");
		evictExpired();
		
		ExpireEntry entry = new ExpireEntry(
			ticker.read() + TimeUnit.NANOSECONDS.convert(expireDelay, expireUnit), 
			key, value
		); 
		ExpireEntry previous = keyLookup.put(key, entry);
		
		// We enqueue its removal
		expireQueue.add(entry);
		return previous != null ? previous.expireValue : null;
	}
	
	/**
	 * Determine if the given key is referring to an unexpired association in the map.
 	 * @param key - the key.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean containsKey(K key) {
		evictExpired();
		return keyLookup.containsKey(key);
	}
	
	/**
	 * Determine if the given value is referring to an unexpired association in the map.
 	 * @param value - the value.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean containsValue(V value) {
		evictExpired();
		
		// Linear scan is the best we've got
		for (ExpireEntry entry : keyLookup.values()) {
			if (Objects.equal(value, entry.expireValue)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove a key and its associated value from the map.
	 * @param key - the key to remove.
	 * @return Value of the removed association, NULL otherwise.
	 */
	public V removeKey(K key) {
		evictExpired();
		
		ExpireEntry entry = keyLookup.remove(key);
		return entry != null ? entry.expireValue : null;
	}
	
	/**
	 * Retrieve the number of entries in the map.
	 * @return The number of entries.
	 */
	public int size() {
		evictExpired();
		return keyLookup.size();
	}
	
	/**
	 * Retrieve a view of the keys in the current map.
	 * @return View of the keys.
	 */
	public Set<K> keySet() {
		evictExpired();
		return keyLookup.keySet();
	}
	
	/**
	 * Retrieve a view of all the values in the current map.
	 * @return All the values.
	 */
	public Collection<V> values() {
		evictExpired();
		return valueView.values();
	}
	
	/**
	 * Retrieve a view of all the entries in the set.
	 * @return All the entries.
	 */
	public Set<Entry<K, V>> entrySet() {
		evictExpired();
		return valueView.entrySet();
	}
	
	/**
	 * Retrieve a view of this expire map as an ordinary map that does not support insertion.
	 * @return The map.
	 */
	public Map<K, V> asMap() {
		evictExpired();
		return valueView;
	}
	
	/**
	 * Clear all references to key-value pairs that have been removed or replaced before they were naturally evicted.
	 * <p>
	 * This operation requires a linear scan of the current entries in the map.
	 */
	public void collect() {
		// First evict what we can
		evictExpired();
		
		// Recreate the eviction queue - this is faster than removing entries in the old queue
		expireQueue.clear();
		expireQueue.addAll(keyLookup.values());
	}
	
	/**
	 * Clear all the entries in the current map.
	 */
	public void clear() {
		keyLookup.clear();
		expireQueue.clear();
	}
	
	/**
	 * Evict any expired entries in the map.
	 * <p>
	 * This is called automatically by any of the read or write operations.
	 */
	protected void evictExpired() {
		long currentTime = ticker.read();
		
		// Remove expired entries
		while (expireQueue.size() > 0 && expireQueue.peek().expireTime <= currentTime) {
			ExpireEntry entry = expireQueue.poll();

			if (entry == keyLookup.get(entry.expireKey)) {
				keyLookup.remove(entry.expireKey);
			}
		}
	}
	
	@Override
	public String toString() {
		return valueView.toString();
	}
}
 