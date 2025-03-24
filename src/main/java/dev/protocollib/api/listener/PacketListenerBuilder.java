package dev.protocollib.api.listener;

import java.util.Collection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    PacketListenerBuilder.WithType types(@NotNull PacketType... packetTypes);

    /**
     * Specifies the types of packets to listen for.
     *
     * @param packetTypes the collection of packet types to listen for
     * @return the same builder for further configuration
     */
    @NotNull
    PacketListenerBuilder.WithType types(@NotNull Collection<PacketType> packetTypes);

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
        @Contract("_ -> this")
        PacketListenerBuilder.WithType priority(@NotNull PacketListenerPriority priority);

        /**
         * Configures the bundle behavior for the listener, determining how packets in bundles are handled.
         *
         * @param bundleBehavior the bundle behavior to apply
         * @return the same builder for further configuration
         * @see PacketListenerBundleBehavior
         */
        @Contract("_ -> this")
        PacketListenerBuilder.WithType bundleBehavior(@NotNull PacketListenerBundleBehavior bundleBehavior);

        /**
         * Configures the listener to include packets that have been canceled. By default,
         * canceled packets are skipped.
         *
         * @return the same builder for further configuration
         */
        @Contract("_ -> this")
        PacketListenerBuilder.WithType includeCanceledPackets();

        /**
         * Allows the listener to modify packets. By default, listeners are read-only and
         * cannot modify packets.
         *
         * @return the same builder for further configuration
         */
        @NotNull
        PacketListenerBuilder.Mutable mutable();

        @NotNull
        PacketListenerRegistration registerSync(@NotNull ImmutablePacketListener listener);

        @NotNull
        PacketListenerRegistration registerAsync(@NotNull ImmutablePacketListener listener);
    }

    public interface Mutable {

        /**
         * Registers the packet listener to operate synchronously. The listener
         * will always get called on the main game thread.
         *
         * @param listener the synchronous packet listener to register
         * @return the same builder for further configuration
         */
        @NotNull
        PacketListenerRegistration registerSync(@NotNull SyncPacketListener listener);

        /**
         * Registers the packet listener to operate asynchronously.
         *
         * @param listener the asynchronous packet listener to register
         * @return the same builder for further configuration
         */
        @NotNull
        PacketListenerRegistration registerAsync(@NotNull AsyncPacketListener listener);
    }
}
