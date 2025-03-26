package dev.protocollib.api.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.protocollib.api.reflect.GenericAccessor;

/**
 * Represents a container for a packet.
 */
public non-sealed interface PacketContainer extends PacketLike {

    /**
     * Retrieves the type of the packet.
     *
     * @return the packet type
     */
    @NotNull
    PacketType packetType();

    /**
     * Provides a {@link GenericAccessor} for accessing packet fields reflectively.
     *
     * @return the generic accessor for packet data
     */
    @NotNull
    GenericAccessor accessor();

    /**
     * Retrieves the packet bundle that this packet is part of, if any.
     * A packet bundle represents a collection of packets that are processed together.
     *
     * @return the packet bundle containing this packet, or {@code null} if not part of a bundle
     */
    @Nullable
    PacketContainer bundle();

    /**
     * Creates and returns a mutable copy of this packet.
     *
     * <p>The cloned packet allows modifications, unlike the immutable {@code PacketContainer}.</p>
     *
     * @return a mutable clone of this packet container
     */
    @Nullable
    PacketContainer clone();
}
