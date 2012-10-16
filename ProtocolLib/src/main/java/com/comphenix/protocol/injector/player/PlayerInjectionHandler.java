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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.Packet;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

/**
 * Responsible for injecting into a player's sendPacket method.
 * 
 * @author Kristian
 */
public class PlayerInjectionHandler {

	/**
	 * The current player phase.
	 * @author Kristian
	 */
	enum GamePhase {
		LOGIN,
		PLAYING,
		CLOSING,
	}
	
	// Server connection injection
	private InjectedServerConnection serverInjection;
	
	// NetLogin injector
	private NetLoginInjector netLoginInjector;
	
	// The last successful player hook
	private PlayerInjector lastSuccessfulHook;
	
	// Player injection
	private Map<SocketAddress, PlayerInjector> addressLookup = Maps.newConcurrentMap();
	private Map<DataInputStream, PlayerInjector> dataInputLookup = Maps.newConcurrentMap();
	private Map<Player, PlayerInjector> playerInjection = new HashMap<Player, PlayerInjector>();
	
	// Player injection type
	private PlayerInjectHooks playerHook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	
	// Error logger
	private Logger logger;
	
	// Whether or not we're closing
	private boolean hasClosed;

	// Used to invoke events
	private ListenerInvoker invoker;
	
	// Enabled packet filters
	private Set<Integer> sendingFilters = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());
	
	// The class loader we're using
	private ClassLoader classLoader;
	
	public PlayerInjectionHandler(ClassLoader classLoader, Logger logger, ListenerInvoker invoker, Server server) {
		this.classLoader = classLoader;
		this.logger = logger;
		this.invoker = invoker;
		this.netLoginInjector = new NetLoginInjector(this, server);
		this.serverInjection = new InjectedServerConnection(logger, server, netLoginInjector);
		serverInjection.injectList();
	}

	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	public PlayerInjectHooks getPlayerHook() {
		return playerHook;
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
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		this.playerHook = playerHook;
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
			return new NetworkFieldInjector(classLoader, logger, player, invoker, sendingFilters);
		case NETWORK_MANAGER_OBJECT: 
			return new NetworkObjectInjector(classLoader, logger, player, invoker, sendingFilters);
		case NETWORK_SERVER_OBJECT:
			return new NetworkServerInjector(classLoader, logger, player, invoker, sendingFilters, serverInjection);
		default:
			throw new IllegalArgumentException("Cannot construct a player injector.");
		}
	}
	
	/**
	 * Retrieve a player by its DataInput connection.
	 * @param inputStream - the associated DataInput connection.
	 * @return The player.
	 */
	public Player getPlayerByConnection(DataInputStream inputStream) {
		try {
			// Concurrency issue!
			netLoginInjector.getReadLock().lock();
			
			PlayerInjector injector = dataInputLookup.get(inputStream);
			
			if (injector != null) {
				return injector.getPlayer();
			} else {
				System.out.println("Unable to find stream: " + inputStream);
				return null;
			}
			
		} finally {
			netLoginInjector.getReadLock().unlock();
		}
	}
	
	private PlayerInjectHooks getInjectorType(PlayerInjector injector) {
		return injector != null ? injector.getHookType() : PlayerInjectHooks.NONE;
	}
	
	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * @param player - player to hook.
	 */
	public void injectPlayer(Player player) {
		// Inject using the player instance itself
		injectPlayer(player, player, GamePhase.PLAYING);
	}
	
	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * @param player - player to hook.
	 * @param injectionPoint - the object to use during the injection process.
	 * @param phase - the current game phase.
	 * @return The resulting player injector, or NULL if the injection failed.
	 */
	PlayerInjector injectPlayer(Player player, Object injectionPoint, GamePhase phase) {

		PlayerInjector injector = playerInjection.get(player);
		PlayerInjectHooks tempHook = playerHook;
		PlayerInjectHooks permanentHook = tempHook;
		
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
						SocketAddress address = socket.getRemoteSocketAddress();
						
						// Make sure the current player is not logged out
						if (socket.isClosed()) {
							throw new PlayerLoggedOutException();
						}
						
						PlayerInjector previous = addressLookup.get(address);
						
						// Close any previously associated hooks before we proceed
						if (previous != null) {
							uninjectPlayer(previous.getPlayer());
							
							// Update the player object too
							previous.setPlayer(player);
							
							// Remove the "hooked" network manager in our instance as well
							if (previous instanceof NetworkObjectInjector) {
								injector.setNetworkManager(previous.getNetworkManager(), true);
							}
						}

						injector.injectManager();
						dataInputLookup.put(inputStream, injector);
						addressLookup.put(address, injector);
						break;
					}
					
				} catch (PlayerLoggedOutException e) {
					throw e;
					
				} catch (Exception e) {
					// Mark this injection attempt as a failure
					logger.log(Level.SEVERE, "Player hook " + tempHook.toString() + " failed.", e);
					hookFailed = true;
				}
				
				// Choose the previous player hook type
				tempHook = PlayerInjectHooks.values()[tempHook.ordinal() - 1];
				
				if (hookFailed)
					logger.log(Level.INFO, "Switching to " + tempHook.toString() + " instead.");
				
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
			if (permanentHook != playerHook) 
				setPlayerHook(tempHook);
			
			// Save last injector
			playerInjection.put(player, injector);
		}
		
		return injector;
	}
	
	private void cleanupHook(PlayerInjector injector) {
		// Clean up as much as possible
		try {
			if (injector != null)
				injector.cleanupAll();
		} catch (Exception e2) {
			logger.log(Level.WARNING, "Cleaing up after player hook failed.", e2);
		}
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 */
	public void uninjectPlayer(Player player) {
		if (!hasClosed && player != null) {
			
			PlayerInjector injector = playerInjection.get(player);
			
			if (injector != null) {
				DataInputStream input = injector.getInputStream(true);
				InetSocketAddress address = player.getAddress();
				injector.cleanupAll();
				
				playerInjection.remove(player);
				dataInputLookup.remove(input);
				
				if (address != null)
					addressLookup.remove(address);
			}
		}	
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
		if (injector != null)
			injector.sendServerPacket(packet.getHandle(), filters);
		else
			logger.log(Level.WARNING, String.format(
					"Unable to send packet %s (%s): Player %s has logged out.", 
					packet.getID(), packet, reciever.getName()
			));
	}
	
	/**
	 * Process a packet as if it were sent by the given player.
	 * @param player - the sender.
	 * @param mcPacket - the packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Player player, Packet mcPacket) throws IllegalAccessException, InvocationTargetException {
		
		PlayerInjector injector = getInjector(player);
		
		// Process the given packet, or simply give up
		if (injector != null)
			injector.processPacket(mcPacket);
		else
			logger.log(Level.WARNING, String.format(
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
		return playerInjection.get(player);
	}
	
	/**
	 * Determine if the given listeners are valid.
	 * @param listeners - listeners to check.
	 */
	public void checkListener(Set<PacketListener> listeners) {
		// Make sure the current listeners are compatible
		if (lastSuccessfulHook != null) {
			for (PacketListener listener : listeners) {
				try {
					checkListener(listener);
				} catch (IllegalStateException e) {
					logger.log(Level.WARNING, "Unsupported listener.", e);
				}
			}
		}
	}
	
	/**
	 * Determine if a listener is valid or not.
	 * @param listener - listener to check.
	 * @throws IllegalStateException If the given listener's whitelist cannot be fulfilled.
	 */
	public void checkListener(PacketListener listener) {
		try {
			if (lastSuccessfulHook != null)
				lastSuccessfulHook.checkListener(listener);
		} catch (Exception e) {
			throw new IllegalStateException("Registering listener " + PacketAdapter.getPluginName(listener) + " failed", e);
		}
	}
	
	/**
	 * Retrieve the current list of registered sending listeners.
	 * @return List of the sending listeners's packet IDs.
	 */
	public Set<Integer> getSendingFilters() {
		return ImmutableSet.copyOf(sendingFilters);
	}
	
	/**
	 * Retrieve the current logger.
	 * @return Error logger.
	 */
	public Logger getLogger() {
		return logger;
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
		dataInputLookup.clear();
		addressLookup.clear();
		invoker = null;
	}

	/**
	 * Inform the current PlayerInjector that it should update the DataInputStream next.
	 * @param player - the player to update.
	 */
	public void scheduleDataInputRefresh(Player player) {
		final PlayerInjector injector = getInjector(player);
		final DataInputStream old = injector.getInputStream(true);
		
		// Update the DataInputStream
		if (injector != null) {
			injector.scheduleAction(new Runnable() {
				@Override
				public void run() {
					dataInputLookup.remove(old);
					dataInputLookup.put(injector.getInputStream(false), injector);
				}
			});
		}
	}
}
