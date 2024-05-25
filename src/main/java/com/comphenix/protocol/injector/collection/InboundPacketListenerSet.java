package com.comphenix.protocol.injector.collection;

import com.comphenix.protocol.concurrent.PacketTypeListenerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

public class InboundPacketListenerSet extends PacketListenerSet {

	public InboundPacketListenerSet(PacketTypeListenerSet mainThreadPacketTypes, ErrorReporter errorReporter) {
		super(mainThreadPacketTypes, errorReporter);
	}

	@Override
	protected ListeningWhitelist getListeningWhitelist(PacketListener packetListener) {
		return packetListener.getReceivingWhitelist();
	}

	@Override
	protected void invokeListener(PacketEvent event, PacketListener listener) {
		try {
			event.setReadOnly(listener.getReceivingWhitelist().getPriority() == ListenerPriority.MONITOR);
			listener.onPacketReceiving(event);
		} catch (OutOfMemoryError | ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			errorReporter.reportMinimal(listener.getPlugin(), "onPacketReceiving(PacketEvent)", e,
					event.getPacket().getHandle());
		}
	}
}
