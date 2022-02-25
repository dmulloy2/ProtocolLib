package com.comphenix.protocol.injector.netty;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;

/**
 * Represents an injected client connection.
 * @author Kristian
 */
public interface Injector {
	/**
	 * Retrieve the current protocol version of the player.
	 * @return Protocol version.
	 */
	int getProtocolVersion();
	
	/**
	 * Inject the current channel.
	 * <p>
	 * Note that only active channels can be injected.
	 * @return TRUE if we injected the channel, false if we could not inject or it was already injected.
	 */
	boolean inject();

	void uninject();

	/**
	 * Close the current injector.
	 */
	void close();

	/**
	 * Send a packet to a player's client.
	 * @param packet - the packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet is filtered.
	 */
	void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered);

	/**
	 * Recieve a packet on the server.
	 * @param packet - the (NMS) packet to send.
	 * @deprecated use {@link #receiveClientPacket(Object)} instead.
	 */
	@Deprecated
	default void recieveClientPacket(Object packet) {
		this.receiveClientPacket(packet);
	}

	void receiveClientPacket(Object packet);

	/**
	 * Retrieve the current protocol state.
	 * @return The current protocol.
	 */
	Protocol getCurrentProtocol();

	/**
	 * Retrieve the network marker associated with a given packet.
	 * @param packet - the packet.
	 * @return The network marker.
	 */
	NetworkMarker getMarker(Object packet);

	/**
	 * Associate a given network marker with a specific packet.
	 * @param packet - the NMS packet.
	 * @param marker - the associated marker.
	 */
	void saveMarker(Object packet, NetworkMarker marker);

	/**
	 * Retrieve the current player or temporary player associated with the injector.
	 * @return The current player.
	 */
	Player getPlayer();

	/**
	 * Set the current player instance.
	 * @param player - the current player.
	 */
	void setPlayer(Player player);
	
	/**
	 * Determine if the channel has already been injected.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	boolean isInjected();

	/**
	 * Determine if this channel has been closed and cleaned up.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	boolean isClosed();
}
