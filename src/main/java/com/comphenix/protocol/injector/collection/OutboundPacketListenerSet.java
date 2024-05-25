package com.comphenix.protocol.injector.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.timing.TimedListenerManager;
import com.comphenix.protocol.timing.TimedListenerManager.ListenerType;
import com.comphenix.protocol.timing.TimedTracker;

public class OutboundPacketListenerSet extends PacketListenerSet {

	public OutboundPacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
		super(mainThreadPacketTypes, errorReporter);
	}

	@Override
	protected ListeningWhitelist getListeningWhitelist(PacketListener packetListener) {
		return packetListener.getSendingWhitelist();
	}

	/**
	 * Invokes the given packet event for every registered listener of the given
	 * priority.
	 * 
	 * @param event          - the packet event to invoke.
	 * @param priorityFilter - the priority for a listener to be invoked. If null is
	 *                       provided, every registered listener will be invoked
	 */
	@Override
	public void invoke(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		invokeUnpackedPacketSending(event, priorityFilter);
		if (event.getPacketType() == PacketType.Play.Server.BUNDLE && !event.isCancelled()) {
			// unpack the bundle and invoke for each packet in the bundle
			Iterable<PacketContainer> packets = event.getPacket().getPacketBundles().read(0);
			List<PacketContainer> outPackets = new ArrayList<>();
			for (PacketContainer subPacket : packets) {
				if (subPacket == null) {
					ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING,
							"Failed to invoke packet event "
									+ (priorityFilter == null ? "" : ("with priority " + priorityFilter))
									+ " in bundle because bundle contains null packet: " + packets,
							new Throwable());
					continue;
				}
				PacketEvent subPacketEvent = PacketEvent.fromServer(this, subPacket, event.getNetworkMarker(),
						event.getPlayer());
				invokeUnpackedPacketSending(subPacketEvent, priorityFilter);

				if (!subPacketEvent.isCancelled()) {
					// if the packet event has been cancelled, the packet will be removed from the
					// bundle
					PacketContainer packet = subPacketEvent.getPacket();
					if (packet == null) {
						ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING,
								"null packet container returned for " + subPacketEvent, new Throwable());
					} else if (packet.getHandle() == null) {
						ProtocolLibrary.getPlugin().getLogger().log(Level.WARNING,
								"null packet handle returned for " + subPacketEvent, new Throwable());
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

	private void invokeUnpackedPacketSending(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		Iterable<PacketListener> listeners = this.map.get(event.getPacketType());

		TimedListenerManager timedManager = TimedListenerManager.getInstance();
		if (timedManager.isTiming()) {
			for (PacketListener element : listeners) {
				if (priorityFilter == null || element.getSendingWhitelist().getPriority() == priorityFilter) {
					TimedTracker tracker = timedManager.getTracker(element, ListenerType.SYNC_SERVER_SIDE);
					long token = tracker.beginTracking();

					// Measure and record the execution time
					invokeSendingListener(event, element);
					tracker.endTracking(token, event.getPacketType());
				}
			}
		} else {
			for (PacketListener element : listeners) {
				if (priorityFilter == null || element.getSendingWhitelist().getPriority() == priorityFilter) {
					invokeSendingListener(event, element);
				}
			}
		}
	}

	/**
	 * Invoke a particular sending listener.
	 * 
	 * @param reporter - the error reporter.
	 * @param event    - the related packet event.
	 * @param listener - the listener to invoke.
	 */
	private void invokeSendingListener(PacketEvent event, PacketListener listener) {
		try {
			event.setReadOnly(listener.getSendingWhitelist().getPriority() == ListenerPriority.MONITOR);
			listener.onPacketSending(event);
		} catch (OutOfMemoryError | ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			// Minecraft doesn't want your Exception.
			errorReporter.reportMinimal(listener.getPlugin(), "onPacketSending(PacketEvent)", e,
					event.getPacket().getHandle());
		}
	}
}
