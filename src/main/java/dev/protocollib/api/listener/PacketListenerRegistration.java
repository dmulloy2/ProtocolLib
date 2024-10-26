package dev.protocollib.api.listener;

/**
 * Representing the registration of a packet listener.
 * Allows unregistering the listener when no longer needed.
 */
public interface PacketListenerRegistration {

    /**
     * Unregisters the packet listener, stopping it from receiving further packets.
     */
    void unregister();
}

