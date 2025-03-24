package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.packet.PacketContainer;

@FunctionalInterface
public interface ImmutablePacketListener {

    void handlePacket(@NotNull PacketContainer packet, @NotNull ImmutablePacketListenerContext context);

}
