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
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import net.sf.cglib.proxy.Factory;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.concurrency.BlockingHashMap;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.PlayerLoggedOutException;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.server.AbstractInputStreamLookup;
import com.comphenix.protocol.injector.server.BukkitSocketInjector;
import com.comphenix.protocol.injector.server.InputStreamLookupBuilder;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.SafeCacheBuilder;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * Responsible for injecting into a player's sendPacket method.
 * 
 * @author Kristian
 */
class ProxyPlayerInjectionHandler implements PlayerInjectionHandler {
	// Warnings and errors
	public static final ReportType REPORT_UNSUPPPORTED_LISTENER = new ReportType("Cannot fully register listener for %s: %s");
	
	// Fallback to older player hook types
	public static final ReportType REPORT_PLAYER_HOOK_FAILED = new ReportType("Player hook %s failed.");
	public static final ReportType REPORT_SWITCHED_PLAYER_HOOK = new ReportType("Switching to %s instead.");
	
	public static final ReportType REPORT_HOOK_CLEANUP_FAILED = new ReportType("Cleaing up after player hook failed.");
	public static final ReportType REPORT_CANNOT_REVERT_HOOK = new ReportType("Unable to fully revert old injector. May cause conflicts.");
	
	// Server connection injection
	private InjectedServerConnection serverInjection;
	
	// Server socket injection
	private AbstractInputStreamLookup inputStreamLookup;
	
	// NetLogin injector
	private NetLoginInjector netLoginInjector;
	
	// The last successful player hook
	private WeakReference<PlayerInjector> lastSuccessfulHook;
	
	// Dummy injection
	private ConcurrentMap<Player, PlayerInjector> dummyInjectors = 
			SafeCacheBuilder.newBuilder().
			expireAfterWrite(30, TimeUnit.SECONDS).
			build(BlockingHashMap.<Player, PlayerInjector>newInvalidCacheLoader());
	
	// Player injection
	private Map<Player, PlayerInjector> playerInjection = Maps.newConcurrentMap();
	
	// Player injection types
	private volatile PlayerInjectHooks loginPlayerHook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	private volatile PlayerInjectHooks playingPlayerHook = PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	
	// Error reporter
	private ErrorReporter reporter;
	
	// Whether or not we're closing
	private boolean hasClosed;

	// Used to invoke events
	private ListenerInvoker invoker;
	
	// Current Minecraft version
	private MinecraftVersion version;
	
	// Enabled packet filters
	private IntegerSet sendingFilters = new IntegerSet(Packets.MAXIMUM_PACKET_ID + 1);
	
	// List of packet listeners
	private Set<PacketListener> packetListeners;
	
	// Used to filter injection attempts
	private Predicate<GamePhase> injectionFilter;
	
	public ProxyPlayerInjectionHandler(
			ErrorReporter reporter, Predicate<GamePhase> injectionFilter, 
			ListenerInvoker invoker, Set<PacketListener> packetListeners, Server server, MinecraftVersion version) {
		
		this.reporter = reporter;
		this.invoker = invoker;
		this.injectionFilter = injectionFilter;
		this.packetListeners = packetListeners;
		this.version = version;
	
		this.inputStreamLookup = InputStreamLookupBuilder.newBuilder().
							  server(server).
							  reporter(reporter).
							  build();
		
		// Create net login injectors and the server connection injector
		this.netLoginInjector = new NetLoginInjector(reporter, server, this);
		this.serverInjection = new InjectedServerConnection(reporter, inputStreamLookup, server, netLoginInjector);
		serverInjection.injectList();
	}
	
	@Override
	public int getProtocolVersion(Player player) {
		// Just use the server version
		return MinecraftProtocolVersion.getCurrentVersion();
	}

	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	@Override
	public PlayerInjectHooks getPlayerHook() {
		return getPlayerHook(GamePhase.PLAYING);
	}
	
	/**
	 * Retrieves how the server packets are read.
	 * @param phase - the current game phase.
	 * @return Injection method for reading server packets.
	 */
	@Override
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

