package dev.protocollib.api.packet;

import java.util.Optional;

import dev.protocollib.api.ProtocolDirection;
import net.kyori.adventure.key.Keyed;

/**
 * Representing the type of a network packet.
 * 
 * <p>A {@code PacketType} identifies a specific type of packet in the protocol,
 * including information about its direction, associated class (if any), and 
 * whether the packet is currently supported.</p>
 */
public interface PacketType extends Keyed {

    /**
     * Retrieves the direction in which the packet is being sent.
     *
     * @return the {@link ProtocolDirection} of the packet, either clientbound or serverbound
     */
    ProtocolDirection protocolDirection();

    /**
     * Retrieves the class associated with the packet type, if available.
     * 
     * <p>Not all packet types have an associated class. If there is no class, 
     * an empty {@link Optional} is returned.</p>
     *
     * @return an {@link Optional} containing the class of the packet, or empty if not applicable
     */
    Optional<Class<?>> packetClass();

    /**
     * Checks whether the packet type is supported by the current protocol version.
     *
     * @return {@code true} if the packet type is supported, {@code false} otherwise
     */
    boolean isSupported();

}
