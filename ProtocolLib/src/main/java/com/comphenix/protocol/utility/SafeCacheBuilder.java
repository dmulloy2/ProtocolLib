package com.comphenix.protocol.utility;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;

/**
 * Represents a Guava CacheBuilder that is compatible with both Guava 10 and 13.
 * 
 * @author Kristian
 */
public class SafeCacheBuilder<K, V> {
	private CacheBuilder<K, V> builder;

	private static Method BUILD_METHOD;
	private  static Method AS_MAP_METHOD;
	
	@SuppressWarnings("unchecked")
	private SafeCacheBuilder() {
		builder = (CacheBuilder<K, V>) CacheBuilder.newBuilder();
	}

	/**
	 * Construct a new safe cache builder.
	 * 
	 * @return A new cache builder.
	 */
	public static <K, V> SafeCacheBuilder<K, V> newBuilder() {
		return new SafeCacheBuilder<K, V>();
	}

	/**
	 * Guides the allowed concurrency among update operations. Used as a hint
	 * for internal sizing. The table is internally partitioned to try to permit
	 * the indicated number of concurrent updates without contention. Because
	 * assignment of entries to these partitions is not necessarily uniform, the
	 * actual concurrency observed may vary. Ideally, you should choose a value
	 * to accommodate as many threads as will ever concurrently modify the
	 * table. Using a significantly higher value than you need can waste space
	 * and time, and a significantly lower value can lead to thread contention.
	 * But overestimates and underestimates within an order of magnitude do not
	 * usually have much noticeable impact. A value of one permits only one
	 * thread to modify the cache at a time, but since read operations can
	 * proceed concurrently, this still yields higher concurrency than full
	 * synchronization. Defaults to 4.
	 * 
	 * <p>
	 * <b>Note:</b>The default may change in the future. If you care about this
	 * value, you should always choose it explicitly.
	 * 
	 * @throws IllegalArgumentException if {@code concurrencyLevel} is
	 *             nonpositive
	 * @throws IllegalStateException if a concurrency level was already set
	 */
	public SafeCacheBuilder<K, V> concurrencyLevel(int concurrencyLevel) {
		builder.concurrencyLevel(concurrencyLevel);
		return this;
	}

	/**
	 * Specifies that each entry should be automatically removed from the cache
	 * once a fixed duration has elapsed after the entry's creation, or last
	 * access. Access time is reset by {@link com.google.common.cache.Cache#get Cache.get()} and
	 * {@link com.google.common.cache.Cache#getUnchecked Cache.getUnchecked()}, 
	 * but not by operations on the view returned by
	 * {@link com.google.common.cache.Cache#asMap() Cache.asMap()}.
	 * 
	 * <p>
	 * When {@code duration} is zero, elements will be evicted immediately after
	 * being loaded into the cache. This has the same effect as invoking
	 * {@link #maximumSize maximumSize}{@code (0)}. It can be useful in testing,
	 * or to disable caching temporarily without a code change.
	 * 
	 * <p>
	 * Expired entries may be counted by {@link com.google.common.cache.Cache#size Cache.size()}, but will never be
	 * visible to read or write operations. Expired entries are currently
	 * cleaned up during write operations, or during occasional read operations
	 * in the absense of writes; though this behavior may change in the future.
	 * 
	 * @param duration the length of time after an entry is last accessed that
	 *            it should be automatically removed
	 * @param unit the unit that {@code duration} is expressed in
	 * @throws IllegalArgumentException if {@code duration} is negative
	 * @throws IllegalStateException if the time to idle or time to live was
	 *             already set
	 */
	public SafeCacheBuilder<K, V> expireAfterAccess(long duration, TimeUnit unit) {
		builder.expireAfterAccess(duration, unit);
		return this;
	}

