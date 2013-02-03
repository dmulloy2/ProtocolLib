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

package com.comphenix.protocol.injector.player;

import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.concurrency.BlockingHashMap;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.player.TemporaryPlayerFactory.InjectContainer;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * Responsible for injecting into a player's sendPacket method.
 * 
 * @author Kristian
 */
public class PlayerInjectionHandler {
	/**
	 * The maximum number of milliseconds to wait until a player can be looked up by connection.
	 */
	private static final long TIMEOUT_PLAYER_LOOKUP = 2000; // ms
	
	/**
	 * The highest possible packet ID. It's unlikely that this value will ever change.
	 */
	private static final int MAXIMUM_PACKET_ID = 255;
	
	// Server connection injection
	private InjectedServerConnection serverInjection;
	
	// NetLogin injector
	private NetLoginInjector netLoginInjector;
	
	// The last successful player hook
	private PlayerInjector lastSuccessfulHook;
	
	// Player injection
	private Map<SocketAddress, PlayerInjector> addressLookup = Maps.newConcurrentMap();
	private Map<Player, PlayerInjector> playerInjection = Maps.newConcurrentMap();
	
	// Lookup player by connection
	private BlockingHashMap<DataInputStream, PlayerInjector> dataInputLookup = BlockingHashMap.create();
	
	// Player injection types
	private volatile PlayerInjectHooks loginPlayerHook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	private volatile PlayerInjectHooks playingPlayerHook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	
	// Error reporter
	private ErrorReporter reporter;
	
	// Whether or not we're closing
	private boolean hasClosed;

	// Used to invoke events
	private ListenerInvoker invoker;
	
	// Enabled packet filters
	private IntegerSet sendingFilters = new IntegerSet(MAXIMUM_PACKET_ID + 1);
	
	// List of packet listeners
	private Set<PacketListener> packetListeners;
	
	// The class loader we're using
	private ClassLoader classLoader;
	
	// Used to filter injection attempts
	private Predicate<GamePhase> injectionFilter;
	
	public PlayerInjectionHandler(ClassLoader classLoader, ErrorReporter reporter, Predicate<GamePhase> injectionFilter, 
								  ListenerInvoker invoker, Set<PacketListener> packetListeners, Server server) {
		
		this.classLoader = classLoader;
		this.reporter = reporter;
		this.invoker = invoker;
		this.injectionFilter = injectionFilter;
		this.packetListeners = packetListeners;
		this.netLoginInjector = new NetLoginInjector(reporter, this, server);
		this.serverInjection = new InjectedServerConnection(reporter, server, netLoginInjector);
		serverInjection.injectList();
	}

	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	public PlayerInjectHooks getPlayerHook() {
		return getPlayerHook(GamePhase.PLAYING);
	}
	
