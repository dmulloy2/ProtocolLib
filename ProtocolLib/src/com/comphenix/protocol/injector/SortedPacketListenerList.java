package com.comphenix.protocol.injector;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * Registry of synchronous packet listeners.
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
		Collection<PrioritizedListener<PacketListener>> list = getListener(event.getPacketID());
		
		if (list == null)
			return;

		// The returned list is thread-safe
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
	
	/**
	 * Invokes the given packet event for every registered listener.
	 * @param logger - the logger that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketSending(Logger logger, PacketEvent event) {
		Collection<PrioritizedListener<PacketListener>> list = getListener(event.getPacketID());
		
		if (list == null)
			return;
		
		for (PrioritizedListener<PacketListener> element : list) {
			try {
				element.getListener().onPacketSending(event);
			} catch (Throwable e) {
				// Minecraft doesn't want your Exception.
				logger.log(Level.SEVERE, 
						"Exception occured in onPacketSending() for " + 
							PacketAdapter.getPluginName(element.getListener()), e);
			}
		}
	}
	
}
