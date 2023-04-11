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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.concurrency.AbstractConcurrentListenerMultimap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
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
	private void invokeReceivingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener<PacketListener> element) {
		try {
			event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
			element.getListener().onPacketReceiving(event);
			
		} catch (OutOfMemoryError | ThreadDeath e) {
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
	 * @param priorityFilter - the priority for a listener to be invoked. If null is provided, every registered listener will be invoked
	 */
	public void invokePacketSending(ErrorReporter reporter, PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		invokeUnpackedPacketSending(reporter, event, priorityFilter);
		if (event.getPacketType() == PacketType.Play.Server.BUNDLE && !event.isCancelled()) {
			// unpack the bundle and invoke for each packet in the bundle
			Iterable<PacketContainer> packets = event.getPacket().getPacketBundles().read(0);
			List<PacketContainer> outPackets = new ArrayList<>();
			for (PacketContainer subPacket : packets) {
				if(subPacket == null) {
					ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING, "Failed to invoke packet event " + (priorityFilter == null ? "" : ("with priority " + priorityFilter)) + " in bundle because bundle contains null packet: " + packets, new Throwable());
					continue;
				}
				PacketEvent subPacketEvent = PacketEvent.fromServer(this, subPacket, event.getNetworkMarker(), event.getPlayer());
				invokeUnpackedPacketSending(reporter, subPacketEvent, priorityFilter);

				if (!subPacketEvent.isCancelled()) {
					// if the packet event has been cancelled, the packet will be removed from the bundle
					PacketContainer packet = subPacketEvent.getPacket();
					if(packet == null) {
						ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING, "null packet container returned for " + subPacketEvent, new Throwable());
					} else if(packet.getHandle() == null) {
						ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING, "null packet handle returned for " + subPacketEvent, new Throwable());
					} else {
						outPackets.add(packet);
					}
				}
			}

			if (packets.iterator().hasNext()) {
				event.getPacket().getPacketBundles().write(0, outPackets);
			} else {
				// cancel entire packet if each individual packet has been cancelled
				event.setCancelled(true);
			}
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
	private void invokeSendingListener(ErrorReporter reporter, PacketEvent event, PrioritizedListener<PacketListener> element) {
		try {
			event.setReadOnly(element.getPriority() == ListenerPriority.MONITOR);
			element.getListener().onPacketSending(event);
			
		} catch (OutOfMemoryError | ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			reporter.reportMinimal(element.getListener().getPlugin(), "onPacketSending(PacketEvent)", e, 
					event.getPacket().getHandle());
		}
	}
}
