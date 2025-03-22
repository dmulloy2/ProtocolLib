package dev.protocollib.api.listener;

/**
 * Functional interface for a listener that is invoked when a packet has been sent
 * (according to the underlying write's ChannelFuture) or received (just before the
 * packet gets processed by mojangs packet handlers).
 * 
 * This method will get invoked on the underlying channel's event-loop.
 */
@FunctionalInterface
public interface PacketTransmissionListener {

    /**
     * Invoked when a packet has been successfully sent.
     */
    void invoke();
}
