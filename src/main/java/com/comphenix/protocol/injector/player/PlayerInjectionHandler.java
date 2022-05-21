package com.comphenix.protocol.injector.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.util.Set;

public interface PlayerInjectionHandler {

	/**
	 * Retrieve the protocol version of the given player.
	 *
	 * @param player - the player.
	 * @return The protocol version, or {@link Integer#MIN_VALUE}.
	 */
	int getProtocolVersion(Player player);

	/**
	 * Add an underlying packet handler of the given type.
	 *
	 * @param type    - packet type to register.
	 * @param options - any specified listener options.
	 */
	void addPacketHandler(PacketType type, Set<ListenerOptions> options);

	/**
	 * Remove an underlying packet handler of this type.
	 *
	 * @param type - packet type to unregister.
	 */
	void removePacketHandler(PacketType type);

	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * <p>
	 * This call will  be ignored if there's no listener that can receive the given events.
	 *
	 * @param player   - player to hook.
	 * @param strategy - how to handle injection conflicts.
	 */
	void injectPlayer(Player player, ConflictStrategy strategy);

	/**
	 * Invoke special routines for handling disconnect before a player is uninjected.
	 *
	 * @param player - player to process.
	 */
	void handleDisconnect(Player player);

	/**
	 * Uninject the given player.
	 *
	 * @param player - player to uninject.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	boolean uninjectPlayer(Player player);

	/**
	 * Send the given packet to the given receiver.
	 *
	 * @param receiver - the player receiver.
	 * @param packet   - the packet to send.
	 * @param marker   - network marker.
	 * @param filters  - whether or not to invoke the packet filters.
	 */
	void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters);

	/**
	 * Process a packet as if it were sent by the given player.
	 *
	 * @param player   - the sender.
	 * @param mcPacket - the packet to process.
	 */
	void receiveClientPacket(Player player, Object mcPacket);

	/**
	 * Ensure that packet readers are informed of this player reference.
	 *
	 * @param player - the player to update.
	 */
	void updatePlayer(Player player);

	/**
	 * Determine if the given listeners are valid.
	 *
	 * @param listeners - listeners to check.
	 */
	void checkListener(Set<PacketListener> listeners);

	/**
	 * Determine if a listener is valid or not.
	 * <p>
	 * If not, a warning will be printed to the console.
	 *
	 * @param listener - listener to check.
	 */
	void checkListener(PacketListener listener);

	/**
	 * Retrieve the current list of registered sending listeners.
	 *
	 * @return List of the sending listeners's packet IDs.
	 */
	Set<PacketType> getSendingFilters();

	/**
	 * Whether or not this player injection handler can also receive packets.
	 *
	 * @return TRUE if it can, FALSE otherwise.
	 */
	boolean canReceivePackets();

	/**
	 * Close any lingering proxy injections.
	 */
	void close();

	/**
	 * Determine if we have packet listeners with the given type that must be executed on the main thread.
	 * <p>
	 * This only applies for onPacketSending(), as it makes certain guarantees.
	 *
	 * @param type - the packet type.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	boolean hasMainThreadListener(PacketType type);

	Channel getChannel(Player player);

	/**
	 * How to handle a previously existing player injection.
	 *
	 * @author Kristian
	 */
	enum ConflictStrategy {
		/**
		 * Override it.
		 */
		OVERRIDE,

		/**
		 * Immediately exit.
		 */
		BAIL_OUT;
	}
}
