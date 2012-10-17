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
