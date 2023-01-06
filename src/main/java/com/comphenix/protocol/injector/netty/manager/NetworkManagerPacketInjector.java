package com.comphenix.protocol.injector.netty.manager;

import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.packet.AbstractPacketInjector;
import org.bukkit.entity.Player;

final class NetworkManagerPacketInjector extends AbstractPacketInjector {

	private final ListenerInvoker invoker;
	private final ChannelListener channelListener;
	private final PacketTypeSet mainThreadListeners;

	public NetworkManagerPacketInjector(
			PacketTypeSet inboundFilters,
			ListenerInvoker invoker,
			ChannelListener listener,
			PacketTypeSet mainThreadListeners
	) {
		super(inboundFilters);

		this.invoker = invoker;
		this.channelListener = listener;
		this.mainThreadListeners = mainThreadListeners;
	}

	@Override
	public boolean addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		if (!type.isAsyncForced() && options != null && options.contains(ListenerOptions.SYNC)) {
			this.mainThreadListeners.addType(type);
		}

		return super.addPacketHandler(type, options);
	}

	@Override
	public boolean removePacketHandler(PacketType type) {
		this.mainThreadListeners.removeType(type);
		return super.removePacketHandler(type);
	}

	@Override
	public PacketEvent packetReceived(PacketContainer packet, Player client) {
		PacketEvent event = PacketEvent.fromClient(this.channelListener, packet, null, client);
		this.invoker.invokePacketReceiving(event);

		return event;
	}

	@Override
	public boolean hasMainThreadListener(PacketType type) {
		return this.mainThreadListeners.contains(type);
	}
}