	/**
	 * Specifies that each entry should be automatically removed from the cache
	 * once a fixed duration has elapsed after the entry's creation, or the most
	 * recent replacement of its value.
	 * 
	 * <p>
	 * When {@code duration} is zero, elements will be evicted immediately after
	 * being loaded into the cache. This has the same effect as invoking
	 * {@link #maximumSize maximumSize}{@code (0)}. It can be useful in testing,
	 * or to disable caching temporarily without a code change.
	 * 
	 * <p>
	 * Expired entries may be counted by {@link com.google.common.cache.Cache#size Cache.size()}, but will never be
	 * visible to read or write operations. Expired entries are currently
	 * cleaned up during write operations, or during occasional read operations
	 * in the absense of writes; though this behavior may change in the future.
	 * 
	 * @param duration the length of time after an entry is created that it
	 *            should be automatically removed
	 * @param unit the unit that {@code duration} is expressed in
	 * @throws IllegalArgumentException if {@code duration} is negative
	 * @throws IllegalStateException if the time to live or time to idle was
	 *             already set
	 */
	public SafeCacheBuilder<K, V> expireAfterWrite(long duration, TimeUnit unit) {
		builder.expireAfterWrite(duration, unit);
		return this;
	}

	/**
	 * Sets the minimum total size for the internal hash tables. For example, if
	 * the initial capacity is {@code 60}, and the concurrency level is
	 * {@code 8}, then eight segments are created, each having a hash table of
	 * size eight. Providing a large enough estimate at construction time avoids
	 * the need for expensive resizing operations later, but setting this value
	 * unnecessarily high wastes memory.
	 * 
	 * @throws IllegalArgumentException if {@code initialCapacity} is negative
	 * @throws IllegalStateException if an initial capacity was already set
	 */
	public SafeCacheBuilder<K, V> initialCapacity(int initialCapacity) {
		builder.initialCapacity(initialCapacity);
		return this;
	}

	/**
	 * Specifies the maximum number of entries the cache may contain. Note that
	 * the cache <b>may evict an entry before this limit is exceeded</b>. As the
	 * cache size grows close to the maximum, the cache evicts entries that are
	 * less likely to be used again. For example, the cache may evict an entry
	 * because it hasn't been used recently or very often.
	 * 
	 * <p>
	 * When {@code size} is zero, elements will be evicted immediately after
	 * being loaded into the cache. This has the same effect as invoking
	 * {@link #expireAfterWrite expireAfterWrite}{@code (0, unit)} or
	 * {@link #expireAfterAccess expireAfterAccess}{@code (0,
	 * unit)}. It can be useful in testing, or to disable caching temporarily
	 * without a code change.
	 * 
	 * @param size the maximum size of the cache
	 * @throws IllegalArgumentException if {@code size} is negative
	 * @throws IllegalStateException if a maximum size was already set
	 */

	public SafeCacheBuilder<K, V> maximumSize(int size) {
		builder.maximumSize(size);
		return this;
	}

	/**
	 * Specifies a listener instance, which all caches built using this
	 * {@code CacheBuilder} will notify each time an entry is removed from the
	 * cache by any means.
	 * 
	 * <p>
	 * Each cache built by this {@code CacheBuilder} after this method is called
	 * invokes the supplied listener after removing an element for any reason
	 * (see removal causes in {@link com.google.common.cache.RemovalCause RemovalCause}). It will invoke the listener
	 * during invocations of any of that cache's public methods (even read-only
	 * methods).
	 * 
	 * <p>
	 * <b>Important note:</b> Instead of returning <em>this</em> as a
	 * {@code CacheBuilder} instance, this method returns
	 * {@code CacheBuilder<K1, V1>}. From this point on, either the original
	 * reference or the returned reference may be used to complete configuration
	 * and build the cache, but only the "generic" one is type-safe. That is, it
	 * will properly prevent you from building caches whose key or value types
	 * are incompatible with the types accepted by the listener already
	 * provided; the {@code CacheBuilder} type cannot do this. For best results,
	 * simply use the standard method-chaining idiom, as illustrated in the
	 * documentation at top, configuring a {@code CacheBuilder} and building
	 * your {@link com.google.common.cache.Cache Cache} all in a single statement.
	 * 
	 * <p>
	 * <b>Warning:</b> if you ignore the above advice, and use this
	 * {@code CacheBuilder} to build a cache whose key or value type is
	 * incompatible with the listener, you will likely experience a
	 * {@link ClassCastException} at some <i>undefined</i> point in the future.
	 * 
	 * @throws IllegalStateException if a removal listener was already set
	 */
	@SuppressWarnings("unchecked")
	public <K1 extends K, V1 extends V> SafeCacheBuilder<K1, V1> removalListener(RemovalListener<? super K1, ? super V1> listener) {
		builder.removalListener(listener);
		return (SafeCacheBuilder<K1, V1>) this;
	}

