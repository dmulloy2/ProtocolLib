package com.comphenix.protocol.injector.spigot;

import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Sets;

/**
 * Dummy packet injector that simply delegates to its parent Spigot packet injector or receiving filters.
 * 
 * @author Kristian
 */
class DummyPacketInjector extends AbstractPacketInjector {
	private SpigotPacketInjector injector;	
	private PacketTypeSet lastBufferedPackets = new PacketTypeSet();

	public DummyPacketInjector(SpigotPacketInjector injector, PacketTypeSet reveivedFilters) {
		super(reveivedFilters);
		this.injector = injector;
	}

	@Override
	public void inputBuffersChanged(Set<PacketType> set) {
		Set<PacketType> removed = Sets.difference(lastBufferedPackets.values(), set);
		Set<PacketType> added = Sets.difference(set, lastBufferedPackets.values());
		
		// Update the proxy packet injector
		for (PacketType packet : removed) {
			injector.getProxyPacketInjector().removePacketHandler(packet);
		}
		for (PacketType packet : added) {
			injector.getProxyPacketInjector().addPacketHandler(packet, null);
		}
	}

	@Override
	public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
		return injector.packetReceived(packet, client, buffered);
	}
}
