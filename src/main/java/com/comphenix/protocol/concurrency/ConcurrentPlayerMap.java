package com.comphenix.protocol.concurrency;

import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.comphenix.protocol.utility.Util;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalListener;
import com.google.common.collect.AbstractIterator;
import com.google.common.util.concurrent.UncheckedExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a concurrent player map.
 * <p>
 * This map may use player addresses as keys.
 * @author Kristian
 */
public class ConcurrentPlayerMap<TValue> extends AbstractMap<Player, TValue> implements ConcurrentMap<Player, TValue> {
	/**
	 * Represents the different standard player keys,
	 * @author Kristian
	 */
	public enum PlayerKey implements Function<Player, Object> {
		/**
		 * Use a player's {@link Player#getAddress()} as key in the map.
		 */
		ADDRESS {
			@Override
			public Object apply(Player player) {
				return player == null ? null : player.getAddress();
			}
		},
		
		/**
		 * Use a player's {@link Player#getName()} as key in the map.
		 */
		NAME {
			@Override
			public Object apply(Player player) {
				return player == null ? null : player.getName();
			}
		},
	}
	
	/**
	 * An internal map of player keys to values.
	 */
	private ConcurrentMap<Object, TValue> valueLookup = createValueMap();
	
	/**
	 * A cache of the associated keys for each player.
	 */

	private ConcurrentMap<Object, Player> keyLookup = createKeyCache();
	/**
	 * The method used to retrieve a unique key for a player.
	 */
	private final Function<Player, Object> keyMethod;
	
	/**
	 * Construct a new concurrent player map that uses each player's address as key.
	 * @param <T> Parameter type
	 * @return Concurrent player map.
	 */
	public static <T> ConcurrentPlayerMap<T> usingAddress() {
		return new ConcurrentPlayerMap<>(PlayerKey.ADDRESS);
	}
	
	/**
	 * Construct a new concurrent player map that uses each player's name as key.
	 * @param <T> Parameter type
	 * @return Concurrent player map.
	 */
	public static <T> ConcurrentPlayerMap<T> usingName() {
		return new ConcurrentPlayerMap<>(PlayerKey.NAME);
	}
	
	/**
	 * Construct a new concurrent player map using the given standard key method.
	 * @param standardMethod - the standard key method.
	 */
	private ConcurrentPlayerMap(PlayerKey standardMethod) {
		this.keyMethod = standardMethod;
	}
	
	/**
	 * Construct a new concurrent player map using the given custom key method.
	 * @param method - custom key method.
	 */
	public ConcurrentPlayerMap(Function<Player, Object> method) {
		this.keyMethod = method;
	}
	
	/**
	 * Construct the map that will store the associated values.
	 * <p>
	 * The default implementation uses a {@link ConcurrentHashMap}.
	 * @return The value map.
	 */
	private ConcurrentMap<Object, TValue> createValueMap() {
		return new ConcurrentHashMap<>();
	}
	
	/**
	 * Construct a cache of keys and the associated player.
	 * @return The key map.
	 */
	private ConcurrentMap<Object, Player> createKeyCache() {
		return SafeCacheBuilder.newBuilder().
			weakValues().
			removalListener(
					(RemovalListener<Object, Player>) removed -> {
						// We ignore explicit removal
						if (removed.wasEvicted()) {
							onCacheEvicted(removed.getKey());
						}
					}).
			build(
			  new CacheLoader<Object, Player>() {
				@Override
				public Player load(Object key) throws Exception {
					Player player = findOnlinePlayer(key);
					
					if (player != null)
						return player;
					
					// Per the contract, this method should not return NULL
					throw new IllegalArgumentException(
							"Unable to find a player associated with: " + key);
				}
			});
	}
	
	/**
	 * Invoked when an entry in the cache has been evicted, typically by the garbage collector.
	 * @param key - the key.
	 */
	private void onCacheEvicted(Object key) {
		Player newPlayer = findOnlinePlayer(key);
		
		if (newPlayer != null) {
			// Update the reference
			keyLookup.put(key, newPlayer);
		} else {
			valueLookup.remove(key);
		}
	}
	
