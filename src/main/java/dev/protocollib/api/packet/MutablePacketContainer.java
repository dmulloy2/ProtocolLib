package dev.protocollib.api.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.protocollib.api.reflect.MutableGenericAccessor;

/**
 * Represents a mutable packet container that allows modifications.
 */
public interface MutablePacketContainer extends PacketContainer {

    /**
     * Retrieves the raw packet object.
     *
     * <p>This method provides access to the underlying packet instance,
     * allowing direct interaction with its data.</p>
     *
     * @return the raw packet object
     */
    @NotNull
    Object packet();

    /**
     * Provides a {@link MutableGenericAccessor} for modifying packet fields reflectively.
     *
     * @return the mutable generic accessor for packet data
     */
    @NotNull
    MutableGenericAccessor accessor();

    /**
     * Retrieves the packet bundle that this packet is part of, if any.
     *
     * @return the packet bundle containing this packet, or {@code null} if not part of a bundle
     */
    @Nullable
    MutablePacketContainer bundle();

    /**
     * Creates and returns a mutable copy of this packet.
     *
     * <p>The cloned packet retains mutability, allowing further modifications.</p>
     *
     * @return a mutable clone of this packet container
     */
    @Nullable
    MutablePacketContainer clone();
}
