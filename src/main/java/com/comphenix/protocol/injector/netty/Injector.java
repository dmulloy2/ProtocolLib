package com.comphenix.protocol.injector.netty;

import java.net.SocketAddress;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;

/**
 * Represents an injected client connection.
 *
 * @author Kristian
 */
public interface Injector {

    /**
     * Retrieve the current protocol version of the player.
     *
     * @return Protocol version.
     */
    int getProtocolVersion();

    /**
     * Inject the current channel.
     * <p>
     * Note that only active channels can be injected.
     */
    void inject();

    /**
     * Close the current injector.
     */
    void close();

    /**
     * Send a packet to a player's client.
     *
     * @param packet   - the packet to send.
     * @param marker   - the network marker.
     * @param filtered - whether or not the packet is filtered.
     */
    void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered);

    void receiveClientPacket(Object packet);

    void sendWirePacket(WirePacket packet);

    /**
     * Retrieve the current protocol state. Note that since 1.20.2 the client and server direction can be in different
     * protocol states.
     *
     * @param sender the side for which the state should be resolved.
     * @return The current protocol.
     */
    Protocol getCurrentProtocol(PacketType.Sender sender);

    SocketAddress getAddress();

    /**
     * Retrieve the current player or temporary player associated with the injector.
     *
     * @return The current player.
     */
    Player getPlayer();

    /**
     * Set the current player instance.
     *
     * @param player - the current player.
     */
    void setPlayer(Player player);

    void disconnect(String message);

    boolean isConnected();

    /**
     * Determine if the channel has already been injected.
     *
     * @return TRUE if it has, FALSE otherwise.
     */
    boolean isInjected();

    /**
     * Determine if this channel has been closed and cleaned up.
     *
     * @return TRUE if it has, FALSE otherwise.
     */
    boolean isClosed();
}
