package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.Connection;

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
    boolean isCancelledVolatile();

    /**
     * Adds a listener to be invoked after the packet is sent or received.
     *
     * @param listener the transmission listener to invoke
     */
    void addAsyncTransmissionListener(@NotNull PacketTransmissionListener listener);// TODO async via netty
}
