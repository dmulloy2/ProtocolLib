package com.comphenix.protocol.injector.netty;

import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketEvent;


/**
 * Represents a listener for received or sent packets.
 * @author Kristian
 */
interface ChannelListener {
	/**
	 * Invoked when a packet is being sent to the client.
	 * <p>
	 * This is invoked on the main thread.
	 * @param injector - the channel injector.
	 * @param packet - the packet.
	 * @param marker - the network marker.
	 * @return The packet even that was passed to the listeners, with a possible packet change, or NULL.
	 */
	public PacketEvent onPacketSending(Injector injector, Object packet, NetworkMarker marker);
	
	/**
	 * Invoked when a packet is being received from a client.
	 * <p>
	 * This is invoked on an asynchronous worker thread.
	 * @param injector - the channel injector.
	 * @param packet - the packet.
	 * @param marker - the associated network marker, if any.
	 * @return The packet even that was passed to the listeners, with a possible packet change, or NULL.
	 */
	public PacketEvent onPacketReceiving(Injector injector, Object packet, NetworkMarker marker);
	
	/**
	 * Determine if there is a packet listener for the given packet.
	 * @param packetClass - the packet class to check.
	 * @return TRUE if there is such a listener, FALSE otherwise.
	 */
	public boolean hasListener(Class<?> packetClass);
	
	/**
	 * Determine if there is a server packet listener that must be executed on the main thread.
	 * @param packetClass - the packet class to check.
	 * @return TRUE if there is, FALSE otherwise.
	 */
	public boolean hasMainThreadListener(Class<?> packetClass);
	
	/**
	 * Determine if we need the buffer data of a given client side packet.
	 * @param packetClass - the packet class.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	public boolean includeBuffer(Class<?> packetClass);
	
	/**
	 * Retrieve the current error reporter.
	 * @return The error reporter.
	 */
	public ErrorReporter getReporter();
	
    /**
     * Determine if debug mode is enabled.
     * @return TRUE if it is, FALSE otherwise.
     */
	boolean isDebug();
}