	/**
	 * Retrieves how the server packets are read.
	 * @param phase - the current game phase.
	 * @return Injection method for reading server packets.
	 */
	public PlayerInjectHooks getPlayerHook(GamePhase phase) {
		switch (phase) {
		case LOGIN:
			return loginPlayerHook;
		case PLAYING:
			return playingPlayerHook;
		default: 
			throw new IllegalArgumentException("Cannot retrieve injection hook for both phases at the same time.");
		}
	}

	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		setPlayerHook(GamePhase.PLAYING, playerHook);
	}
	
	/**
	 * Sets how the server packets are read.
	 * @param phase - the current game phase.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public void setPlayerHook(GamePhase phase, PlayerInjectHooks playerHook) {
		if (phase.hasLogin())
			loginPlayerHook = playerHook;
		if (phase.hasPlaying())
			playingPlayerHook = playerHook;
		
		// Make sure the current listeners are compatible
		checkListener(packetListeners);
	}
	
	/**
	 * Add an underlying packet handler of the given ID.
	 * @param packetID - packet ID to register.
	 */
	public void addPacketHandler(int packetID) {
		sendingFilters.add(packetID);
	}
	
	/**
	 * Remove an underlying packet handler of ths ID.  
	 * @param packetID - packet ID to unregister.
	 */
	public void removePacketHandler(int packetID) {
		sendingFilters.remove(packetID);
	}

	/**
	 * Used to construct a player hook.
	 * @param player - the player to hook.
	 * @param hook - the hook type.
	 * @return A new player hoook
	 * @throws IllegalAccessException Unable to do our reflection magic.
	 */
	private PlayerInjector getHookInstance(Player player, PlayerInjectHooks hook) throws IllegalAccessException {
		// Construct the correct player hook
		switch (hook) {
		case NETWORK_HANDLER_FIELDS: 
			return new NetworkFieldInjector(classLoader, reporter, player, invoker, sendingFilters);
		case NETWORK_MANAGER_OBJECT: 
			return new NetworkObjectInjector(classLoader, reporter, player, invoker, sendingFilters);
		case NETWORK_SERVER_OBJECT:
			return new NetworkServerInjector(classLoader, reporter, player, invoker, sendingFilters, serverInjection);
		default:
			throw new IllegalArgumentException("Cannot construct a player injector.");
		}
	}
	
	/**
	 * Retrieve a player by its DataInput connection.
	 * @param inputStream - the associated DataInput connection.
	 * @return The player.
	 * @throws InterruptedException If the thread was interrupted during the wait.
	 */
	public Player getPlayerByConnection(DataInputStream inputStream) throws InterruptedException {
		return getPlayerByConnection(inputStream, TIMEOUT_PLAYER_LOOKUP, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Retrieve a player by its DataInput connection.
	 * @param inputStream - the associated DataInput connection.
	 * @param playerTimeout - the amount of time to wait for a result.
	 * @param unit - unit of playerTimeout.
	 * @return The player. 
	 * @throws InterruptedException If the thread was interrupted during the wait.
	 */
	public Player getPlayerByConnection(DataInputStream inputStream, long playerTimeout, TimeUnit unit) throws InterruptedException {
		// Wait until the connection owner has been established
		PlayerInjector injector = dataInputLookup.get(inputStream, playerTimeout, unit);
		
		if (injector != null) {
			return injector.getPlayer();
		} else {
			reporter.reportWarning(this, "Unable to find stream: " + inputStream);
			return null;
		}
	}
	
	/**
	 * Helper function that retrieves the injector type of a given player injector.
	 * @param injector - injector type.
	 * @return The injector type.
	 */
	private PlayerInjectHooks getInjectorType(PlayerInjector injector) {
		return injector != null ? injector.getHookType() : PlayerInjectHooks.NONE;
	}
	
	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * <p>
	 * This call will  be ignored if there's no listener that can receive the given events.
	 * @param player - player to hook.
	 */
	public void injectPlayer(Player player) {
		// Inject using the player instance itself
		if (isInjectionNecessary(GamePhase.PLAYING)) {
			injectPlayer(player, player, GamePhase.PLAYING);
		}
	}
	
	/**
	 * Determine if it's truly necessary to perform the given player injection.
	 * @param phase - current game phase.
	 * @return TRUE if we should perform the injection, FALSE otherwise.
	 */
	public boolean isInjectionNecessary(GamePhase phase) {
		return injectionFilter.apply(phase);
	}
	
	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * <p>
	 * This method will always perform the instructed injection.
	 * 
	 * @param player - player to hook.
	 * @param injectionPoint - the object to use during the injection process.
	 * @param phase - the current game phase.
	 * @return The resulting player injector, or NULL if the injection failed.
	 */
	PlayerInjector injectPlayer(Player player, Object injectionPoint, GamePhase phase) {
		// Unfortunately, due to NetLoginHandler, multiple threads may potentially call this method.
		synchronized (player) {
			return injectPlayerInternal(player, injectionPoint, phase);
		}
	}
	
	// Unsafe variant of the above
	private PlayerInjector injectPlayerInternal(Player player, Object injectionPoint, GamePhase phase) {
		
		PlayerInjector injector = playerInjection.get(player);
		PlayerInjectHooks tempHook = getPlayerHook(phase);
		PlayerInjectHooks permanentHook = tempHook;
		
		// The given player object may be fake, so be careful!
		
		// See if we need to inject something else
		boolean invalidInjector = injector != null ? !injector.canInject(phase) : true;

		// Don't inject if the class has closed
		if (!hasClosed && player != null && (tempHook != getInjectorType(injector) || invalidInjector)) {
			while (tempHook != PlayerInjectHooks.NONE) {
				// Whether or not the current hook method failed completely
				boolean hookFailed = false;

				// Remove the previous hook, if any
				cleanupHook(injector);
				
				try {
					injector = getHookInstance(player, tempHook);
					
					// Make sure this injection method supports the current game phase
					if (injector.canInject(phase)) {
						injector.initialize(injectionPoint);
						
						DataInputStream inputStream = injector.getInputStream(false);

						Socket socket = injector.getSocket();
						SocketAddress address = socket != null ? socket.getRemoteSocketAddress() : null;
						
						// Guard against NPE here too
						PlayerInjector previous = address != null ? addressLookup.get(address) : null;
						
						// Close any previously associated hooks before we proceed
						if (previous != null) {
							uninjectPlayer(previous.getPlayer(), false, true);
						}
	
						injector.injectManager();
						
						if (inputStream != null)
							dataInputLookup.put(inputStream, injector);
						if (address != null) 
							addressLookup.put(address, injector);
						break;
					}
					
				} catch (PlayerLoggedOutException e) {
					throw e;
					
				} catch (Exception e) {
					// Mark this injection attempt as a failure
					reporter.reportDetailed(this, "Player hook " + tempHook.toString() + " failed.", 
											 e, player, injectionPoint, phase);
					hookFailed = true;
				}
				
				// Choose the previous player hook type
				tempHook = PlayerInjectHooks.values()[tempHook.ordinal() - 1];
				
				if (hookFailed)
					reporter.reportWarning(this, "Switching to " + tempHook.toString() + " instead.");
				
				// Check for UTTER FAILURE
				if (tempHook == PlayerInjectHooks.NONE) {
					cleanupHook(injector);
					injector = null;
					hookFailed = true;
				}
				
				// Should we set the default hook method too?
				if (hookFailed) {
					permanentHook = tempHook;
				}
			}
			
			// Update values
			if (injector != null)
				lastSuccessfulHook = injector;
			if (permanentHook != getPlayerHook(phase)) 
				setPlayerHook(phase, tempHook);
			
			// Save injector
			if (injector != null) {
				playerInjection.put(player, injector);
			}
		}
		
		return injector;
	}
	
	private void cleanupHook(PlayerInjector injector) {
		// Clean up as much as possible
		try {
			if (injector != null)
				injector.cleanupAll();
		} catch (Exception ex) {
			reporter.reportDetailed(this, "Cleaing up after player hook failed.", ex, injector);
		}
	}
	
	/**
	 * Invoke special routines for handling disconnect before a player is uninjected.
	 * @param player - player to process.
	 */
	public void handleDisconnect(Player player) {
		PlayerInjector injector = getInjector(player);
		
		if (injector != null) {
			injector.handleDisconnect();
		}
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	public boolean uninjectPlayer(Player player) {
		return uninjectPlayer(player, true, false);
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 * @param removeAuxiliary - TRUE to remove auxiliary information, such as input stream and address.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	public boolean uninjectPlayer(Player player, boolean removeAuxiliary) {
		return uninjectPlayer(player, removeAuxiliary, false);
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 * @param removeAuxiliary - TRUE to remove auxiliary information, such as input stream and address.
	 * @param prepareNextHook - whether or not we need to fix any lingering hooks.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	private boolean uninjectPlayer(Player player, boolean removeAuxiliary, boolean prepareNextHook) {
		if (!hasClosed && player != null) {
			
			PlayerInjector injector = playerInjection.remove(player);

			if (injector != null) {
				InetSocketAddress address = player.getAddress();
				injector.cleanupAll();
				
				// Remove the "hooked" network manager in our instance as well
				if (prepareNextHook && injector instanceof NetworkObjectInjector) {
					try {
						PlayerInjector dummyInjector = getHookInstance(player, PlayerInjectHooks.NETWORK_SERVER_OBJECT);
						dummyInjector.initializePlayer(player);
						dummyInjector.setNetworkManager(injector.getNetworkManager(), true);

					} catch (IllegalAccessException e) {
						// Let the user know
						reporter.reportWarning(this, "Unable to fully revert old injector. May cause conflicts.", e);
					}
				}
				
				// Clean up
				if (removeAuxiliary) {
					// Note that the dataInputLookup will clean itself
					if (address != null) 
						addressLookup.remove(address);
				}
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Unregisters a player by the given address.
	 * <p>
	 * If the server handler has been created before we've gotten a chance to unject the player,
	 * the method will try a workaround to remove the injected hook in the NetServerHandler.
	 * 
	 * @param address - address of the player to unregister.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	public boolean uninjectPlayer(InetSocketAddress address) {
		if (!hasClosed && address != null) {
			PlayerInjector injector = addressLookup.get(address);
			
			// Clean up
			if (injector != null)
				uninjectPlayer(injector.getPlayer(), false, true);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the given packet to the given reciever.
	 * @param reciever - the player receiver.
	 * @param packet - the packet to send.
	 * @param filters - whether or not to invoke the packet filters.
	 * @throws InvocationTargetException If an error occured during sending.
	 */
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
		PlayerInjector injector = getInjector(reciever);
		
		// Send the packet, or drop it completely
		if (injector != null) {
			injector.sendServerPacket(packet.getHandle(), filters);
		} else {
			throw new PlayerLoggedOutException(String.format(
					"Unable to send packet %s (%s): Player %s has logged out.", 
					packet.getID(), packet, reciever.getName()
			));
		}
	}
	
	/**
	 * Process a packet as if it were sent by the given player.
	 * @param player - the sender.
	 * @param mcPacket - the packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
		
		PlayerInjector injector = getInjector(player);
		
		// Process the given packet, or simply give up
		if (injector != null)
			injector.processPacket(mcPacket);
		else
			throw new PlayerLoggedOutException(String.format(
					"Unable to receieve packet %s. Player %s has logged out.", 
					mcPacket, player.getName()
			));
	}
	
	/**
	 * Retrieve the injector associated with this player.
	 * @param player - the player to find.
	 * @return The injector, or NULL if not found.
	 */
	private PlayerInjector getInjector(Player player) {
		PlayerInjector injector = playerInjection.get(player);
		
		if (injector == null) {
			// Try getting it from the player itself
			if (player instanceof InjectContainer)
				return ((InjectContainer) player).getInjector();
			else
				return searchAddressLookup(player);
		} else {
			return injector;
		}
	}
	
	/**
	 * Find an injector by looking through the address map.
	 * @param player - player to find.
	 * @return The injector, or NULL if not found.
	 */
	private PlayerInjector searchAddressLookup(Player player) {
		// See if we can find it anywhere
		for (PlayerInjector injector : addressLookup.values()) {
			if (player.equals(injector.getUpdatedPlayer())) {
				return injector;
			}
		}
		return null;
	}
	
	/**
	 * Retrieve a player injector by looking for its NetworkManager.
	 * @param networkManager - current network manager.
	 * @return Related player injector.
	 */
	PlayerInjector getInjectorByNetworkHandler(Object networkManager) {
		// That's not legal
		if (networkManager == null)
			return null;
		
		// O(n) is okay in this instance. This is only a backup solution.
		for (PlayerInjector injector : playerInjection.values()) {
			if (injector.getNetworkManager() == networkManager)
				return injector;
		}
		
		// None found
		return null;
	}
	
	/**
	 * Determine if the given listeners are valid.
	 * @param listeners - listeners to check.
	 */
	public void checkListener(Set<PacketListener> listeners) {
		// Make sure the current listeners are compatible
		if (lastSuccessfulHook != null) {
			for (PacketListener listener : listeners) {
				checkListener(listener);
			}
		}
	}
	
	/**
	 * Determine if a listener is valid or not.
	 * <p>
	 * If not, a warning will be printed to the console. 
	 * @param listener - listener to check.
	 */
	public void checkListener(PacketListener listener) {
		if (lastSuccessfulHook != null) {
			UnsupportedListener result = lastSuccessfulHook.checkListener(listener);

			// We won't prevent the listener, as it may still have valid packets
			if (result != null) {
				reporter.reportWarning(this, "Cannot fully register listener for " + 
						  PacketAdapter.getPluginName(listener) + ": " + result.toString());
				
				// These are illegal
				for (int packetID : result.getPackets())
					removePacketHandler(packetID);
			}
		}
	}
	
	/**
	 * Retrieve the current list of registered sending listeners.
	 * @return List of the sending listeners's packet IDs.
	 */
	public Set<Integer> getSendingFilters() {
		return sendingFilters.toSet();
	}
	
	public void close() {
		// Guard
		if (hasClosed || playerInjection == null)
			return;

		// Remove everything
		for (PlayerInjector injection : playerInjection.values()) {
			if (injection != null) {
				injection.cleanupAll();
			}
		}
		
		// Remove server handler
		if (serverInjection != null)
			serverInjection.cleanupAll();
		if (netLoginInjector != null)
			netLoginInjector.cleanupAll();
		serverInjection = null;
		netLoginInjector = null;
		hasClosed = true;
		
		playerInjection.clear();
		addressLookup.clear();
		invoker = null;
	}

	/**
	 * Inform the current PlayerInjector that it should update the DataInputStream next.
	 * @param player - the player to update.
	 */
	public void scheduleDataInputRefresh(Player player) {
		final PlayerInjector injector = getInjector(player);

		// Update the DataInputStream
		if (injector != null) {
			injector.scheduleAction(new Runnable() {
				@Override
				public void run() {
					dataInputLookup.put(injector.getInputStream(false), injector);
				}
			});
		}
	}
}
