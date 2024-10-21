package dev.protocollib.api.listener;

/**
 * Functional interface for a listener that is invoked when a packet has been sent.
 * 
 * This method will get invoked on the underlying channel's event-loop.
 */
@FunctionalInterface
public interface PacketSentListener {

    /**
     * Invoked when a packet has been successfully sent.
     */
    void invoke();
}

