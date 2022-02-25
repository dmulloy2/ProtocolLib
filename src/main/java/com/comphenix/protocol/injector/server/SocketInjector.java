package com.comphenix.protocol.injector.server;

import com.comphenix.protocol.events.NetworkMarker;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import org.bukkit.entity.Player;

/**
 * Represents an injector that only gives access to a player's socket.
 *
 * @author Kristian
 */
public interface SocketInjector {

	/**
	 * Retrieve the associated address of this player.
	 *
	 * @return The associated address.
	 * @throws IllegalAccessException If we're unable to read the socket field.
	 */
	SocketAddress getAddress() throws IllegalAccessException;

	/**
	 * Attempt to disconnect the current client.
	 *
	 * @param message - the message to display.
	 * @throws InvocationTargetException If disconnection failed.
	 */
	void disconnect(String message) throws InvocationTargetException;

	/**
	 * Send a packet to the client.
	 *
	 * @param packet   - server packet to send.
	 * @param marker   - the network marker.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @throws InvocationTargetException If an error occured when sending the packet.
	 */
	void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered)
			throws InvocationTargetException;

	/**
	 * Retrieve the hooked player.
	 *
	 * @return The hooked player.
	 */
	Player getPlayer();

	/**
	 * Invoked when a delegated socket injector transfers the state of one injector to the next.
	 *
	 * @param delegate - the new injector.
	 */
	void transferState(SocketInjector delegate);

	/**
	 * Determines if the player is currently connected.
	 *
	 * @return true if the player is connected.
	 */
	boolean isConnected();
}
