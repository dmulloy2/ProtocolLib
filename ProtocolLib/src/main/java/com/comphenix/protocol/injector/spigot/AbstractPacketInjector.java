package com.comphenix.protocol.injector.spigot;

import java.util.Set;

import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.injector.packet.PacketInjector;

public abstract class AbstractPacketInjector implements PacketInjector {
	private IntegerSet reveivedFilters;
	
	public AbstractPacketInjector(IntegerSet reveivedFilters) {
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
	public boolean addPacketHandler(int packetID) {
		reveivedFilters.add(packetID);
		return true;
	}

	@Override
	public boolean removePacketHandler(int packetID) {
		reveivedFilters.remove(packetID);
		return true;
	}

	@Override
	public boolean hasPacketHandler(int packetID) {
		return reveivedFilters.contains(packetID);
	}

	@Override
	public Set<Integer> getPacketHandlers() {
		return reveivedFilters.toSet();
	}

	@Override
	public void cleanupAll() {
		reveivedFilters.clear();
	}
}