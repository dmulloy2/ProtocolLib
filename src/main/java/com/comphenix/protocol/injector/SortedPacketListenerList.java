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
import java.util.Iterator;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedListenerManager.ListenerType;
import com.comphenix.protocol.timing.TimedTracker;

import javax.annotation.Nullable;

/**
 * Registry of synchronous packet listeners.
 * 
 * @author Kristian
 */
public final class SortedPacketListenerList extends AbstractConcurrentListenerMultimap<PacketListener> {
	// The current listener manager
	private TimedListenerManager timedManager = TimedListenerManager.getInstance();
	
	public SortedPacketListenerList() {
		super();
	}

	/**
	 * Invokes the given packet event for every registered listener.
	 * @param reporter - the error reporter that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketRecieving(ErrorReporter reporter, PacketEvent event) {
		Collection<PrioritizedListener<PacketListener>> list = getListener(event.getPacketType());
		
		if (list == null)
			return;

		// The returned list is thread-safe
		if (timedManager.isTiming()) {
			for (PrioritizedListener<PacketListener> element : list) {
				TimedTracker tracker = timedManager.getTracker(element.getListener(), ListenerType.SYNC_CLIENT_SIDE);
				long token = tracker.beginTracking();
				
				// Measure and record the execution time
				invokeReceivingListener(reporter, event, element);
				tracker.endTracking(token, event.getPacketType());
			}
		} else {
			for (PrioritizedListener<PacketListener> element : list) {
				invokeReceivingListener(reporter, event, element);
			}
		}
	}
	
	/**
	 * Invokes the given packet event for every registered listener of the given priority.
	 * @param reporter - the error reporter that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 * @param priorityFilter - the required priority for a listener to be invoked.
	 */
	public void invokePacketRecieving(ErrorReporter reporter, PacketEvent event, ListenerPriority priorityFilter) {
		Collection<PrioritizedListener<PacketListener>> list = getListener(event.getPacketType());
		
		if (list == null)
			return;

		// The returned list is thread-safe
		if (timedManager.isTiming()) {
			for (PrioritizedListener<PacketListener> element : list) {
				if (element.getPriority() == priorityFilter) {
					TimedTracker tracker = timedManager.getTracker(element.getListener(), ListenerType.SYNC_CLIENT_SIDE);
					long token = tracker.beginTracking();
					
					// Measure and record the execution time
					invokeReceivingListener(reporter, event, element);
					tracker.endTracking(token, event.getPacketType());
				}
			}
		} else {
			for (PrioritizedListener<PacketListener> element : list) {
				if (element.getPriority() == priorityFilter) {
					invokeReceivingListener(reporter, event, element);
				}
			}
		}
	}
	
	/**
	 * Invoke a particular receiving listener.
	 * @param reporter - the error reporter.
	 * @param event - the related packet event.
	 * @param element - the listener to invoke.
	 */
	private final void invokeReceivingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener<PacketListener> element) {
		try {
			event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
			element.getListener().onPacketReceiving(event);
			
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			reporter.reportMinimal(element.getListener().getPlugin(), "onPacketReceiving(PacketEvent)", e, 
					event.getPacket().getHandle());
		}
	}
	
	/**
	 * Invokes the given packet event for every registered listener.
	 * @param reporter - the error reporter that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 */
	public void invokePacketSending(ErrorReporter reporter, PacketEvent event) {
		invokePacketSending(reporter, event, null);
	}
	
	/**
	 * Invokes the given packet event for every registered listener of the given priority.
	 * @param reporter - the error reporter that will be used to inform about listener exceptions.
	 * @param event - the packet event to invoke.
	 * @param priorityFilter - the piority for a listener to be invoked. If null is provided, every registered listener will be invoked
	 */
	public void invokePacketSending(ErrorReporter reporter, PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		if(event.getPacketType() == PacketType.Play.Server.DELIMITER) {
			// unpack the bundle and invoke for each packet in the bundle
			Iterable packets = event.getPacket().getSpecificModifier(Iterable.class).read(0);
			Iterator iterator = packets.iterator();
			while (iterator.hasNext()) {
				Object handle = iterator.next();
				PacketType packetType = PacketRegistry.getPacketType(handle.getClass());
				PacketContainer container = new PacketContainer(packetType, handle);
				PacketEvent packetEvent = PacketEvent.fromServer(this, container, event.getNetworkMarker(), event.getPlayer());
				invokeUnpackedPacketSending(reporter, packetEvent, priorityFilter);

				if(packetEvent.isCancelled()) {
					iterator.remove();
				}
			}
			if(!packets.iterator().hasNext()) { // are there still packets in this bundle?
				event.setCancelled(true);
			} else {
				event.getPacket().getSpecificModifier(Iterable.class).write(0, packets);
			}
		} else {
			invokeUnpackedPacketSending(reporter, event, priorityFilter);
		}
	}

	private void invokeUnpackedPacketSending(ErrorReporter reporter, PacketEvent event, @org.jetbrains.annotations.Nullable ListenerPriority priorityFilter) {
		Collection<PrioritizedListener<PacketListener>> list = getListener(event.getPacketType());

		if (list == null)
			return;

		if (timedManager.isTiming()) {
			for (PrioritizedListener<PacketListener> element : list) {
				if (priorityFilter == null || element.getPriority() == priorityFilter) {
					TimedTracker tracker = timedManager.getTracker(element.getListener(), ListenerType.SYNC_SERVER_SIDE);
					long token = tracker.beginTracking();

					// Measure and record the execution time
					invokeSendingListener(reporter, event, element);
					tracker.endTracking(token, event.getPacketType());
				}
			}
		} else {
			for (PrioritizedListener<PacketListener> element : list) {
				if (priorityFilter == null || element.getPriority() == priorityFilter) {
					invokeSendingListener(reporter, event, element);
				}
			}
		}
	}

	/**
	 * Invoke a particular sending listener.
	 * @param reporter - the error reporter.
	 * @param event - the related packet event.
	 * @param element - the listener to invoke.
	 */
	private final void invokeSendingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener<PacketListener> element) {
		try {
			event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
			element.getListener().onPacketSending(event);
			
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			reporter.reportMinimal(element.getListener().getPlugin(), "onPacketSending(PacketEvent)", e, 
					event.getPacket().getHandle());
		}
	}
}
