package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.injector.netty.Injector;
import org.bukkit.entity.Player;

final class EmptyInjector implements Injector {

    public static final Injector WITHOUT_PLAYER = new EmptyInjector(null);

    private Player player;

    public EmptyInjector(Player player) {
        this.player = player;
    }

    @Override
    public int getProtocolVersion() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean inject() {
        return false;
    }

    @Override
    public void uninject() {
    }

    @Override
    public void close() {
    }

    @Override
    public void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) {
    }

    @Override
    public void receiveClientPacket(Object packet) {
    }

    @Override
    public Protocol getCurrentProtocol(PacketType.Sender sender) {
        return Protocol.HANDSHAKING;
    }

    @Override
    public NetworkMarker getMarker(Object packet) {
        return null;
    }

    @Override
    public void saveMarker(Object packet, NetworkMarker marker) {
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
    public void disconnect(String message) {
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
