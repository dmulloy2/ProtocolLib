package com.comphenix.protocol.injector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.base.Objects;
import com.google.common.primitives.Ints;

/**
 * A thread-safe implementation of a listener multimap.
 * 
 * @author Kristian
 */
public class ConcurrentListenerMultimap {

	// The core of our map
	protected ConcurrentMap<Integer, SortedArrayList<PacketListener>> listeners = 
		  new ConcurrentHashMap<Integer, SortedArrayList<PacketListener>>();
	
	/**
	 * Adds a listener to its requested list of packet recievers.
	 * @param listener - listener with a list of packets to recieve notifcations for.
	 */
	public void addListener(PacketListener listener) {
		for (Integer packetID : listener.getPacketsID()) {
			addListener(packetID, listener);
		}
	}
	
	// Add the listener to a specific packet notifcation list
	private void addListener(Integer packetID, PacketListener listener) {
		
		SortedArrayList<PacketListener> list = listeners.get(packetID);
		
		// We don't want to create this for every lookup
		if (list == null) {
			// It would be nice if we could use a PriorityBlockingQueue, but it doesn't preseve iterator order,
			// which is a essential feature for our purposes.
			final SortedArrayList<PacketListener> value = new SortedArrayList<PacketListener>(new Comparator<PacketListener>() {
				@Override
				public int compare(PacketListener o1, PacketListener o2) {
					// This ensures that lower priority listeners are executed first
					return Ints.compare(o1.getListenerPriority().getSlot(),
							            o2.getListenerPriority().getSlot());
				}
			});
			
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
	public List<Integer> removeListener(PacketListener listener) {
	
		List<Integer> removedPackets = new ArrayList<Integer>();
		
		// Again, not terribly efficient. But adding or removing listeners should be a rare event.
		for (Integer packetID : listener.getPacketsID()) {
			
			SortedArrayList<PacketListener> list = listeners.get(packetID);
			
			// Remove any listeners
			if (list != null) {
				synchronized(list) {
					// Don't remove from newly created lists
					if (list.size() > 0) {
						list.removeAll(listener);
						
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
		SortedArrayList<PacketListener> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		// We have to be careful. Cannot modify the underlying list when sending notifications.
		synchronized (list) {
			for (PacketListener listener : list) {
				try {
					listener.onPacketReceiving(event);
				} catch (Exception e) {
					// Minecraft doesn't want your Exception.
					logger.log(Level.SEVERE, 
							"Exception occured in onPacketReceiving() for " + PacketAdapter.getPluginName(listener), e);
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
		SortedArrayList<PacketListener> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		synchronized (list) {
			for (PacketListener listener : list) {
				try {
					listener.onPacketSending(event);
				} catch (Exception e) {
					// Minecraft doesn't want your Exception.
					logger.log(Level.SEVERE, 
							"Exception occured in onPacketReceiving() for " + PacketAdapter.getPluginName(listener), e);
				}
			}
		}	
	}
	
	/**
	 * An implicitly sorted array list that preserves insertion order and maintains duplicates.
	 * 
	 * Note that only the {@link insertSorted} method will update the list correctly,
	 * @param <T> - type of the sorted list.
	 */
	private class SortedArrayList<T> implements Iterable<T> {

		private Comparator<T> comparator;
		private List<T> list = new ArrayList<T>();
		
		public SortedArrayList(Comparator<T> comparator) {
			this.comparator = comparator;
		}
		
		/**
		 * Inserts the given element in the proper location.
		 * @param value - element to insert.
		 */
	    public void insertSorted(T value) {
	    	list.add(value);
	        for (int i = list.size() - 1; i > 0 && comparator.compare(value, list.get(i-1)) < 0; i--) {
	            T tmp = list.get(i);
	            list.set(i, list.get(i-1));
	            list.set(i-1, tmp);
	        }
	    }
	    
	    /**
	     * Removes every instance of the given element.
	     * @param element - element to remove.
	     */
	    public void removeAll(T element) {
			for (Iterator<T> it = list.iterator(); it.hasNext(); ) {
				if (Objects.equal(it.next(), element)) {
					it.remove();
				}
			}
	    }
	    
	    /**
	     * Retrieve the size of the list.
	     * @return Size of the list.
	     */
	    public int size() {
	    	return list.size();
	    }

		@Override
		public Iterator<T> iterator() {
			return list.iterator();
		}
	}
}