	@Override
	public boolean hasMainThreadListener(PacketType type) {
		return sendingFilters.contains(type.getLegacyId());
	}
	
	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	@Override
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		setPlayerHook(GamePhase.PLAYING, playerHook);
	}
	
	/**
	 * Sets how the server packets are read.
	 * @param phase - the current game phase.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	@Override
	public void setPlayerHook(GamePhase phase, PlayerInjectHooks playerHook) {
		if (phase.hasLogin())
			loginPlayerHook = playerHook;
		if (phase.hasPlaying())
			playingPlayerHook = playerHook;
		
		// Make sure the current listeners are compatible
		checkListener(packetListeners);
	}
	
	@Override
	public void addPacketHandler(PacketType type, Set<ListenerOptions> options) {
		sendingFilters.add(type.getLegacyId());
	}
	
	@Override
	public void removePacketHandler(PacketType type) {
		sendingFilters.remove(type.getLegacyId());
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
			return new NetworkFieldInjector(reporter, player, invoker, sendingFilters);
		case NETWORK_MANAGER_OBJECT: 
			return new NetworkObjectInjector(reporter, player, invoker, sendingFilters);
		case NETWORK_SERVER_OBJECT:
			return new NetworkServerInjector(reporter, player, invoker, sendingFilters, serverInjection);
		default:
			throw new IllegalArgumentException("Cannot construct a player injector.");
		}
	}
	
	/**
	 * Retrieve a player by its DataInput connection.
	 * @param inputStream - the associated DataInput connection.
	 * @return The player we found.
	 */
	@Override
	public Player getPlayerByConnection(DataInputStream inputStream) {
		// Wait until the connection owner has been established
		SocketInjector injector = inputStreamLookup.waitSocketInjector(inputStream);
		
		if (injector != null) {
			return injector.getPlayer();
		} else {
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
	 * @param strategy - how to handle previous player injections.
	 */
	@Override
	public void injectPlayer(Player player, ConflictStrategy strategy) {
		// Inject using the player instance itself
		if (isInjectionNecessary(GamePhase.PLAYING)) {
			injectPlayer(player, player, strategy, GamePhase.PLAYING);
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
	PlayerInjector injectPlayer(Player player, Object injectionPoint, ConflictStrategy stategy, GamePhase phase) {
		if (player == null)
			throw new IllegalArgumentException("Player cannot be NULL.");
		if (injectionPoint == null)
			throw new IllegalArgumentException("injectionPoint cannot be NULL.");
		if (phase == null)
			throw new IllegalArgumentException("phase cannot be NULL.");
		
		// Unfortunately, due to NetLoginHandler, multiple threads may potentially call this method.
		synchronized (player) {
			return injectPlayerInternal(player, injectionPoint, stategy, phase);
		}
	}
	
	// Unsafe variant of the above
	private PlayerInjector injectPlayerInternal(Player player, Object injectionPoint, ConflictStrategy stategy, GamePhase phase) {
		PlayerInjector injector = playerInjection.get(player);
		PlayerInjectHooks tempHook = getPlayerHook(phase);
		PlayerInjectHooks permanentHook = tempHook;
		
		// The given player object may be fake, so be careful!
		
		// See if we need to inject something else
		boolean invalidInjector = injector != null ? !injector.canInject(phase) : true;

		// Don't inject if the class has closed
		if (!hasClosed && (tempHook != getInjectorType(injector) || invalidInjector)) {
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
						
						// Get socket and socket injector
						SocketAddress address = injector.getAddress();
						
						// Ignore logged out players
						if (address == null)
							return null;
						
						SocketInjector previous = inputStreamLookup.peekSocketInjector(address);
						Socket socket = injector.getSocket();

						// Close any previously associated hooks before we proceed
						if (previous != null && !(player instanceof Factory)) {
							switch (stategy) {
							case OVERRIDE:
								uninjectPlayer(previous.getPlayer(), true);
								break;
							case BAIL_OUT:
								return null;
							}
						}
						injector.injectManager();
						
						saveAddressLookup(address, socket, injector);
						break;
					}
					
				} catch (PlayerLoggedOutException e) {
					throw e;
					
				} catch (Exception e) {
					// Mark this injection attempt as a failure
					reporter.reportDetailed(this, 
							Report.newBuilder(REPORT_PLAYER_HOOK_FAILED).messageParam(tempHook).callerParam(player, injectionPoint, phase).error(e) 
					);
					hookFailed = true;
				}
				
				// Choose the previous player hook type
				tempHook = PlayerInjectHooks.values()[tempHook.ordinal() - 1];
				
				if (hookFailed)
					reporter.reportWarning(this, Report.newBuilder(REPORT_SWITCHED_PLAYER_HOOK).messageParam(tempHook));
				
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
				lastSuccessfulHook = new WeakReference<PlayerInjector>(injector);
			if (permanentHook != getPlayerHook(phase)) 
				setPlayerHook(phase, tempHook);
			
			// Save injector
			if (injector != null) {
				playerInjection.put(player, injector);
			}
		}
		
		return injector;
	}

	private void saveAddressLookup(SocketAddress address, Socket socket, SocketInjector injector) {
		SocketAddress socketAddress = socket != null ? socket.getRemoteSocketAddress() : null;
		
		if (socketAddress != null && !Objects.equal(socketAddress, address)) {
			// Save this version as well
			inputStreamLookup.setSocketInjector(socketAddress, injector);
		}
		// Save injector
		inputStreamLookup.setSocketInjector(address, injector);
	}
	
	private void cleanupHook(PlayerInjector injector) {
		// Clean up as much as possible
		try {
			if (injector != null)
				injector.cleanupAll();
		} catch (Exception ex) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_HOOK_CLEANUP_FAILED).callerParam(injector).error(ex));
		}
	}
	
	/**
	 * Invoke special routines for handling disconnect before a player is uninjected.
	 * @param player - player to process.
	 */
	@Override
	public void handleDisconnect(Player player) {
		PlayerInjector injector = getInjector(player);
		
		if (injector != null) {
			injector.handleDisconnect();
		}
	}
	
	@Override
	public void updatePlayer(Player player) {
		SocketAddress address = player.getAddress();
		
		// Ignore logged out players
		if (address != null) {
			SocketInjector injector = inputStreamLookup.peekSocketInjector(address);
			
			if (injector != null) {
				injector.setUpdatedPlayer(player);
			} else {
				inputStreamLookup.setSocketInjector(player.getAddress(), 
						new BukkitSocketInjector(player));
			}
		}
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	@Override
	public boolean uninjectPlayer(Player player) {
		return uninjectPlayer(player, false);
	}
	
	/**
	 * Unregisters the given player.
	 * @param player - player to unregister.
	 * @param prepareNextHook - whether or not we need to fix any lingering hooks.
	 * @return TRUE if a player has been uninjected, FALSE otherwise.
	 */
	private boolean uninjectPlayer(Player player, boolean prepareNextHook) {
		if (!hasClosed && player != null) {
			
			PlayerInjector injector = playerInjection.remove(player);

			if (injector != null) {
				injector.cleanupAll();
				
				// Remove the "hooked" network manager in our instance as well
				if (prepareNextHook && injector instanceof NetworkObjectInjector) {
					try {
						PlayerInjector dummyInjector = getHookInstance(player, PlayerInjectHooks.NETWORK_SERVER_OBJECT);
						dummyInjector.initializePlayer(player);
						dummyInjector.setNetworkManager(injector.getNetworkManager(), true);

					} catch (IllegalAccessException e) {
						// Let the user know
						reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_REVERT_HOOK).error(e));
					}
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
	@Override
	public boolean uninjectPlayer(InetSocketAddress address) {
		if (!hasClosed && address != null) {
			SocketInjector injector = inputStreamLookup.peekSocketInjector(address);
			
			// Clean up
			if (injector != null)
				uninjectPlayer(injector.getPlayer(), true);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send the given packet to the given receiver.
	 * @param receiver - the player receiver.
	 * @param packet - the packet to send.
	 * @param filters - whether or not to invoke the packet filters.
	 * @throws InvocationTargetException If an error occured during sending.
	 */
	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		SocketInjector injector = getInjector(receiver);
		
		// Send the packet, or drop it completely
		if (injector != null) {
			injector.sendServerPacket(packet.getHandle(), marker, filters);
		} else {
			throw new PlayerLoggedOutException(String.format(
					"Unable to send packet %s (%s): Player %s has logged out.", 
					packet.getType(), packet, receiver
			));
		}
	}
	
	/**
	 * Recieve a packet as if it were sent by the given player.
	 * @param player - the sender.
	 * @param mcPacket - the packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	@Override
	public void recieveClientPacket(Player player, Object mcPacket) throws IllegalAccessException, InvocationTargetException {
		PlayerInjector injector = getInjector(player);
		
		// Process the given packet, or simply give up
		if (injector != null)
			injector.processPacket(mcPacket);
		else
			throw new PlayerLoggedOutException(String.format(
					"Unable to receieve packet %s. Player %s has logged out.", 
					mcPacket, player
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
			SocketAddress address = player.getAddress();
			
			// Must have logged out - there's nothing we can do
			if (address == null)
				return null;
			
			// Look that up without blocking
			SocketInjector result = inputStreamLookup.peekSocketInjector(address);

			// Ensure that it is non-null and a player injector
			if (result instanceof PlayerInjector)
				return (PlayerInjector) result;
			else
				// Make a dummy injector them
				return createDummyInjector(player);
			
		} else {
			return injector;
		}
	}
	
	/**
	 * Construct a simple dummy injector incase none has been constructed.
	 * @param player - the CraftPlayer to construct for.
	 * @return A dummy injector, or NULL if the given player is not a CraftPlayer.
	 */
	private PlayerInjector createDummyInjector(Player player) {
		if (!MinecraftReflection.getCraftPlayerClass().isAssignableFrom(player.getClass())) {
			// No - this is not safe
			return null;
		}

		try {
			PlayerInjector dummyInjector = getHookInstance(player, PlayerInjectHooks.NETWORK_SERVER_OBJECT);
			dummyInjector.initializePlayer(player);
			
			// This probably means the player has disconnected
			if (dummyInjector.getSocket() == null) {
				return null;
			}
			
			inputStreamLookup.setSocketInjector(dummyInjector.getAddress(), dummyInjector);
			dummyInjectors.put(player, dummyInjector);
			return dummyInjector;
			
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot access fields.", e);
		}
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
	
	@Override
	public boolean canRecievePackets() {
		return false;
	}
	
	@Override
	public PacketEvent handlePacketRecieved(PacketContainer packet, InputStream input, byte[] buffered) {
		throw new UnsupportedOperationException("Proxy injection cannot handle received packets.");
	}
	
	/**
	 * Determine if the given listeners are valid.
	 * @param listeners - listeners to check.
	 */
	@Override
	public void checkListener(Set<PacketListener> listeners) {
		// Make sure the current listeners are compatible
		if (getLastSuccessfulHook() != null) {
			for (PacketListener listener : listeners) {
				checkListener(listener);
			}
		}
	}
	
	/**
	 * Retrieve the last successful hook.
	 * <p>
	 * May be NULL if the hook has been uninjected.
	 * @return Last successful hook.
	 */
	private PlayerInjector getLastSuccessfulHook() {
		return lastSuccessfulHook != null ? lastSuccessfulHook.get() : null;
	}
	
	/**
	 * Determine if a listener is valid or not.
	 * <p>
	 * If not, a warning will be printed to the console. 
	 * @param listener - listener to check.
	 */
	@Override
	public void checkListener(PacketListener listener) {
		PlayerInjector last = getLastSuccessfulHook();
		
		if (last != null) {
			UnsupportedListener result = last.checkListener(version, listener);

			// We won't prevent the listener, as it may still have valid packets
			if (result != null) {
				reporter.reportWarning(this, 
						Report.newBuilder(REPORT_UNSUPPPORTED_LISTENER).messageParam(PacketAdapter.getPluginName(listener), result) 
				);
				
				// These are illegal
				for (int packetID : result.getPackets()) {
					removePacketHandler(PacketType.findLegacy(packetID, Sender.CLIENT));
					removePacketHandler(PacketType.findLegacy(packetID, Sender.SERVER));
				}
			}
		}
	}
	
	/**
	 * Retrieve the current list of registered sending listeners.
	 * @return List of the sending listeners's packet IDs.
	 */
	@Override
	public Set<PacketType> getSendingFilters() {
		return PacketRegistry.toPacketTypes(sendingFilters.toSet(), Sender.SERVER);
	}
	
	@Override
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
		if (inputStreamLookup != null)
			inputStreamLookup.cleanupAll();
		if (serverInjection != null)
			serverInjection.cleanupAll();
		if (netLoginInjector != null)
			netLoginInjector.cleanupAll();
		inputStreamLookup = null;
		serverInjection = null;
		netLoginInjector = null;
		hasClosed = true;
		
		playerInjection.clear();
		invoker = null;
	}
}
