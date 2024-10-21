package dev.protocollib.api.listener;

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
    Connection connection();

    /**
     * Checks if the packet handling has been cancelled.
     *
     * @return true if the packet is cancelled, false otherwise
     */
    boolean isCancelled();

    /**
     * Cancels the packet, preventing it from being processed further.
     */
    void cancel();

    /**
     * Adds a listener to be invoked after the packet is sent.
     *
     * @param listener the post-sent listener
     */
    void addPostSentListener(PacketSentListener listener);
}

