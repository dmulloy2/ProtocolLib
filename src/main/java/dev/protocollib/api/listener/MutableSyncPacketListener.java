package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.packet.MutablePacketContainer;

/**
 * Functional interface for handling and manipulating packets synchronously.
 */
@FunctionalInterface
public interface MutableSyncPacketListener {

    /**
     * Handles a packet that was sent or received, synchronously.
     *
     * @param packet  the packet to handle
     * @param context the context providing additional information about the packet and functions
     */
    void handlePacket(@NotNull MutablePacketContainer packet, @NotNull MutableSyncPacketListenerContext context);

}
