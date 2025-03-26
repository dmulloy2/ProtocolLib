package dev.protocollib.api.packet;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import dev.protocollib.api.ProtocolDirection;
import dev.protocollib.api.ProtocolPhase;
import net.kyori.adventure.key.Keyed;

/**
 * Representing the type of a network packet.
 * 
 * <p>A {@code PacketType} identifies a specific type of packet in the protocol,
 * including information about its direction, phase in the protocol, associated class (if any),
 * whether the packet is currently supported and if it terminates the current phase.</p>
 */
public interface PacketType extends Keyed {

    /**
     * Retrieves the direction in which the packet is being sent.
     *
     * @return the {@link ProtocolDirection} of the packet, either clientbound or serverbound
     */
    @NotNull
    ProtocolDirection protocolDirection();

    /**
     * Retrieves the protocol phase during which this packet is used.
     * This indicates which phase of the protocol (e.g., HANDSHAKE, LOGIN, GAME)
     * the packet belongs to.
     *
     * @return the {@link ProtocolPhase} associated with this packet
     */
    @NotNull
    ProtocolPhase protocolPhase();

    /**
     * Retrieves the class associated with the packet type, if available.
     * 
     * <p>If the packet is supported (i.e., {@link #isSupported()} returns {@code true}),
     * this method will always return a non-empty {@link Optional} containing the packet class.
     * Note that the same packet class may be reused across different protocol phases.
     * The current protocol phase should be checked via {@link #protocolPhase()} to determine
     * the correct context.</p>
     *
     * @return an {@link Optional} containing the class of the packet, or empty if not applicable
     */
    @NotNull
    Optional<Class<?>> packetClass();

    /**
     * Checks whether the packet type is supported by the current protocol version.
     *
     * @return {@code true} if the packet type is supported, {@code false} otherwise
     */
    boolean isSupported();

    /**
     * Determines if processing this packet terminates the current protocol phase,
     * potentially transitioning to a new phase. For example, a login success packet
     * might terminate the LOGIN phase and transition to the CONFIGURATION phase.
     *
     * @return {@code true} if this packet ends the current protocol phase, {@code false} otherwise
     */
    boolean isTerminal();
}
