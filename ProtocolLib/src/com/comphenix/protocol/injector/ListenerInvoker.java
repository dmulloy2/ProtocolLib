package com.comphenix.protocol.injector;

import net.minecraft.server.Packet;

import com.comphenix.protocol.events.PacketEvent;

/**
 * Represents an object that initiate the packet listeners.
 * 
 * @author Kristian
 */
public interface ListenerInvoker {

	/**
	 * Invokes the given packet event for every registered listener.
	 * @param event - the packet event to invoke.
	 */
	public abstract void invokePacketRecieving(PacketEvent event);

	/**
	 * Invokes the given packet event for every registered listener.
	 * @param event - the packet event to invoke.
	 */
	public abstract void invokePacketSending(PacketEvent event);

	/**
	 * Retrieve the associated ID of a packet.
	 * @param packet - the packet.
	 * @return The packet ID.
	 */
	public abstract int getPacketID(Packet packet);
}