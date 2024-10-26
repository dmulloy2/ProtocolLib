package dev.protocollib.api;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.bukkit.entity.Player;

import dev.protocollib.api.packet.PacketLike;
import dev.protocollib.api.packet.PacketOperationBuilder;

/**
 * Represents a connection associated with a player.
 * 
 * <p>This interface provides methods to interact with the player's network connection,
 * including retrieving the player's information, connection address, protocol version,
 * and current connection state. It also allows for sending and receiving packets
 * through the connection.</p>
 */
public interface Connection {


    /**
     * Retrieves the player associated with the connection, if available.
     *
     * @return an {@link Optional} containing the player, or empty if the player is not present
     */
    Optional<Player> player();

    /**
     * Retrieves the address of the connection.
     *
     * @return the remote {@link InetSocketAddress} of the connection
     */
    InetSocketAddress address();

    /**
     * Retrieves the protocol version used by the connection.
     *
     * @return the protocol version
     */
    int protocolVersion();

    /**
     * Retrieves the current protocol phase of the connection for a given direction.
     *
     * @param packetDirection the direction of the packet (clientbound or serverbound)
     * @return the {@link ProtocolPhase} representing the current phase of the connection
     */
    ProtocolPhase protocolPhase(ProtocolDirection packetDirection);

    /**
     * Checks if the connection is currently open.
     *
     * @return {@code true} if the connection is open, {@code false} otherwise
     */
    boolean isConnected();

    /**
     * Initiates a packet operation, which can involve sending or receiving a packet.
     *
     * @return a {@link PacketOperationBuilder} to configure the packet operation
     */
    PacketOperationBuilder packetOperation();

    /**
     * Sends a packet to the client.
     *
     * @param packet the packet to send
     */
    void sendPacket(PacketLike packet);

    /**
     * Receives a packet as if the client had sent it.
     *
     * @param packet the received packet
     */
    void receivePacket(PacketLike packet);

    /**
     * Disconnects the connection with the specified reason.
     *
     * @param reason the reason for disconnecting
     */
    void disconnect(String reason);

}
