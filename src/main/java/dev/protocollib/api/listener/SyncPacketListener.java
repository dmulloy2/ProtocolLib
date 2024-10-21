package dev.protocollib.api.listener;

import dev.protocollib.api.PacketContainer;

/**
 * Functional interface for handling packets synchronously.
 */
@FunctionalInterface
public interface SyncPacketListener {

    /**
     * Synchronously handles a packet that was sent or received.
     *
     * @param packet  the packet to handle
     * @param context the context providing additional information about the packet and functions
     */
    void handlePacket(PacketContainer packet, SyncPacketListenerContext context);

}
