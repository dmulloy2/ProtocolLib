package com.comphenix.protocol.injector.player;

import java.io.DataInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.Set;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;

public interface PlayerInjectionHandler {
	/**
	 * How to handle a previously existing player injection.
	 * 
	 * @author Kristian
	 */
	public enum ConflictStrategy {
		/**
		 * Override it.
		 */
		OVERRIDE,
		
		/**
		 * Immediately exit.
		 */
		BAIL_OUT;
	}
	
	/**
	 * Retrieve the protocol version of the given player.
	 * @param player - the player.
	 * @return The protocol version, or {@link Integer#MIN_VALUE}.
	 */
	public abstract int getProtocolVersion(Player player);
	
	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	public abstract PlayerInjectHooks getPlayerHook();

	/**
	 * Retrieves how the server packets are read.
	 * @param phase - the current game phase.
	 * @return Injection method for reading server packets.
	 */
	public abstract PlayerInjectHooks getPlayerHook(GamePhase phase);

	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public abstract void setPlayerHook(PlayerInjectHooks playerHook);

	/**
	 * Sets how the server packets are read.
	 * @param phase - the current game phase.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public abstract void setPlayerHook(GamePhase phase, PlayerInjectHooks playerHook);

	/**
	 * Add an underlying packet handler of the given type.
	 * @param type - packet type to register.
	 * @param options - any specified listener options.
	 */
	public abstract void addPacketHandler(PacketType type, Set<ListenerOptions> options);

	/**
	 * Remove an underlying packet handler of this type.  
	 * @param type - packet type to unregister.
	 */
	public abstract void removePacketHandler(PacketType type);

	/**
	 * Retrieve a player by its DataInput connection.
	 * @param inputStream - the associated DataInput connection.
	 * @return The player.
	 * @throws InterruptedException If the thread was interrupted during the wait.
	 */
	public abstract Player getPlayerByConnection(DataInputStream inputStream)
			throws InterruptedException;

	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * <p>
	 * This call will  be ignored if there's no listener that can receive the given events.
	 * @param player - player to hook.
	 * @param strategy - how to handle injection conflicts.
	 */
	public abstract void injectPlayer(Player player, ConflictStrategy strategy);

	/**
	 * Invoke special routines for handling disconnect before a player is uninjected.
	 * @param player - player to process.
	 */
	public abstract void handleDisconnect(Player player);

	/**
	 * Uninject the given player.
	 * @param player - player to uninject.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	public abstract boolean uninjectPlayer(Player player);
	
	/**
	 * Unregisters a player by the given address.
	 * <p>
	 * If the server handler has been created before we've gotten a chance to unject the player,
	 * the method will try a workaround to remove the injected hook in the NetServerHandler.
	 * 
	 * @param address - address of the player to unregister.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	public abstract boolean uninjectPlayer(InetSocketAddress address);

	/**
	 * Send the given packet to the given receiver.
	 * @param receiver - the player receiver.
	 * @param packet - the packet to send.
	 * @param marker 
	 * @param filters - whether or not to invoke the packet filters.
	 * @throws InvocationTargetException If an error occurred during sending.
	 */
	public abstract void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters)
			throws InvocationTargetException;

	/**
	 * Process a packet as if it were sent by the given player.
	 * @param player - the sender.
	 * @param mcPacket - the packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public abstract void recieveClientPacket(Player player, Object mcPacket)
			throws IllegalAccessException, InvocationTargetException;

	/**
	 * Ensure that packet readers are informed of this player reference.
	 * @param player - the player to update.
	 */
	public abstract void updatePlayer(Player player);
	
	/**
	 * Determine if the given listeners are valid.
	 * @param listeners - listeners to check.
	 */
	public abstract void checkListener(Set<PacketListener> listeners);

	/**
	 * Determine if a listener is valid or not.
	 * <p>
	 * If not, a warning will be printed to the console. 
	 * @param listener - listener to check.
	 */
	public abstract void checkListener(PacketListener listener);

	/**
	 * Retrieve the current list of registered sending listeners.
	 * @return List of the sending listeners's packet IDs.
	 */
	public abstract Set<PacketType> getSendingFilters();

	/**
	 * Whether or not this player injection handler can also receive packets.
	 * @return TRUE if it can, FALSE otherwise.
	 */
	public abstract boolean canRecievePackets();
	
	/**
	 * Invoked if this player injection handler can process received packets.
	 * @param packet - the received packet.
	 * @param input - the input stream.
	 * @param buffered - the buffered packet.
	 * @return The packet event.
	 */
	public abstract PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered);
	
	/**
	 * Close any lingering proxy injections.
	 */
	public abstract void close();
	
	/**
	 * Determine if we have packet listeners with the given type that must be executed on the main thread.
	 * <p>
	 * This only applies for onPacketSending(), as it makes certain guarantees.
	 * @param type - the packet type.
	 * @return TRUE if we do, FALSE otherwise.
	 */
	public abstract boolean hasMainThreadListener(PacketType type);
}