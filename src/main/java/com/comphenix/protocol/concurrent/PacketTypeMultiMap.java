package com.comphenix.protocol.concurrent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * A map-like data structure that associates {@link PacketType}s with sets of
 * values. The values are stored in a {@link SortedCopyOnWriteSet}, which
 * ensures that the elements are kept in a sorted order based on the
 * {@link ListenerPriority} of the {@link ListeningWhitelist}, while maintaining
 * their insertion order for elements with equal priorities.
 * <p>
 * This class is thread-safe for modifications and guarantees a
 * modification-free iteration of associated values per packet type. All read methods
 * work on a lock-free, best-effort principle, ensuring fast access.
 * </p>
 *
 * @param <T> the type of elements maintained by this map
 */
public class PacketTypeMultiMap<T> {

	private final Map<PacketType, SortedCopyOnWriteSet<T, PriorityHolder>> typeMap = new HashMap<>();

	/**
	 * Adds a value to the map, associating it with the {@link PacketType}s
	 * contained in the specified {@link ListeningWhitelist}. If the value is
	 * already present in the set (as determined by {@code equals}), it will not be
	 * added again.
	 *
	 * @param key   the whitelist containing the packet types to associate the value
	 *              with
	 * @param value the value to be added
	 * @throws NullPointerException if the key or value is null
	 */
	public synchronized void put(ListeningWhitelist key, T value) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(value, "value cannot be null");

		for (PacketType packetType : key.getTypes()) {
			this.typeMap.computeIfAbsent(packetType, type -> new SortedCopyOnWriteSet<>()).add(value,
					new PriorityHolder(key));
		}
	}

	/**
	 * Removes a value from the map, disassociating it from the {@link PacketType}s
	 * contained in the specified {@link ListeningWhitelist}. If the value is not
	 * present, the map remains unchanged.
	 *
	 * @param key   the whitelist containing the packet types to disassociate the
	 *              value from
	 * @param value the value to be removed
	 * @return a list of packet types that got removed because they don't have any
	 *         associated values anymore
	 * @throws NullPointerException if the key or value is null
	 */
	public synchronized List<PacketType> remove(ListeningWhitelist key, T value) {
		Objects.requireNonNull(key, "key cannot be null");
		Objects.requireNonNull(value, "value cannot be null");

		List<PacketType> removedTypes = new ArrayList<>();

		for (PacketType packetType : key.getTypes()) {
			SortedCopyOnWriteSet<T, PriorityHolder> entrySet = this.typeMap.get(packetType);
			if (entrySet == null) {
				continue;
			}

			// we shouldn't have empty entrySets
			assert !entrySet.isEmpty();

			// continue if value wasn't removed
			if (!entrySet.remove(value)) {
				continue;
			}

			// remove packet type without entries
			if (entrySet.isEmpty()) {
				this.typeMap.remove(packetType);
				removedTypes.add(packetType);
			}
		}
		
		return removedTypes;
	}

	/**
	 * Returns an immutable set of all {@link PacketType}s currently in the map.
	 *
	 * @return an immutable set of packet types
	 */
	public ImmutableSet<PacketType> getPacketTypes() {
		return ImmutableSet.copyOf(this.typeMap.keySet());
	}

	/**
	 * Checks if a specified {@link PacketType} is contained in the map.
	 *
	 * @param packetType the packet type to check for
	 * @return {@code true} if the packet type is contained in the map,
	 *         {@code false} otherwise
	 */
	public boolean contains(PacketType packetType) {
		return this.typeMap.containsKey(packetType);
	}

	/**
	 * Returns an iterable of values associated with a specified {@link PacketType}.
	 * If no values are associated with the packet type, an empty iterator is
	 * returned.
	 *
	 * @param packetType the packet type to retrieve values for
	 * @return an iterable of values associated with the packet type
	 */
	public Iterable<T> get(PacketType packetType) {
		return () -> {
			SortedCopyOnWriteSet<T, PriorityHolder> entrySet = this.typeMap.get(packetType);

			if (entrySet != null) {
				return entrySet.iterator();
			}

			return Collections.emptyIterator();
		};
	}

    public Iterable<T> values() {
        return Iterables.concat(this.typeMap.values());
    }

	/**
	 * Clears all entries from the map.
	 */
	public synchronized void clear() {
		this.typeMap.clear();
	}

	/**
	 * A holder for priority information used to order elements within the
	 * {@link SortedCopyOnWriteSet}.
	 */
	private static class PriorityHolder implements Comparable<PriorityHolder> {

		private final ListenerPriority priority;

		public PriorityHolder(ListeningWhitelist key) {
			this.priority = key.getPriority();
		}

		@Override
		public int compareTo(PriorityHolder other) {
			return Integer.compare(this.priority.getSlot(), other.priority.getSlot());
		}
	}
}
