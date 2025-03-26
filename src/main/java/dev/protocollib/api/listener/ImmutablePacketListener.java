package dev.protocollib.api.listener;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.packet.PacketContainer;

/**
 * Functional interface for handling immutable packets.
 * 
 * <p>An immutable packet listener can be executed either synchronously or asynchronously
 * depending on how it was registered. If registered asynchronously, it will be executed
 * in parallel with other listeners, ensuring non-blocking packet processing.</p>
 */
@FunctionalInterface
public interface ImmutablePacketListener {

    /**
     * Handles a packet that was sent or received.
     * 
     * @param packet  the immutable packet to handle
     * @param context the context providing additional information about the packet
     */
    void handlePacket(@NotNull PacketContainer packet, @NotNull ImmutablePacketListenerContext context);
}
