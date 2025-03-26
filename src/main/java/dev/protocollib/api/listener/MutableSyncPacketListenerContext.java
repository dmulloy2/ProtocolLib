package dev.protocollib.api.listener;

/**
 * Context of a mutable synchronous packet listener.
 */
public interface MutableSyncPacketListenerContext extends ImmutablePacketListenerContext {

    /**
     * Sets whether the packet handling is cancelled. If cancelled, the packet will
     * not be processed further unless a listener is using the
     * {@link PacketListenerBuilder.WithType#includeCanceledPackets() includeCanceledPackets} flag.
     *
     * @param cancelled true to cancel the packet, false to allow processing
     * 
     * @see PacketListenerBuilder
     */
    void setCancelled(boolean cancelled);
}
