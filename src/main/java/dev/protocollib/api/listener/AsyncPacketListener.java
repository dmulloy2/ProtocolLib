package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.packet.MutablePacketContainer;

/**
 * Functional interface for handling packets asynchronously.
 * 
 * <p>Once a packet is processed by the listener, the context's 
 * {@code resumeProcessing()} or {@code resumeProcessingWithException(Throwable)} 
 * methods must be called to signal that the listener is done with the packet.
 * Failing to call one of these methods will cause the packet to remain in 
 * a waiting state until it times out, preventing further listeners from 
 * receiving the packet.
 * </p>
 */
@FunctionalInterface
public interface AsyncPacketListener {

    /**
     * Handles a packet that was sent or received, asynchronously.
     *
     * <p>Once processing is complete, ensure that one of the {@code resumeProcessing} 
     * methods from the {@link AsyncPacketListenerContext} is called. This allows the 
     * packet to continue to the next listener. If not called, the packet will remain 
     * in a waiting state and will only proceed after a timeout occurs.</p>
     *
     * @param packet  the packet to handle
     * @param context the context providing additional information about the packet and connection
     */
    void handlePacket(@NotNull MutablePacketContainer packet, @NotNull AsyncPacketListenerContext context);

}
