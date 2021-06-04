package com.comphenix.protocol.injector.server;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;

import com.comphenix.protocol.events.NetworkMarker;

import org.bukkit.entity.Player;

/**
 * Represents an injector that only gives access to a player's socket.
 * 
 * @author Kristian
 */
public interface SocketInjector {
	/**
	 * Retrieve the associated socket of this player.
	 * @return The associated socket.
	 * @throws IllegalAccessException If we're unable to read the socket field.
	 * @deprecated May be null on certain server implementations. Also don't use raw sockets.
	 */
	@Deprecated
	Socket getSocket() throws IllegalAccessException;

	/**
	 * Retrieve the associated address of this player.
	 * @return The associated address.
	 * @throws IllegalAccessException If we're unable to read the socket field.
	 */
	SocketAddress getAddress() throws IllegalAccessException;

	/**
	 * Attempt to disconnect the current client.
	 * @param message - the message to display.
	 * @throws InvocationTargetException If disconnection failed.
	 */
	void disconnect(String message) throws InvocationTargetException;

	/**
	 * Send a packet to the client.
	 * @param packet - server packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @throws InvocationTargetException If an error occured when sending the packet.
	 */
	void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered)
			throws InvocationTargetException;

	/**
	 * Retrieve the hooked player.
	 * @return The hooked player.
	 */
	Player getPlayer();

	/**
	 * Retrieve the hooked player object OR the more up-to-date player instance.
	 * @return The hooked player, or a more up-to-date instance.
	 */
	Player getUpdatedPlayer();

	/**
	 * Invoked when a delegated socket injector transfers the state of one injector to the next.
	 * @param delegate - the new injector.
	 */
	void transferState(SocketInjector delegate);

	/**
	 * Set the real Bukkit player that we will use.
	 * @param updatedPlayer - the real Bukkit player.
	 */
	void setUpdatedPlayer(Player updatedPlayer);

	/**
	 * Determines if the player is currently connected.
	 * @return true if the player is connected.
	 */
	boolean isConnected();
}
