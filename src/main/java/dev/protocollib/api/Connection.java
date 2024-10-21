package dev.protocollib.api;

import java.net.InetSocketAddress;

import javax.annotation.Nullable;

import org.bukkit.entity.Player;

public interface Connection {

    @Nullable
    Player player();

    InetSocketAddress address();

    int protocolVersion();

    ProtocolPhase protocolPhase(PacketDirection packetDirection);

    boolean isConnected();

    void sendPacket(BinaryPacket packet);

    void sendPacket(PacketContainer packet);

    void receivePacket(PacketContainer packet);

    void disconnect(String reason);

}
