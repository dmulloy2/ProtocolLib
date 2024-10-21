package dev.protocollib.api;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import dev.protocollib.api.listener.PacketListenerBuilder;

/**
 * Representing the main entry point for the ProtocolLib API.
 */
public interface ProtocolLib {

	/**
     * Creates a packet listener for the provided plugin.
     *
     * @param plugin the plugin registering the packet listener
     * @return a builder to configure and register the packet listener
     */
    PacketListenerBuilder createListener(Plugin plugin);
   
    /**
     * Retrieves the connection associated with a specific player.
     *
     * @param player the player whose connection is being retrieved
     * @return the connection for the specified player
     */
    Connection connection(Player player);

    /**
     * Creates a new binary packet with the given type and payload.
     *
     * @param packetType the type of the packet to create
     * @param payload the binary payload to include in the packet
     * @return a new {@link BinaryPacket} instance
     */
    BinaryPacket createBinaryPacket(PacketType packetType, byte[] payload);

    /**
     * Creates a new packet container for the given packet type.
     *
     * @param packetType the type of the packet to create
     * @return a new {@link PacketContainer} instance
     */
    PacketContainer createPacket(PacketType packetType);
}
