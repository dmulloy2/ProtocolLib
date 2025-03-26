package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.Connection;

/**
 * Context for immutable packet listeners.
 */
public interface ImmutablePacketListenerContext {

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
     * Adds a listener to be invoked after the packet is fully sent or received.
     * The transmission listener will get invoked on the underlying channel's
     * event-loop.
     *
     * @param listener the transmission listener to invoke
     */
    void addTransmissionListener(@NotNull PacketTransmissionListener listener);
}
