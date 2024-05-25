package com.comphenix.protocol.injector.netty.manager;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import com.comphenix.protocol.injector.netty.channel.NettyChannelInjector;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;

import io.netty.channel.Channel;

final class NetworkManagerPlayerInjector implements PlayerInjectionHandler {

    private final ChannelListener listener;
    private final InjectionFactory injectionFactory;

    public NetworkManagerPlayerInjector(
            ChannelListener listener,
            InjectionFactory injectionFactory
    ) {
        this.listener = listener;
        this.injectionFactory = injectionFactory;
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
    public Channel getChannel(Player player) {
        Injector injector = this.injectionFactory.fromPlayer(player, this.listener);
        if (injector instanceof NettyChannelInjector) {
            return ((NettyChannelInjector) injector).getWrappedChannel();
        }

        return null;
    }
}
