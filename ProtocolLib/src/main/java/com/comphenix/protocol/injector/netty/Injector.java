package com.comphenix.protocol.injector.netty;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.NetworkMarker;

/**
 * Represents an injected client connection.
 * @author Kristian
 */
interface Injector {
	/**
	 * Retrieve the current protocol version of the player.
	 * @return Protocol version.
	 */
	public abstract int getProtocolVersion();
	
	/**
	 * Inject the current channel.
	 * <p>
	 * Note that only active channels can be injected.
	 * @return TRUE if we injected the channel, false if we could not inject or it was already injected.
	 */
	public abstract boolean inject();

	/**
	 * Close the current injector.
	 */
	public abstract void close();

	/**
	 * Send a packet to a player's client.
	 * @param packet - the packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet is filtered.
	 */
	public abstract void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered);

	/**
	 * Recieve a packet on the server.
	 * @param packet - the (NMS) packet to send.
	 */
	public abstract void recieveClientPacket(Object packet);

	/**
	 * Retrieve the current protocol state.
	 * @return The current protocol.
	 */
	public abstract Protocol getCurrentProtocol();

	/**
	 * Retrieve the network marker associated with a given packet.
	 * @param packet - the packet.
	 * @return The network marker.
	 */
	public abstract NetworkMarker getMarker(Object packet);

	/**
	 * Associate a given network marker with a specific packet.
	 * @param packet - the NMS packet.
	 * @param marker - the associated marker.
	 */
	public abstract void saveMarker(Object packet, NetworkMarker marker);

	/**
	 * Retrieve the current player or temporary player associated with the injector.
	 * @return The current player.
	 */
	public abstract Player getPlayer();

	/**
	 * Set the current player instance.
	 * @param player - the current player.
	 */
	public abstract void setPlayer(Player player);
	
	/**
	 * Determine if the channel has already been injected.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public abstract boolean isInjected();

	/**
	 * Determine if this channel has been closed and cleaned up.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public abstract boolean isClosed();

	/**
	 * Set the updated player instance.
	 * <p>
	 * This will not replace the current instance, but it will allow PacketEvent to provide additional player information.
	 * @param player - the more up-to-date player.
	 */
	public abstract void setUpdatedPlayer(Player player);
}