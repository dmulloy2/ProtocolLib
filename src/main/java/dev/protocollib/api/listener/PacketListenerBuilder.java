package dev.protocollib.api.listener;

import java.util.Collection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.packet.PacketType;

/**
 * Builder for creating and registering packet listeners.
 * 
 * <p>This builder allows configuring packet listeners with various settings,
 * including the packet types they should handle, execution priority, and behavior
 * regarding canceled packets and packet bundles.</p>
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
         * 
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
         * @return the mutable packet listener builder for further configuration
         */
        @NotNull
        PacketListenerBuilder.Mutable mutable();

        /**
         * Registers the packet listener to operate synchronously. The listener will be executed
         * in order relative to other synchronous listeners.
         *
         * @param listener the immutable synchronous packet listener to register
         * @return the packet listener registration instance
         */
        @NotNull
        PacketListenerRegistration registerSync(@NotNull ImmutablePacketListener listener);

        /**
         * Registers the packet listener to operate asynchronously. The listener will be executed
         * in parallel with other asynchronous listeners, ensuring non-blocking packet processing.
         *
         * @param listener the immutable asynchronous packet listener to register
         * @return the packet listener registration instance
         */
        @NotNull
        PacketListenerRegistration registerAsync(@NotNull ImmutablePacketListener listener);
    }

    /**
     * Interface for building a mutable packet listener, allowing packet modifications.
     */
    public interface Mutable {

        /**
         * Registers the packet listener to operate synchronously. The listener will always
         * be executed on the main game thread.
         *
         * @param listener the synchronous mutable packet listener to register
         * @return the packet listener registration instance
         */
        @NotNull
        PacketListenerRegistration registerSync(@NotNull MutableSyncPacketListener listener);

        /**
         * Registers the packet listener to operate asynchronously. The listener will be executed
         * in parallel with other asynchronous listeners, ensuring non-blocking packet processing.
         *
         * @param listener the asynchronous mutable packet listener to register
         * @return the packet listener registration instance
         */
        @NotNull
        PacketListenerRegistration registerAsync(@NotNull MutableAsyncPacketListener listener);
    }
}
