package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.temporary.MinimalInjector;
import java.net.SocketAddress;
import org.bukkit.entity.Player;

final class NettyChannelMinimalInjector implements MinimalInjector {

	private final NettyChannelInjector injector;

	public NettyChannelMinimalInjector(NettyChannelInjector injector) {
		this.injector = injector;
	}

	@Override
	public SocketAddress getAddress() {
		return this.injector.getWrappedChannel().remoteAddress();
	}

	@Override
	public void disconnect(String message) {
		this.injector.disconnect(message);
	}

	@Override
	public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
		this.injector.sendServerPacket(packet, marker, filtered);
	}

	@Override
	public Player getPlayer() {
		return this.injector.getPlayer();
	}

	@Override
	public boolean isConnected() {
		return this.injector.getWrappedChannel().isActive();
	}

	public NettyChannelInjector getInjector() {
		return this.injector;
	}
}
