package com.comphenix.protocol.injector.spigot;

import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.google.common.collect.Sets;

/**
 * Dummy packet injector that simply delegates to its parent Spigot packet injector or receiving filters.
 * 
 * @author Kristian
 */
class DummyPacketInjector extends AbstractPacketInjector implements PacketInjector {
	private SpigotPacketInjector injector;	
	private IntegerSet lastBufferedPackets = new IntegerSet(Packets.MAXIMUM_PACKET_ID + 1);

	public DummyPacketInjector(SpigotPacketInjector injector, IntegerSet reveivedFilters) {
		super(reveivedFilters);
		this.injector = injector;
	}

	@Override
	public void inputBuffersChanged(Set<Integer> set) {
		Set<Integer> removed = Sets.difference(lastBufferedPackets.toSet(), set);
		Set<Integer> added = Sets.difference(set, lastBufferedPackets.toSet());
		
		// Update the proxy packet injector
		for (int packet : removed) {
			injector.getProxyPacketInjector().removePacketHandler(packet);
		}
		for (int packet : added) {
			injector.getProxyPacketInjector().addPacketHandler(packet);
		}
	}

	@Override
	public PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered) {
		return injector.packetReceived(packet, client, buffered);
	}
}
