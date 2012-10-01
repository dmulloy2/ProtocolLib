package com.comphenix.protocol;

import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;

/**
 * Represents a asynchronous packet handler.
 * 
 * @author Kristian
 */
public interface AsynchronousManager {

	/**
	 * Registers an asynchronous packet handler.
	 * <p>
	 * To start listening asynchronously, pass the getListenerLoop() runnable to a different thread.
	 * @param listener - the packet listener that will recieve these asynchronous events.
	 * @return An asynchrouns handler.
	 */
	public abstract AsyncListenerHandler registerAsyncHandler(PacketListener listener);

	/**
	 * Unregisters and closes the given asynchronous handler.
	 * @param handler - asynchronous handler.
	 */
	public abstract void unregisterAsyncHandler(AsyncListenerHandler handler);

	/**
	 * Unregisters every asynchronous handler associated with this plugin.
	 * @param plugin - the original plugin.
	 */
	public void unregisterAsyncHandlers(Plugin plugin);
	
	/**
	 * Retrieves a immutable set containing the ID of the sent server packets that will be 
	 * observed by the asynchronous listeners.
	 * @return Every filtered server packet.
	 */
	public abstract Set<Integer> getSendingFilters();

	/**
	 * Retrieves a immutable set containing the ID of the recieved client packets that will be
	 * observed by the asynchronous listeners.
	 * @return Every filtered client packet.
	 */
	public abstract Set<Integer> getReceivingFilters();

	/**
	 * Determine if a given synchronous packet has asynchronous listeners.
	 * @param packet - packet to test.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public abstract boolean hasAsynchronousListeners(PacketEvent packet);

	/**
	 * Retrieve the default packet stream.
	 * @return Default packet stream.
	 */
	public abstract PacketStream getPacketStream();

	/**
	 * Retrieve the default error logger.
	 * @return Default logger.
	 */
	public abstract Logger getLogger();

	/**
	 * Remove listeners, close threads and transmit every delayed packet.
	 */
	public abstract void cleanupAll();
}