package com.comphenix.protocol.injector.netty.manager;

import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.packet.AbstractPacketInjector;
import org.bukkit.entity.Player;

final class NetworkManagerPacketInjector extends AbstractPacketInjector {

	private final ListenerInvoker invoker;
	private final ChannelListener channelListener;

	public NetworkManagerPacketInjector(PacketTypeSet inboundFilters, ListenerInvoker invoker, ChannelListener listener) {
		super(inboundFilters);

		this.invoker = invoker;
		this.channelListener = listener;
	}

	@Override
	public PacketEvent packetReceived(PacketContainer packet, Player client) {
		PacketEvent event = PacketEvent.fromClient(this.channelListener, packet, null, client);
		this.invoker.invokePacketReceiving(event);

		return event;
	}
}
