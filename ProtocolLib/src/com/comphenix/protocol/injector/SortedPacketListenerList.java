package com.comphenix.protocol.injector;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.concurrency.SortedCopyOnWriteArray;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * A thread-safe implementation of a listener multimap.
 * 
 * @author Kristian
 */
class SortedPacketListenerList extends AbstractConcurrentListenerMultimap<PacketListener> {
	
	/**
	 * Invokes the given packet event for every registered listener.
	 * @param logger - the logger that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketRecieving(Logger logger, PacketEvent event) {
		SortedCopyOnWriteArray<PrioritizedListener<PacketListener>> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		// We have to be careful. Cannot modify the underlying list when sending notifications.
		synchronized (list) {
			for (PrioritizedListener<PacketListener> element : list) {
				try {
					element.getListener().onPacketReceiving(event);
				} catch (Throwable e) {
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
		SortedCopyOnWriteArray<PrioritizedListener<PacketListener>> list = listeners.get(event.getPacketID());
		
		if (list == null)
			return;
		
		synchronized (list) {
			for (PrioritizedListener<PacketListener> element : list) {
				try {
					element.getListener().onPacketSending(event);
				} catch (Throwable e) {
					// Minecraft doesn't want your Exception.
					logger.log(Level.SEVERE, 
							"Exception occured in onPacketReceiving() for " + 
								PacketAdapter.getPluginName(element.getListener()), e);
				}
			}
		}	
	}
	
}
