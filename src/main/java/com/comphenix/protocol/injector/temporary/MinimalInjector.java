package com.comphenix.protocol.injector.temporary;

import com.comphenix.protocol.events.NetworkMarker;
import java.net.SocketAddress;
import org.bukkit.entity.Player;

/**
 * Represents an injector that only gives access to a player's socket.
 *
 * @author Kristian
 */
public interface MinimalInjector {

	/**
	 * Retrieve the associated address of this player.
	 *
	 * @return The associated address.
	 */
	SocketAddress getAddress();

	/**
	 * Attempt to disconnect the current client.
	 *
	 * @param message - the message to display.
	 */
	void disconnect(String message);

	/**
	 * Send a packet to the client.
	 *
	 * @param packet   - server packet to send.
	 * @param marker   - the network marker.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 */
	void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered);

	/**
	 * Retrieve the hooked player.
	 *
	 * @return The hooked player.
	 */
	Player getPlayer();

	/**
	 * Determines if the player is currently connected.
	 *
	 * @return true if the player is connected.
	 */
	boolean isConnected();
}
