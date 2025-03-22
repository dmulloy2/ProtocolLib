package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.Connection;

/**
 * Representing the context of a synchronous packet listener.
 */
public interface SyncPacketListenerContext {

    /**
     * Retrieves the connection associated with the packet.
     *
     * @return the connection handling the packet
     */
    @NotNull
    Connection connection();

    /**
     * Checks if the packet handling has been cancelled.
     *
     * @return true if the packet is cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * Sets whether the packet handling is cancelled. If cancelled, the packet
     * will not be processed further.
     *
     * @param cancelled true to cancel the packet, false to allow processing
     */
    void setCancelled(boolean cancelled);

    /**
     * Adds a listener to be invoked after the packet is sent or received.
     *
     * @param listener the transmission listener to invoke
     */
    void addTransmissionListener(@NotNull PacketTransmissionListener listener);
}
