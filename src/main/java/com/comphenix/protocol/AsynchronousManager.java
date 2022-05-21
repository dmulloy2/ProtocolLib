/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol;

import com.comphenix.protocol.async.AsyncListenerHandler;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Represents a asynchronous packet handler.
 * 
 * @author Kristian
 */
public interface AsynchronousManager {
	/**
	 * Registers an asynchronous packet handler.
	 * <p>
	 * Use {@link AsyncMarker#incrementProcessingDelay()} to delay a packet until its ready to be transmitted.
	 * <p>
	 * To start listening asynchronously, pass the getListenerLoop() runnable to a different thread.
	 * @param listener - the packet listener that will receive these asynchronous events.
	 * @return An asynchronous handler.
	 */
	AsyncListenerHandler registerAsyncHandler(PacketListener listener);

	/**
	 * Unregisters and closes the given asynchronous handler.
	 * @param handler - asynchronous handler.
	 */
	void unregisterAsyncHandler(AsyncListenerHandler handler);

	/**
	 * Unregisters and closes the first asynchronous handler associated with the given listener.
	 * @param listener - asynchronous listener
	 */
	void unregisterAsyncHandler(PacketListener listener);
	
	/**
	 * Unregisters every asynchronous handler associated with this plugin.
	 * @param plugin - the original plugin.
	 */
	void unregisterAsyncHandlers(Plugin plugin);

	/**
	 * Retrieves a immutable set containing the types of the sent server packets that will be
	 * observed by the asynchronous listeners.
	 * @return Every filtered server packet.
	 */
	Set<PacketType> getSendingTypes();

	/**
	 * Retrieves a immutable set containing the types of the received client packets that will be
	 * observed by the asynchronous listeners.
	 * @return Every filtered client packet.
	 */
	Set<PacketType> getReceivingTypes();

	/**
	 * Determine if a given synchronous packet has asynchronous listeners.
	 * @param packet - packet to test.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	boolean hasAsynchronousListeners(PacketEvent packet);

	/**
	 * Retrieve the default packet stream.
	 * @return Default packet stream.
	 */
	PacketStream getPacketStream();

	/**
	 * Retrieve the default error reporter.
	 * @return Default reporter.
	 */
	ErrorReporter getErrorReporter();

	/**
	 * Remove listeners, close threads and transmit every delayed packet.
	 */
	void cleanupAll();

	/**
	 * Signal that a packet is ready to be transmitted.
	 * <p>
	 * This should only be called if {@link com.comphenix.protocol.async.AsyncMarker#incrementProcessingDelay() AsyncMarker.incrementProcessingDelay()}
	 * has been called previously.
	 * @param packet - packet to signal.
	 */
	void signalPacketTransmission(PacketEvent packet);

	/**
	 * Register a synchronous listener that handles packets when they time out.
	 * @param listener - synchronous listener that will handle timed out packets.
	 */
	void registerTimeoutHandler(PacketListener listener);
	
	/**
	 * Unregisters a given timeout listener.
	 * @param listener - the timeout listener to unregister.
	 */
	void unregisterTimeoutHandler(PacketListener listener);

	/**
	 * Get a immutable set of every registered timeout handler.
	 * @return Set of every registered timeout handler.
	 */
	Set<PacketListener> getTimeoutHandlers();

	/**
	 * Get an immutable set of every registered asynchronous packet listener.
	 * @return Set of every asynchronous packet listener.
	 */
	Set<PacketListener> getAsyncHandlers();
}
