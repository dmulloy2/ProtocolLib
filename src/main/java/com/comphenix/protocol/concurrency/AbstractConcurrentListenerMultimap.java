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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe implementation of a listener multimap.
 *
 * @author Kristian
 */
public abstract class AbstractConcurrentListenerMultimap<T> {

	// The core of our map
	private final ConcurrentMap<PacketType, SortedCopyOnWriteArray<PrioritizedListener<T>>> mapListeners;

	public AbstractConcurrentListenerMultimap() {
		this.mapListeners = new ConcurrentHashMap<>();
	}

	/**
	 * Adds a listener to its requested list of packet receivers.
	 *
	 * @param listener  - listener with a list of packets to receive notifications for.
	 * @param whitelist - the packet whitelist to use.
	 */
	public void addListener(T listener, ListeningWhitelist whitelist) {
		PrioritizedListener<T> prioritized = new PrioritizedListener<>(listener, whitelist.getPriority());
		for (PacketType type : whitelist.getTypes()) {
			this.addListener(type, prioritized);
		}
	}

	// Add the listener to a specific packet notifcation list
	private void addListener(PacketType type, PrioritizedListener<T> listener) {
		SortedCopyOnWriteArray<PrioritizedListener<T>> list = this.mapListeners.get(type);

		// We don't want to create this for every lookup
		if (list == null) {
			// It would be nice if we could use a PriorityBlockingQueue, but it doesn't preseve iterator order,
			// which is a essential feature for our purposes.
			final SortedCopyOnWriteArray<PrioritizedListener<T>> value = new SortedCopyOnWriteArray<PrioritizedListener<T>>();

			// We may end up creating multiple multisets, but we'll agree on which to use
			list = this.mapListeners.putIfAbsent(type, value);

			if (list == null) {
				list = value;
			}
		}

		// Thread safe
		list.add(listener);
	}

	/**
	 * Removes the given listener from the packet event list.
	 *
	 * @param listener  - listener to remove.
	 * @param whitelist - the packet whitelist that was used.
	 * @return Every packet ID that was removed due to no listeners.
	 */
	public List<PacketType> removeListener(T listener, ListeningWhitelist whitelist) {
		List<PacketType> removedPackets = new ArrayList<PacketType>();

		// Again, not terribly efficient. But adding or removing listeners should be a rare event.
		for (PacketType type : whitelist.getTypes()) {
			SortedCopyOnWriteArray<PrioritizedListener<T>> list = this.mapListeners.get(type);

			// Remove any listeners
			if (list != null) {
				// Don't remove from newly created lists
				if (list.size() > 0) {
					// Remove this listener. Note that priority is generally ignored.
					list.remove(new PrioritizedListener<T>(listener, whitelist.getPriority()));

					if (list.size() == 0) {
						this.mapListeners.remove(type);
						removedPackets.add(type);
					}
				}
			}
			// Move on to the next
		}
		return removedPackets;
	}

	/**
	 * Retrieve the registered listeners, in order from the lowest to the highest priority.
	 * <p>
	 * The returned list is thread-safe and doesn't require synchronization.
	 *
	 * @param type - packet type.
	 * @return Registered listeners.
	 */
	public Collection<PrioritizedListener<T>> getListener(PacketType type) {
		return this.mapListeners.get(type);
	}

	/**
	 * Retrieve every listener.
	 *
	 * @return Every listener.
	 */
	public Iterable<PrioritizedListener<T>> values() {
		return Iterables.concat(this.mapListeners.values());
	}

	/**
	 * Retrieve every registered packet type:
	 *
	 * @return Registered packet type.
	 */
	public Set<PacketType> keySet() {
		return this.mapListeners.keySet();
	}

	/**
	 * Remove all packet listeners.
	 */
	protected void clearListeners() {
		this.mapListeners.clear();
	}
}
