package dev.protocollib.api.listener;

import java.util.Collection;

import dev.protocollib.api.packet.PacketType;

/**
 * Builder for creating and registering packet listeners.
 */
public interface PacketListenerBuilder {

    /**
     * Specifies the types of packets to listen for.
     *
     * @param packetTypes the packet types to listen for
     * @return the same builder for further configuration
     */
    PacketListenerBuilder.WithType types(PacketType... packetTypes);

    /**
     * Specifies the types of packets to listen for.
     *
     * @param packetTypes the collection of packet types to listen for
     * @return the same builder for further configuration
     */
    PacketListenerBuilder.WithType types(Collection<PacketType> packetTypes);

    /**
     * Interface for building a packet listener with specific packet types.
     */
    public interface WithType {

        /**
         * Specifies the priority of the packet listener.
         *
         * @param priority the priority to assign to the listener
         * @return the same builder for further configuration
         */
        PacketListenerBuilder.WithType priority(PacketListenerPriority priority);

        /**
         * Marks the listener as immutable, preventing modifications of packets
         * inside the listener.
         *
         * @return the same builder for further configuration
         */
        PacketListenerBuilder.WithType immutable();

        /**
         * Configures the listener to ignore packets that have been cancelled.
         *
         * @return the same builder for further configuration
         */
        PacketListenerBuilder.WithType ignoreCancelledPackets();

        /**
         * Registers the packet listener to operate synchronously. The listener
         * will always get called on the main game thread.
         *
         * @param listener the synchronous packet listener to register
         * @return the same builder for further configuration
         */
        PacketListenerRegistration registerSync(SyncPacketListener listener);

        /**
         * Registers the packet listener to operate asynchronously.
         *
         * @param listener the asynchronous packet listener to register
         * @return the same builder for further configuration
         */
        PacketListenerRegistration registerAsync(AsyncPacketListener listener);
    }
}

