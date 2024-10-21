package dev.protocollib.api;

import java.net.InetSocketAddress;
import java.util.Optional;

import org.bukkit.entity.Player;

import dev.protocollib.api.listener.PacketSentListener;

/**
 * Representing a connection of a player.
 */
public interface Connection {

    /**
     * Retrieves the player associated with the connection, if available.
     *
     * @return an optional containing the player, or empty if the player is not present
     */
    Optional<Player> player();

    /**
     * Retrieves the address of the connection.
     *
     * @return the remote address of the connection
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
     * @return the protocol phase of the connection
     */
    ProtocolPhase protocolPhase(ProtocolDirection packetDirection);

    /**
     * Checks if the connection is currently open.
     *
     * @return true if the connection is open, false otherwise
     */
    boolean isConnected();

    /**
     * Sends a binary packet over the connection.
     *
     * @param packet the binary packet to send
     */
    void sendPacket(BinaryPacket packet);

    /**
     * Sends a binary packet over the connection and registers a listener for when the packet is sent.
     *
     * @param packet   the binary packet to send
     * @param listener the listener to invoke once the packet is sent
     */
    void sendPacket(BinaryPacket packet, PacketSentListener listener);

    /**
     * Sends a packet container over the connection.
     *
     * @param packet the packet container to send
     */
    void sendPacket(PacketContainer packet);

    /**
     * Sends a packet container over the connection and registers a listener for when the packet is sent.
     *
     * @param packet   the packet container to send
     * @param listener the listener to invoke once the packet is sent
     */
    void sendPacket(PacketContainer packet, PacketSentListener listener);

    /**
     * Receives a packet container from the connection.
     *
     * @param packet the packet container received
     */
    void receivePacket(PacketContainer packet);

    /**
     * Disconnects the connection with the specified reason.
     *
     * @param reason the reason for disconnecting
     */
    void disconnect(String reason);

}
