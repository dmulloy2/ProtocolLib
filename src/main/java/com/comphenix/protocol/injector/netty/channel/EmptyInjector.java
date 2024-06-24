package com.comphenix.protocol.injector.netty.channel;

import java.net.SocketAddress;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.WirePacket;

final class EmptyInjector implements Injector {

    public static final Injector WITHOUT_PLAYER = new EmptyInjector(null);

    private Player player;

    public EmptyInjector(Player player) {
        this.player = player;
    }

    @Override
    public SocketAddress getAddress() {
        if (this.player != null) {
            return this.player.getAddress();
        }
        return null;
    }

    @Override
    public int getProtocolVersion() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void inject() {
    }

    @Override
    public void close() {
    }

    @Override
    public void sendClientboundPacket(Object packet, NetworkMarker marker, boolean filtered) {
    }

    @Override
    public void readServerboundPacket(Object packet) {
    }

    @Override
    public void sendWirePacket(WirePacket packet) {
    }

    @Override
    public void disconnect(String message) {
    }

    @Override
    public Protocol getCurrentProtocol(PacketType.Sender sender) {
        return Protocol.HANDSHAKING;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isInjected() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return true;
    }
}