	/**
	 * Find an online player from the given key.
	 * @param key - a non-null key.
	 * @return The player with the given key, or NULL if not found.
	 */
	private Player findOnlinePlayer(Object key) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (key.equals(keyMethod.apply(player))) {
				return player;
			}
		}
		return null;
	}

	/**
	 * Lookup a player by key in the cache, optionally searching every online player.
	 * @param key - the key of the player we are locating.
	 * @return The player, or NULL if not found.
	 */
	private Player lookupPlayer(Object key) {
		try {
			return keyLookup.get(key);
		} catch (UncheckedExecutionException e) {
			return null;
		}
	}
	
	/**
	 * Retrieve the key of a particular player, ensuring it is cached.
	 * @param player - the player whose key we want to retrieve.
	 * @return The key.
	 */
	private Object cachePlayerKey(Player player) {
		Preconditions.checkNotNull(player, "player cannot be null");

		Object key = keyMethod.apply(player);
		
		keyLookup.put(key, player);
		return key;
	}
	
	@Override
	public TValue put(Player key, TValue value) {
		return valueLookup.put(cachePlayerKey(key), value);
	}
	
	@Override
	public TValue putIfAbsent(Player key, TValue value) {
		return valueLookup.putIfAbsent(cachePlayerKey(key), value);
	}
	
	@Override
	public TValue replace(Player key, TValue value) {
		return valueLookup.replace(cachePlayerKey(key), value);
	}
	
	@Override
	public boolean replace(Player key, TValue oldValue, TValue newValue) {
		return valueLookup.replace(cachePlayerKey(key), oldValue, newValue);
	}

	@Override
	public TValue remove(Object key) {
		if (key instanceof Player) {
			Object playerKey = keyMethod.apply((Player) key);
			
			if (playerKey != null) {
				TValue value = valueLookup.remove(playerKey);
			
				keyLookup.remove(playerKey);
				return value;
			}
		}
		return null;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		if (key instanceof Player) {
			Object playerKey = keyMethod.apply((Player) key);
			
			if (playerKey != null && valueLookup.remove(playerKey, value)) {
				keyLookup.remove(playerKey);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public TValue get(Object key) {
		if (key instanceof Player) {
			Object playerKey = keyMethod.apply((Player) key);
			return playerKey != null ? valueLookup.get(playerKey) : null;
		}
		return null;
	}
	
	@Override
	public boolean containsKey(Object key) {
		if (key instanceof Player) {
			Object playerKey = keyMethod.apply((Player) key);
			return playerKey != null && valueLookup.containsKey(playerKey);
		}
		return false;
	}
		
	@Override
	public Set<Entry<Player, TValue>> entrySet() {
		return new AbstractSet<Entry<Player,TValue>>() {
			@Override
			public Iterator<Entry<Player, TValue>> iterator() {
				return entryIterator();
			}
		
			@Override
			public int size() {
				return valueLookup.size();
			}
			
			@Override
			public void clear() {
				valueLookup.clear();
				keyLookup.clear();
			}
		};
	}

	/**
	 * Retrieve an iterator of entries that supports removal of elements.
	 * @return Entry iterator.
	 */
	private Iterator<Entry<Player, TValue>> entryIterator() {
		// Skip entries with stale data
		final Iterator<Entry<Object, TValue>> source = valueLookup.entrySet().iterator();
		final AbstractIterator<Entry<Player,TValue>> filtered =
		  new AbstractIterator<Entry<Player,TValue>>() {
			@Override
			protected Entry<Player, TValue> computeNext() {
				while (source.hasNext()) {
					Entry<Object, TValue> entry = source.next();
					Player player = lookupPlayer(entry.getKey());
					
					if (player == null) {
						// Remove entries that cannot be found
						source.remove();
						keyLookup.remove(entry.getKey());
					} else {
						return new SimpleEntry<Player, TValue>(player, entry.getValue());
					}
				}
				return endOfData();
			}
		};
		
		// We can't return AbstractIterator directly, as it doesn't permitt the remove() method
		return new Iterator<Entry<Player, TValue>>() {
			@Override
			public boolean hasNext() {
				return filtered.hasNext();
			}
			@Override
			public Entry<Player, TValue> next() {
				return filtered.next();
			}
			@Override
			public void remove() {
				source.remove();
			}
		};
	}
}
