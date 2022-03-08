package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketListener;
import java.util.Set;

public abstract class AbstractPlayerInjectionHandler implements PlayerInjectionHandler {

	private final PacketTypeSet sendingFilters;

	public AbstractPlayerInjectionHandler(PacketTypeSet sendingFilters) {
		this.sendingFilters = sendingFilters;
	}

	@Override
	public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		this.sendingFilters.addType(type);
	}

	@Override
	public void removePacketHandler(PacketType type) {
		this.sendingFilters.removeType(type);
	}

	@Override
	public Set<PacketType> getSendingFilters() {
		return this.sendingFilters.values();
	}

	@Override
	public void close() {
		this.sendingFilters.clear();
	}

	@Override
	public boolean canReceivePackets() {
		return true;
	}

	@Override
	public void checkListener(PacketListener listener) {
		// They're all fine!
	}

	@Override
	public void checkListener(Set<PacketListener> listeners) {
		// Yes, really
	}
}
