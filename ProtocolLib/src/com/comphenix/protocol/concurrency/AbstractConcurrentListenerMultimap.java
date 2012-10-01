package com.comphenix.protocol.concurrency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.injector.PrioritizedListener;
import com.google.common.collect.Iterables;

/**
 * A thread-safe implementation of a listener multimap.
 * 
 * @author Kristian
 */
public abstract class AbstractConcurrentListenerMultimap<TListener> {

	// The core of our map
	private ConcurrentMap<Integer, SortedCopyOnWriteArray<PrioritizedListener<TListener>>> listeners = 
		new ConcurrentHashMap<Integer, SortedCopyOnWriteArray<PrioritizedListener<TListener>>>();
	
	/**
	 * Adds a listener to its requested list of packet recievers.
	 * @param listener - listener with a list of packets to recieve notifcations for.
	 * @param whitelist - the packet whitelist to use.
	 */
	public void addListener(TListener listener, ListeningWhitelist whitelist) {
		
		PrioritizedListener<TListener> prioritized = new PrioritizedListener<TListener>(listener, whitelist.getPriority());
		
		for (Integer packetID : whitelist.getWhitelist()) {
			addListener(packetID, prioritized);
		}
	}
	
	// Add the listener to a specific packet notifcation list
	private void addListener(Integer packetID, PrioritizedListener<TListener> listener) {
		
		SortedCopyOnWriteArray<PrioritizedListener<TListener>> list = listeners.get(packetID);
		
		// We don't want to create this for every lookup
		if (list == null) {
			// It would be nice if we could use a PriorityBlockingQueue, but it doesn't preseve iterator order,
			// which is a essential feature for our purposes.
			final SortedCopyOnWriteArray<PrioritizedListener<TListener>> value = new SortedCopyOnWriteArray<PrioritizedListener<TListener>>();
			
			list = listeners.putIfAbsent(packetID, value);
			
			// We may end up creating multiple multisets, but we'll agree 
			// on the one to use.
			if (list == null) {
				list = value;
			}
		}
		
		// Thread safe
		list.add(listener);
	}
	
	/**
	 * Removes the given listener from the packet event list.
	 * @param listener - listener to remove.
	 * @param whitelist - the packet whitelist that was used.
	 * @return Every packet ID that was removed due to no listeners.
	 */
	public List<Integer> removeListener(TListener listener, ListeningWhitelist whitelist) {
	
		List<Integer> removedPackets = new ArrayList<Integer>();
		
		// Again, not terribly efficient. But adding or removing listeners should be a rare event.
		for (Integer packetID : whitelist.getWhitelist()) {
			
			SortedCopyOnWriteArray<PrioritizedListener<TListener>> list = listeners.get(packetID);
			
			// Remove any listeners
			if (list != null) {
				// Don't remove from newly created lists
				if (list.size() > 0) {
					// Remove this listener. Note that priority is generally ignored.
					list.remove(new PrioritizedListener<TListener>(listener, whitelist.getPriority()));
					
					if (list.size() == 0) {
						listeners.remove(packetID);
						removedPackets.add(packetID);
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
	 * @param packetID - packet ID.
	 * @return Registered listeners.
	 */
	public Collection<PrioritizedListener<TListener>> getListener(int packetID) {
		return listeners.get(packetID);
	}
	
	/**
	 * Retrieve every listener.
	 * @return Every listener.
	 */
	public Iterable<PrioritizedListener<TListener>> values() {
		return Iterables.concat(listeners.values());
	}
	
	/**
	 * Retrieve every registered packet ID:
	 * @return Registered packet ID.
	 */
	public Set<Integer> keySet() {
		return listeners.keySet();
	}
	
	/**
	 * Remove all packet listeners.
	 */
	protected void clearListeners() {
		listeners.clear();
	}
}
