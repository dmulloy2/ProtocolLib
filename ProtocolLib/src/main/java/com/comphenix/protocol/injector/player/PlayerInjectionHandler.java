package com.comphenix.protocol.injector.player;

import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
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

/**
 * Responsible for injecting into a player's sendPacket method.
 * 
 * @author Kristian
 */
public class PlayerInjectionHandler {

	// Server connection injection
	private InjectedServerConnection serverInjection;
	
	// The last successful player hook
	private PlayerInjector lastSuccessfulHook;
	
	// Player injection
	private Map<DataInputStream, Player> connectionLookup = new ConcurrentHashMap<DataInputStream, Player>();
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
		this.serverInjection = new InjectedServerConnection(logger, server);
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
			return new NetworkObjectInjector(logger, player, invoker, sendingFilters);
		case NETWORK_SERVER_OBJECT:
			return new NetworkServerInjector(classLoader, logger, player, invoker, sendingFilters, serverInjection);
		default:
			throw new IllegalArgumentException("Cannot construct a player injector.");
		}
	}
	
	public Player getPlayerByConnection(DataInputStream inputStream) {
		return connectionLookup.get(inputStream);
	}
	
	/**
	 * Initialize a player hook, allowing us to read server packets.
	 * @param player - player to hook.
	 */
	public void injectPlayer(Player player) {
		
		PlayerInjector injector = null;
		PlayerInjectHooks currentHook = playerHook;
		boolean firstPlayer = lastSuccessfulHook == null;
		
		// Don't inject if the class has closed
		if (!hasClosed && player != null && !playerInjection.containsKey(player)) {
			while (true) {
				try {
					injector = getHookInstance(player, currentHook);
					injector.initialize();
					injector.injectManager();
					
					DataInputStream inputStream = injector.getInputStream(false);
					
					if (!player.isOnline() || inputStream == null) {
						throw new PlayerLoggedOutException();
					}
					
					playerInjection.put(player, injector);
					connectionLookup.put(inputStream, player);
					break;
					
					
				} catch (PlayerLoggedOutException e) {
					throw e;
					
				} catch (Exception e) {

					// Mark this injection attempt as a failure
					logger.log(Level.SEVERE, "Player hook " + currentHook.toString() + " failed.", e);
					
					// Clean up as much as possible
					try {
						if (injector != null)
							injector.cleanupAll();
					} catch (Exception e2) {
						logger.log(Level.WARNING, "Cleaing up after player hook failed.", e);
					}
					
					if (currentHook.ordinal() > 0) {

						// Choose the previous player hook type
						currentHook = PlayerInjectHooks.values()[currentHook.ordinal() - 1];
						logger.log(Level.INFO, "Switching to " + currentHook.toString() + " instead.");
					} else {
						// UTTER FAILURE
						playerInjection.put(player, null);
						return;
					}
				}
			}
			
			// Update values
			if (injector != null)
				lastSuccessfulHook = injector;
			if (currentHook != playerHook || firstPlayer) 
				setPlayerHook(currentHook);
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
				injector.cleanupAll();
				
				playerInjection.remove(player);
				connectionLookup.remove(input);
			}
		}	
	}
	
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
		getInjector(reciever).sendServerPacket(packet.getHandle(), filters);
	}
	
	private PlayerInjector getInjector(Player player) {
		if (!playerInjection.containsKey(player)) {
			// What? Try to inject again.
			injectPlayer(player);
		}
		
		PlayerInjector injector = playerInjection.get(player);
		
		// Check that the injector was sucessfully added
		if (injector != null)
			return injector;
		else
			throw new IllegalArgumentException("Player has no injected handler.");
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
	 * Process a packet as if it were sent by the given player.
	 * @param player - the sender.
	 * @param mcPacket - the packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Player player, Packet mcPacket) throws IllegalAccessException, InvocationTargetException {
		
		PlayerInjector injector = getInjector(player);
		injector.processPacket(mcPacket);
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
		serverInjection.cleanupAll();
		hasClosed = true;
		
		playerInjection.clear();
		connectionLookup.clear();
		invoker = null;
	}
}
