package dev.protocollib.api.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.protocollib.api.reflect.GenericAccessor;

/**
 * Representing a container for a packet.
 */
public non-sealed interface PacketContainer extends PacketLike {

    /**
     * Retrieves the type of the packet.
     *
     * @return the packet type
     */
    @NotNull
    PacketType packetType();

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
     * @return a clone of this instance
     */
    @Nullable
    PacketContainer clone();
}
