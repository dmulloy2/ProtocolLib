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

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.MapMaker;

/**
 * A map that supports blocking on read operations. Null keys are not supported.
 * <p>
 * Keys are stored as weak references, and will be automatically removed once they've all been dereferenced.
 * <p>
 * @author Kristian
 *
 * @param <TKey> - type of the key.
 * @param <TValue> - type of the value.
 */
public class BlockingHashMap<TKey, TValue> {

	// Map of values
	private final ConcurrentMap<TKey, TValue> backingMap;

	// Map of locked objects
	private final ConcurrentMap<TKey, Object> locks;
	
	/**
	 * Initialize a new map.
	 */
	public BlockingHashMap() {
		backingMap = new MapMaker().weakKeys().makeMap();
		locks = new MapMaker().weakKeys().makeMap();
	}
	
	/**
	 * Initialize a new map.
	 * @return The created map.
	 */
	public static <TKey, TValue> BlockingHashMap<TKey, TValue> create() {
		return new BlockingHashMap<TKey, TValue>();
	}
	
	/**
	 * Waits until a value has been associated with the given key, and then retrieves that value.
	 * @param key - the key whose associated value is to be returned 
	 * @return The value to which the specified key is mapped.
	 * @throws InterruptedException If the current thread got interrupted while waiting.
	 */
	public TValue get(TKey key) throws InterruptedException {
		if (key == null)
			throw new IllegalArgumentException("key cannot be NULL.");
		
		TValue value = backingMap.get(key);
		
		// Only lock if no value is available
		if (value == null) {
			final Object lock = getLock(key);
			
			synchronized (lock) {
				while (value == null) {
					lock.wait();
					value = backingMap.get(key);
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Waits until a value has been associated with the given key, and then retrieves that value.
	 * @param key - the key whose associated value is to be returned 
	 * @param timeout - the amount of time to wait until an association has been made.
	 * @param unit - unit of timeout.
	 * @return The value to which the specified key is mapped, or NULL if the timeout elapsed.
	 * @throws InterruptedException If the current thread got interrupted while waiting.
	 */
	public TValue get(TKey key, long timeout, TimeUnit unit) throws InterruptedException {
		if (key == null)
			throw new IllegalArgumentException("key cannot be NULL.");
		if (unit == null)
			throw new IllegalArgumentException("Unit cannot be NULL.");
		
		TValue value = backingMap.get(key);
		
		// Only lock if no value is available
		if (value == null) {
			final Object lock = getLock(key);
			final long stopTimeNS = System.nanoTime() + unit.toNanos(timeout);
			
			// Don't exceed the timeout
			synchronized (lock) {
				while (value == null) {
					long remainingTime = stopTimeNS - System.nanoTime();
					
					if (remainingTime > 0) {
						TimeUnit.NANOSECONDS.timedWait(lock, remainingTime);
						value = backingMap.get(key);
					} else {
						// Timeout elapsed
						break;
					}
				}
			}
		}
		
		return value;
	}
	
	/**
	 * Associate a given key with the given value.
	 * <p>
	 * Wakes up any blocking getters on this specific key.
	 * 
	 * @param key - the key to associate.
	 * @param value - the value.
	 * @return The previously associated value.
	 */
	public TValue put(TKey key, TValue value) {
		if (value == null)
			throw new IllegalArgumentException("This map doesn't support NULL values.");
		
		final TValue previous = backingMap.put(key, value);
		final Object lock = getLock(key);
		
		// Inform our readers about this change
		synchronized (lock) {
			lock.notifyAll();
			return previous;
		}
	}
	
	public int size() {
		return backingMap.size();
	}
	
	public Collection<TValue> values() {
		return backingMap.values();
	}
	
	public Set<TKey> keys() {
		return backingMap.keySet();
	}
	
	/**
	 * Atomically retrieve the lock associated with a given key.
	 * @param key - the current key.
	 * @return An asssociated lock.
	 */
	private Object getLock(TKey key) {
		Object lock = locks.get(key);
		
		if (lock == null) {
			Object created = new Object();
			
			// Do this atomically
			lock = locks.putIfAbsent(key, created);
			
			// If we succeeded, use the latch we created - otherwise, use the already inserted latch
			if (lock == null) {
				lock = created;
			}
		}
		
		return lock;
	}
}
