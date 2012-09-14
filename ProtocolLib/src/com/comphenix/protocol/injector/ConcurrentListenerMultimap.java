package com.comphenix.protocol.injector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.primitives.Ints;

/**
 * A thread-safe implementation of a listener multimap.
 * 
 * @author Kristian
 */
public class ConcurrentListenerMultimap {

	// The core of our map
	protected ConcurrentMap<Integer, SortedCopyOnWriteArray<PrioritizedListener>> listeners = 
		  new ConcurrentHashMap<Integer, SortedCopyOnWriteArray<PrioritizedListener>>();
	
	/**
	 * Adds a listener to its requested list of packet recievers.
	 * @param listener - listener with a list of packets to recieve notifcations for.
	 */
	public void addListener(PacketListener listener, ListeningWhitelist whitelist) {
		
		PrioritizedListener prioritized = new PrioritizedListener(listener, whitelist.getPriority());
		
		for (Integer packetID : whitelist.getWhitelist()) {
			addListener(packetID, prioritized);
		}
	}
	
	// Add the listener to a specific packet notifcation list
	private void addListener(Integer packetID, PrioritizedListener listener) {
		
		SortedCopyOnWriteArray<PrioritizedListener> list = listeners.get(packetID);
		
		// We don't want to create this for every lookup
		if (list == null) {
			// It would be nice if we could use a PriorityBlockingQueue, but it doesn't preseve iterator order,
			// which is a essential feature for our purposes.
			final SortedCopyOnWriteArray<PrioritizedListener> value = new SortedCopyOnWriteArray<PrioritizedListener>();
			
			list = listeners.putIfAbsent(packetID, value);
			
			// We may end up creating multiple multisets, but we'll agree 
			// on the one to use.
			if (list == null) {
				list = value;
			}
		}
		
		// Careful when modifying the set
		synchronized(list) {
			list.insertSorted(listener);
		}
	}
	
	/**
	 * Removes the given listener from the packet event list.
	 * @param listener - listener to remove.
	 * @return Every packet ID that was removed due to no listeners.
	 */
	public List<Integer> removeListener(PacketListener listener, ListeningWhitelist whitelist) {
	
		List<Integer> removedPackets = new ArrayList<Integer>();
		
		// Again, not terribly efficient. But adding or removing listeners should be a rare event.
		for (Integer packetID : whitelist.getWhitelist()) {
			
			SortedCopyOnWriteArray<PrioritizedListener> list = listeners.get(packetID);
			
			// Remove any listeners
			if (list != null) {
				synchronized(list) {
					// Don't remove from newly created lists
					if (list.size() > 0) {
						// Remove this listener
						for (Iterator<PrioritizedListener> it = list.iterator(); it.hasNext(); ) {
							if (it.next().getListener().equals(list)) {
								it.remove();
							}
						}
						
						if (list.size() == 0) {
							listeners.remove(packetID);
							removedPackets.add(packetID);
						}
					}
				}
			}
			
			// Move on to the next
		}
		
		return removedPackets;
	}
	
	/**
	 * Invokes the given packet event for every registered listener.
	 * @param logger - the logger that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketRecieving(Logger logger, PacketEvent event) {
		SortedCopyOnWriteArray<PrioritizedListener> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		// We have to be careful. Cannot modify the underlying list when sending notifications.
		synchronized (list) {
			for (PrioritizedListener element : list) {
				try {
					element.getListener().onPacketReceiving(event);
				} catch (Exception e) {
					// Minecraft doesn't want your Exception.
					logger.log(Level.SEVERE, 
							"Exception occured in onPacketReceiving() for " + 
								PacketAdapter.getPluginName(element.getListener()), e);
				}
			}
		}
	}
	
	/**
	 * Invokes the given packet event for every registered listener.
	 * @param logger - the logger that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketSending(Logger logger, PacketEvent event) {
		SortedCopyOnWriteArray<PrioritizedListener> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		synchronized (list) {
			for (PrioritizedListener element : list) {
				try {
					element.getListener().onPacketSending(event);
				} catch (Exception e) {
					// Minecraft doesn't want your Exception.
					logger.log(Level.SEVERE, 
							"Exception occured in onPacketReceiving() for " + 
								PacketAdapter.getPluginName(element.getListener()), e);
				}
			}
		}	
	}
	
	/**
	 * A listener with an associated priority.
	 */
	private class PrioritizedListener implements Comparable<PrioritizedListener> {
		private PacketListener listener;
		private ListenerPriority priority;
		
		public PrioritizedListener(PacketListener listener, ListenerPriority priority) {
			this.listener = listener;
			this.priority = priority;
		}

		@Override
		public int compareTo(PrioritizedListener other) {
			// This ensures that lower priority listeners are executed first
			return Ints.compare(this.getPriority().getSlot(),
					            other.getPriority().getSlot());
		}
		
		public PacketListener getListener() {
			return listener;
		}

		public ListenerPriority getPriority() {
			return priority;
		}
	}
}