	/**
	 * Specifies a nanosecond-precision time source for use in determining when
	 * entries should be expired. By default, {@link System#nanoTime} is used.
	 * 
	 * <p>
	 * The primary intent of this method is to facilitate testing of caches
	 * which have been configured with {@link #expireAfterWrite} or
	 * {@link #expireAfterAccess}.
	 * 
	 * @throws IllegalStateException if a ticker was already set
	 */
	public SafeCacheBuilder<K, V> ticker(Ticker ticker) {
		builder.ticker(ticker);
		return this;
	}

	/**
	 * Specifies that each value (not key) stored in the cache should be wrapped
	 * in a {@link java.lang.ref.SoftReference SoftReference} (by default, strong references are used).
	 * Softly-referenced objects will be garbage-collected in a <i>globally</i>
	 * least-recently-used manner, in response to memory demand.
	 * 
	 * <p>
	 * <b>Warning:</b> in most circumstances it is better to set a per-cache
	 * {@linkplain #maximumSize maximum size} instead of using soft references.
	 * You should only use this method if you are well familiar with the
	 * practical consequences of soft references.
	 * 
	 * <p>
	 * <b>Note:</b> when this method is used, the resulting cache will use
	 * identity ({@code ==}) comparison to determine equality of values.
	 * 
	 * @throws IllegalStateException if the value strength was already set
	 */
	public SafeCacheBuilder<K, V> softValues() {
		builder.softValues();
		return this;
	}

	/**
	 * Specifies that each key (not value) stored in the cache should be wrapped
	 * in a {@link java.lang.ref.WeakReference WeakReference} (by default, strong references are used).
	 * 
	 * <p>
	 * <b>Warning:</b> when this method is used, the resulting cache will use
	 * identity ({@code ==}) comparison to determine equality of keys.
	 * 
	 * @throws IllegalStateException if the key strength was already set
	 */
	public SafeCacheBuilder<K, V> weakKeys() {
		builder.weakKeys();
		return this;
	}

	/**
	 * Specifies that each value (not key) stored in the cache should be wrapped
	 * in a {@link java.lang.ref.WeakReference WeakReference} (by default, strong references are used).
	 * 
	 * <p>
	 * Weak values will be garbage collected once they are weakly reachable.
	 * This makes them a poor candidate for caching; consider
	 * {@link #softValues} instead.
	 * 
	 * <p>
	 * <b>Note:</b> when this method is used, the resulting cache will use
	 * identity ({@code ==}) comparison to determine equality of values.
	 * 
	 * @throws IllegalStateException if the value strength was already set
	 */
	public SafeCacheBuilder<K, V> weakValues() {
		builder.weakValues();
		return this;
	}
	
	/**
	 * Returns the cache wrapped as a ConcurrentMap.
	 * <>
	 * We can't return the direct Cache instance as it changed in Guava 13.
	 * @return The cache as a map.
	 */
	@SuppressWarnings("unchecked")
	public <K1 extends K, V1 extends V> ConcurrentMap<K1, V1> build(CacheLoader<? super K1, V1> loader) {
		Object cache = null;
		
		if (BUILD_METHOD == null) {
			try {
				BUILD_METHOD = builder.getClass().getDeclaredMethod("build", CacheLoader.class);
				BUILD_METHOD.setAccessible(true);
			} catch (Exception e) {
				throw new FieldAccessException("Unable to find CacheBuilder.build(CacheLoader)", e);
			}
		}
		
		// Attempt to build the Cache
		try {
			cache = BUILD_METHOD.invoke(builder, loader);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to invoke " + BUILD_METHOD + " on " + builder, e);
		}
		
		if (AS_MAP_METHOD == null) {
			try {
				AS_MAP_METHOD = cache.getClass().getMethod("asMap");
				AS_MAP_METHOD.setAccessible(true);
			} catch (Exception e) {
				throw new FieldAccessException("Unable to find Cache.asMap() in " + cache, e);
			}
		}
		
		// Retrieve it as a map
		try {
			return (ConcurrentMap<K1, V1>) AS_MAP_METHOD.invoke(cache);
		} catch (Exception e) {
			throw new FieldAccessException("Unable to invoke " + AS_MAP_METHOD + " on " + cache, e);
		}
	}
}
