package com.comphenix.protocol.injector.packet;

import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;

/**
 * Represents an incoming packet injector.
 *
 * @author Kristian
 */
public interface PacketInjector {

	/**
	 * Start intercepting packets with the given packet type.
	 *
	 * @param type    - the type of the packets to start intercepting.
	 * @param options - any listener options.
	 * @return TRUE if we didn't already intercept these packets, FALSE otherwise.
	 */
	boolean addPacketHandler(PacketType type, Set<ListenerOptions> options);

	/**
	 * Stop intercepting packets with the given packet type.
	 *
	 * @param type - the type of the packets to stop intercepting.
	 * @return TRUE if we successfuly stopped intercepting a given packet ID, FALSE otherwise.
	 */
	boolean removePacketHandler(PacketType type);

	/**
	 * Determine if packets with the given packet type is being intercepted.
	 *
	 * @param type - the packet type to lookup.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	boolean hasPacketHandler(PacketType type);

	/**
	 * Retrieve every intercepted packet type.
	 *
	 * @return Every intercepted packet type.
	 */
	Set<PacketType> getPacketHandlers();

	/**
	 * Let the packet listeners process the given packet.
	 *
	 * @param packet - a packet to process.
	 * @param client - the client that sent the packet.
	 * @return The resulting packet event.
	 */
	PacketEvent packetReceived(PacketContainer packet, Player client);

	/**
	 * Determine if we have packet listeners with the given type that must be executed on the main thread.
	 *
	 * @param type - the packet type.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	boolean hasMainThreadListener(PacketType type);

	/**
	 * Perform any necessary cleanup before unloading ProtocolLib.
	 */
	void cleanupAll();
}
