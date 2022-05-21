package com.comphenix.protocol.injector.netty.manager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.concurrency.PacketTypeSet;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import com.comphenix.protocol.injector.netty.channel.NettyChannelInjector;
import com.comphenix.protocol.injector.player.AbstractPlayerInjectionHandler;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.util.Set;

final class NetworkManagerPlayerInjector extends AbstractPlayerInjectionHandler {

	private final ChannelListener listener;
	private final InjectionFactory injectionFactory;
	private final PacketTypeSet mainThreadListeners;

	public NetworkManagerPlayerInjector(
			PacketTypeSet outboundListener,
			ChannelListener listener,
			InjectionFactory injectionFactory,
			PacketTypeSet mainThreadListeners
	) {
		super(outboundListener);

		this.listener = listener;
		this.injectionFactory = injectionFactory;
		this.mainThreadListeners = mainThreadListeners;
	}

	@Override
	public int getProtocolVersion(Player player) {
		return this.injectionFactory.fromPlayer(player, this.listener).getProtocolVersion();
	}

	@Override
	public void injectPlayer(Player player, ConflictStrategy strategy) {
		this.injectionFactory.fromPlayer(player, this.listener).inject();
	}

	@Override
	public void handleDisconnect(Player player) {
		// noop
	}

	@Override
	public boolean uninjectPlayer(Player player) {
		this.injectionFactory.fromPlayer(player, this.listener).uninject();
		return true;
	}

	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) {
		this.injectionFactory.fromPlayer(receiver, this.listener).sendServerPacket(packet.getHandle(), marker, filters);
	}

	@Override
	public void receiveClientPacket(Player player, Object mcPacket) {
		this.injectionFactory.fromPlayer(player, this.listener).receiveClientPacket(mcPacket);
	}

	@Override
	public void updatePlayer(Player player) {
		this.injectionFactory.fromPlayer(player, this.listener).inject();
	}

	@Override
	public boolean hasMainThreadListener(PacketType type) {
		return this.mainThreadListeners.contains(type);
	}

	@Override
	public Channel getChannel(Player player) {
		Injector injector = this.injectionFactory.fromPlayer(player, this.listener);
		if (injector instanceof NettyChannelInjector) {
			return ((NettyChannelInjector) injector).getWrappedChannel();
		}

		return null;
	}

	@Override
	public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		if (!type.isAsyncForced() && (options == null || !options.contains(ListenerOptions.ASYNC))) {
			this.mainThreadListeners.addType(type);
		}

		super.addPacketHandler(type, options);
	}

	@Override
	public void removePacketHandler(PacketType type) {
		this.mainThreadListeners.removeType(type);
		super.removePacketHandler(type);
	}
}
