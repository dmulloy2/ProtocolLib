package com.comphenix.protocol.injector.server;

import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.SocketAddress;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.NetworkMarker;

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
	 */
	public abstract Socket getSocket() throws IllegalAccessException;

	/**
	 * Retrieve the associated address of this player.
	 * @return The associated address.
	 * @throws IllegalAccessException If we're unable to read the socket field.
	 */
	public abstract SocketAddress getAddress() throws IllegalAccessException;

	/**
	 * Attempt to disconnect the current client.
	 * @param message - the message to display.
	 * @throws InvocationTargetException If disconnection failed.
	 */
	public abstract void disconnect(String message) throws InvocationTargetException;

	/**
	 * Send a packet to the client.
	 * @param packet - server packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @throws InvocationTargetException If an error occured when sending the packet.
	 */
	public abstract void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) 
			throws InvocationTargetException;

	/**
	 * Retrieve the hooked player.
	 */
	public abstract Player getPlayer();

	/**
	 * Retrieve the hooked player object OR the more up-to-date player instance.
	 * @return The hooked player, or a more up-to-date instance.
	 */
	public abstract Player getUpdatedPlayer();

	/**
	 * Invoked when a delegated socket injector transfers the state of one injector to the next. 
	 * @param delegate - the new injector.
	 */
	public abstract void transferState(SocketInjector delegate);

	/**
	 * Set the real Bukkit player that we will use.
	 * @param updatedPlayer - the real Bukkit player.
	 */
	public abstract void setUpdatedPlayer(Player updatedPlayer);
}