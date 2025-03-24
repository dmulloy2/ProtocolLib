package dev.protocollib.api.packet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.protocollib.api.reflect.MutableGenericAccessor;

public interface MutablePacketContainer extends PacketContainer {

    /**
     * Retrieves the raw packet object.
     *
     * @return the packet object
     */
    @NotNull
    Object packet();

    @NotNull
    MutableGenericAccessor accessor();

    @Nullable
    MutablePacketContainer bundle();

    @Nullable
    MutablePacketContainer clone();
}
