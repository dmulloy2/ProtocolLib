package com.comphenix.protocol.injector.collection;

import java.util.Set;

import javax.annotation.Nullable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.concurrent.PacketTypeMultiMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.timing.TimingListenerType;
import com.comphenix.protocol.timing.TimingTrackerManager;
import com.google.common.collect.ImmutableSet;

public abstract class PacketListenerSet {

	private static final ReportType UNSUPPORTED_PACKET = new ReportType(
			"Plugin %s tried to register listener for unknown packet %s [direction: from %s]");

	protected final PacketTypeMultiMap<PacketListener> map = new PacketTypeMultiMap<>();

	protected final PacketTypeListenerSet mainThreadPacketTypes;
	protected final ErrorReporter errorReporter;

	public PacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
		this.mainThreadPacketTypes = mainThreadPacketTypes;
		this.errorReporter = errorReporter;
	}

	protected abstract ListeningWhitelist getListeningWhitelist(PacketListener packetListener);

	public void addListener(PacketListener packetListener) {
		ListeningWhitelist listeningWhitelist = getListeningWhitelist(packetListener);
		this.map.put(listeningWhitelist, packetListener);

		Set<ListenerOptions> options = listeningWhitelist.getOptions();
		for (PacketType packetType : listeningWhitelist.getTypes()) {
			if (this.mainThreadPacketTypes != null && !packetType.isAsyncForced()) {
				boolean isOutboundSync = packetType.getSender() == Sender.SERVER && !options.contains(ListenerOptions.ASYNC);
				boolean isInboundSync = packetType.getSender() == Sender.CLIENT && options.contains(ListenerOptions.SYNC);
				if (isOutboundSync || isInboundSync) {
					this.mainThreadPacketTypes.add(packetType, packetListener);
				}
			}

			Set<PacketType> supportedPacketTypes = (packetType.getSender() == Sender.SERVER)
					? PacketRegistry.getServerPacketTypes()
					: PacketRegistry.getClientPacketTypes();

			if (!supportedPacketTypes.contains(packetType)) {
				this.errorReporter.reportWarning(this, Report.newBuilder(UNSUPPORTED_PACKET)
						.messageParam(PacketAdapter.getPluginName(packetListener), packetType, packetType.getSender())
						.build());
			}
		}
	}

	public void removeListener(PacketListener packetListener) {
		ListeningWhitelist listeningWhitelist = getListeningWhitelist(packetListener);
		this.map.remove(listeningWhitelist, packetListener);

		if (this.mainThreadPacketTypes != null) {
			for (PacketType packetType : listeningWhitelist.getTypes()) {
				this.mainThreadPacketTypes.remove(packetType, packetListener);
			}
		}
	}

	public final boolean containsPacketType(PacketType packetType) {
		return this.map.contains(packetType);
	}

	public final ImmutableSet<PacketType> getPacketTypes() {
		return this.map.getPacketTypes();
	}

	public void invoke(PacketEvent event) {
		this.invoke(event, null);
	}

	public void invoke(PacketEvent event, @Nullable ListenerPriority priorityFilter) {
		Iterable<PacketListener> listeners = this.map.get(event.getPacketType());

		for (PacketListener listener : listeners) {
			ListeningWhitelist listeningWhitelist = listener.getReceivingWhitelist();
			if (priorityFilter != null && listeningWhitelist.getPriority() != priorityFilter) {
				continue;
			}

			TimingTrackerManager.get(listener, event.isServerPacket() ? TimingListenerType.SYNC_OUTBOUND : TimingListenerType.SYNC_INBOUND)
				.track(event.getPacketType(), () -> invokeListener(event, listener));
		}
	}

	protected abstract void invokeListener(PacketEvent event, PacketListener listener);

	public void clear() {
		this.map.clear();
	}
}
