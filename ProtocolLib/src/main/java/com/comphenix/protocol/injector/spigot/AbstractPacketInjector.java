package com.comphenix.protocol.injector.spigot;

import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.injector.packet.PacketInjector;

public abstract class AbstractPacketInjector implements PacketInjector {
	private PacketTypeSet reveivedFilters;
	
	public AbstractPacketInjector(PacketTypeSet reveivedFilters) {
		this.reveivedFilters = reveivedFilters;
	}

	@Override
	public boolean isCancelled(Object packet) {
		// No, it's never cancelled
		return false;
	}

	@Override
	public void setCancelled(Object packet, boolean cancelled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		reveivedFilters.addType(type);
		return true;
	}

	@Override
	public boolean removePacketHandler(PacketType type) {
		reveivedFilters.removeType(type);
		return true;
	}

	@Override
	public boolean hasPacketHandler(PacketType type) {
		return reveivedFilters.contains(type);
	}

	@Override
	public Set<PacketType> getPacketHandlers() {
		return reveivedFilters.values();
	}

	@Override
	public void cleanupAll() {
		reveivedFilters.clear();
	}
}