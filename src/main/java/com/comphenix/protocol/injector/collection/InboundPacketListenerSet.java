package com.comphenix.protocol.injector.collection;

import javax.annotation.Nullable;

import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedListenerManager.ListenerType;
import com.comphenix.protocol.timing.TimedTracker;

public class InboundPacketListenerSet extends PacketListenerSet {

	public InboundPacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
		super(mainThreadPacketTypes, errorReporter);
	}

	@Override
	protected ListeningWhitelist getListeningWhitelist(PacketListener packetListener) {
		return packetListener.getReceivingWhitelist();
	}

	/**
	 * Invokes the given packet event for every registered listener of the given
	 * priority.
	 * 
	 * @param event          - the packet event to invoke.
	 * @param priorityFilter - the required priority for a listener to be invoked.
	 */
	@Override
	public void invoke(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		Iterable<PacketListener> listeners = this.map.get(event.getPacketType());

		TimedListenerManager timedManager = TimedListenerManager.getInstance();
		if (timedManager.isTiming()) {
			for (PacketListener element : listeners) {
				if (priorityFilter == null || element.getReceivingWhitelist().getPriority() == priorityFilter) {
					TimedTracker tracker = timedManager.getTracker(element, ListenerType.SYNC_CLIENT_SIDE);
					long token = tracker.beginTracking();

					// Measure and record the execution time
					invokeReceivingListener(event, element);
					tracker.endTracking(token, event.getPacketType());
				}
			}
		} else {
			for (PacketListener element : listeners) {
				if (priorityFilter == null || element.getReceivingWhitelist().getPriority() == priorityFilter) {
					invokeReceivingListener(event, element);
				}
			}
		}
	}

	/**
	 * Invoke a particular receiving listener.
	 * 
	 * @param reporter - the error reporter.
	 * @param event    - the related packet event.
	 * @param listener - the listener to invoke.
	 */
	private void invokeReceivingListener(PacketEvent event, PacketListener listener) {
		try {
			event.setReadOnly(listener.getReceivingWhitelist().getPriority() == ListenerPriority.MONITOR);
			listener.onPacketReceiving(event);
		} catch (OutOfMemoryError | ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			errorReporter.reportMinimal(listener.getPlugin(), "onPacketReceiving(PacketEvent)", e,
					event.getPacket().getHandle());
		}
	}
}
