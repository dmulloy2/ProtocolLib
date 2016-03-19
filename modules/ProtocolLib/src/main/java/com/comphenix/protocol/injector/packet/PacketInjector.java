package com.comphenix.protocol.injector.packet;

import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 * Represents an incoming packet injector.
 * 
 * @author Kristian
 */
public interface PacketInjector {
	/**
	 * Determine if a packet is cancelled or not.
	 * @param packet - the packet to check.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public abstract boolean isCancelled(Object packet);
	
	/**
	 * Set whether or not a packet is cancelled.
	 * @param packet - the packet to set.
	 * @param cancelled - TRUE to cancel the packet, FALSE otherwise.
	 */
	public abstract void setCancelled(Object packet, boolean cancelled);
	
	/**
	 * Start intercepting packets with the given packet type.
	 * @param type - the type of the packets to start intercepting.
	 * @param options - any listener options.
	 * @return TRUE if we didn't already intercept these packets, FALSE otherwise.
	 */
	public abstract boolean addPacketHandler(PacketType type, Set<ListenerOptions> options);

	/**
	 * Stop intercepting packets with the given packet type.
	 * @param type - the type of the packets to stop intercepting.
	 * @return TRUE if we successfuly stopped intercepting a given packet ID, FALSE otherwise.
	 */
	public abstract boolean removePacketHandler(PacketType type);

	/**
	 * Determine if packets with the given packet type is being intercepted.
	 * @param type - the packet type to lookup.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	public abstract boolean hasPacketHandler(PacketType type);

	/**
	 * Invoked when input buffers have changed.
	 * @param set - the new set of packets that require the read buffer.
	 */
	public abstract void inputBuffersChanged(Set<PacketType> set);
	
	/**
	 * Retrieve every intercepted packet type.
	 * @return Every intercepted packet type.
	 */
	public abstract Set<PacketType> getPacketHandlers();

	/**
	 * Let the packet listeners process the given packet.
	 * @param packet - a packet to process.
	 * @param client - the client that sent the packet.
	 * @param buffered - a buffer containing the data that had to be read in order to construct the packet.
	 * @return The resulting packet event.
	 */
	public abstract PacketEvent packetRecieved(PacketContainer packet, Player client, byte[] buffered);

	/**
	 * Perform any necessary cleanup before unloading ProtocolLib.
	 */
	public abstract void cleanupAll();
}