package com.comphenix.protocol.injector.player;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;

import io.netty.channel.Channel;

public interface PlayerInjectionHandler {

    /**
     * Retrieve the protocol version of the given player.
     *
     * @param player - the player.
     * @return The protocol version, or {@link Integer#MIN_VALUE}.
     */
    int getProtocolVersion(Player player);

    /**
     * Initialize a player hook, allowing us to read server packets.
     * <p>
     * This call will  be ignored if there's no listener that can receive the given events.
     *
     * @param player   - player to hook.
     * @param strategy - how to handle injection conflicts.
     */
    void injectPlayer(Player player, ConflictStrategy strategy);

    /**
     * Uninject the given player.
     *
     * @param player - player to uninject.
     * @return TRUE if a player has been uninjected, FALSE otherwise.
     */
    boolean uninjectPlayer(Player player);

    /**
     * Send the given packet to the given receiver.
     *
     * @param receiver - the player receiver.
     * @param packet   - the packet to send.
     * @param marker   - network marker.
     * @param filters  - whether or not to invoke the packet filters.
     */
    void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters);

    /**
     * Process a packet as if it were sent by the given player.
     *
     * @param player   - the sender.
     * @param mcPacket - the packet to process.
     */
    void receiveClientPacket(Player player, Object mcPacket);

    /**
     * Ensure that packet readers are informed of this player reference.
     *
     * @param player - the player to update.
     */
    void updatePlayer(Player player);

    Channel getChannel(Player player);

    /**
     * How to handle a previously existing player injection.
     *
     * @author Kristian
     */
    enum ConflictStrategy {
        /**
         * Override it.
         */
        OVERRIDE,

        /**
         * Immediately exit.
         */
        BAIL_OUT;
    }
}
