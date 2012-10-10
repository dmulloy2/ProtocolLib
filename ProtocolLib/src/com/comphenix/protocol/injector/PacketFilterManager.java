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

package com.comphenix.protocol.injector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.Packet;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public final class PacketFilterManager implements ProtocolManager, ListenerInvoker {

	/**
	 * Sets the inject hook type. Different types allow for maximum compatibility.
	 * @author Kristian
	 */
	public enum PlayerInjectHooks {
		/**
		 * Override the packet queue lists in NetworkHandler. 
		 * <p>
		 * Cannot intercept MapChunk packets. 
		 */
		NETWORK_HANDLER_FIELDS,
		
		/**
		 * Override the network handler object itself. Only works in 1.3.
		 * <p>
		 * Cannot intercept MapChunk packets. 
		 */
		NETWORK_MANAGER_OBJECT,
		
		/**
		 * Override the server handler object. Versatile, but a tad slower.
		 */
		NETWORK_SERVER_OBJECT;
	}
	
	// Create a concurrent set
	private Set<PacketListener> packetListeners = 
			Collections.newSetFromMap(new ConcurrentHashMap<PacketListener, Boolean>());
		
	// Packet injection
	private PacketInjector packetInjector;
	
	// Player injection
	private PlayerInjectionHandler playerInjection;
	
	// The two listener containers
	private SortedPacketListenerList recievedListeners = new SortedPacketListenerList();
	private SortedPacketListenerList sendingListeners = new SortedPacketListenerList();
	
	// Whether or not this class has been closed
	private volatile boolean hasClosed;
	
	// The default class loader
	private ClassLoader classLoader;
		
	// Error logger
	private Logger logger;
	
	// The async packet handler
	private AsyncFilterManager asyncFilterManager;
	
	// Valid server and client packets
	private Set<Integer> serverPackets;
	private Set<Integer> clientPackets;
	
	
	/**
	 * Only create instances of this class if protocol lib is disabled.
	 */
	public PacketFilterManager(ClassLoader classLoader, Server server, Logger logger) {
		if (logger == null)
			throw new IllegalArgumentException("logger cannot be NULL.");
		if (classLoader == null)
			throw new IllegalArgumentException("classLoader cannot be NULL.");
		
		try {
			// Initialize values
			this.classLoader = classLoader;
			this.logger = logger;
			this.playerInjection = new PlayerInjectionHandler(classLoader, logger, this, server);
			this.packetInjector = new PacketInjector(classLoader, this, playerInjection);
			this.asyncFilterManager = new AsyncFilterManager(logger, server.getScheduler(), this);
			
			// Attempt to load the list of server and client packets
			try {
				this.serverPackets = MinecraftRegistry.getServerPackets();
				this.clientPackets = MinecraftRegistry.getClientPackets();
			} catch (FieldAccessException e) {
				logger.log(Level.WARNING, "Cannot load server and client packet list.", e);
			}

		} catch (IllegalAccessException e) {
			logger.log(Level.SEVERE, "Unable to initialize packet injector.", e);
		}
	}
	
	@Override
	public AsynchronousManager getAsynchronousManager() {
		return asyncFilterManager;
	}
	
	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	public PlayerInjectHooks getPlayerHook() {
		return playerInjection.getPlayerHook();
	}

	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		playerInjection.setPlayerHook(playerHook);
		
		// Make sure the current listeners are compatible
		playerInjection.checkListener(packetListeners);
	}

	public Logger getLogger() {
		return logger;
	}
	
	@Override
	public ImmutableSet<PacketListener> getPacketListeners() {
		return ImmutableSet.copyOf(packetListeners);
	}

	@Override
	public void addPacketListener(PacketListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL.");
		
		// A listener can only be added once
		if (packetListeners.contains(listener))
			return;
		
		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();
		boolean hasSending = sending != null && sending.isEnabled();
		boolean hasReceiving = receiving != null && receiving.isEnabled();
		
		if (hasSending || hasReceiving) {
			// Add listeners and hooks
			if (hasSending) {
				verifyWhitelist(listener, sending);
				sendingListeners.addListener(listener, sending);
				enablePacketFilters(listener, ConnectionSide.SERVER_SIDE, sending.getWhitelist());
				
				// Make sure this is possible
				playerInjection.checkListener(listener);
			}
			if (hasReceiving) {
				verifyWhitelist(listener, receiving);
				recievedListeners.addListener(listener, receiving);
				enablePacketFilters(listener, ConnectionSide.CLIENT_SIDE, receiving.getWhitelist());
			}
			
			// Inform our injected hooks
			packetListeners.add(listener);
		}
	}
	
	/**
	 * Determine if the packet IDs in a whitelist is valid.
	 * @param listener - the listener that will be mentioned in the error.
	 * @param whitelist - whitelist of packet IDs.
	 * @throws IllegalArgumentException If the whitelist is illegal.
	 */
	public static void verifyWhitelist(PacketListener listener, ListeningWhitelist whitelist) {
		for (Integer id : whitelist.getWhitelist()) {
			if (id >= 256 || id < 0) {
				throw new IllegalArgumentException(String.format("Invalid packet id %s in listener %s.", 
							id, PacketAdapter.getPluginName(listener))
				);
			}
		}
	}
		
	@Override
	public void removePacketListener(PacketListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL");

		List<Integer> sendingRemoved = null;
		List<Integer> receivingRemoved = null;
		
		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();
		
		// Remove from the overal list of listeners
		if (!packetListeners.remove(listener))
			return;
		
		// Add listeners
		if (sending != null && sending.isEnabled())
			sendingRemoved = sendingListeners.removeListener(listener, sending);
		if (receiving != null && receiving.isEnabled())
			receivingRemoved = recievedListeners.removeListener(listener, receiving);
		
		// Remove hooks, if needed
		if (sendingRemoved != null && sendingRemoved.size() > 0)
			disablePacketFilters(ConnectionSide.SERVER_SIDE, sendingRemoved);
		if (receivingRemoved != null && receivingRemoved.size() > 0)
			disablePacketFilters(ConnectionSide.CLIENT_SIDE, receivingRemoved);
	}
	
	@Override
	public void removePacketListeners(Plugin plugin) {
		
		// Iterate through every packet listener
		for (PacketListener listener : packetListeners) {			
			// Remove the listener
			if (Objects.equal(listener.getPlugin(), plugin)) {
				removePacketListener(listener);
			}
		}
		
		// Do the same for the asynchronous events
		asyncFilterManager.unregisterAsyncHandlers(plugin);
	}
	
	@Override
	public void invokePacketRecieving(PacketEvent event) {
		handlePacket(recievedListeners, event, false);
	}

	@Override
	public void invokePacketSending(PacketEvent event) {
		handlePacket(sendingListeners, event, true);
	}
	
	/**
	 * Handle a packet sending or receiving event.
	 * <p>
	 * Note that we also handle asynchronous events.
	 * @param packetListeners - packet listeners that will receive this event.
	 * @param event - the evnet to broadcast.
	 */
	private void handlePacket(SortedPacketListenerList packetListeners, PacketEvent event, boolean sending) {
		
		// By default, asynchronous packets are queued for processing
		if (asyncFilterManager.hasAsynchronousListeners(event)) {
			event.setAsyncMarker(asyncFilterManager.createAsyncMarker());
		}
		
		// Process synchronous events
		if (sending)
			packetListeners.invokePacketSending(logger, event);
		else
			packetListeners.invokePacketRecieving(logger, event);
		
		// To cancel asynchronous processing, use the async marker
		if (!event.isCancelled() && !hasAsyncCancelled(event.getAsyncMarker())) {
			asyncFilterManager.enqueueSyncPacket(event, event.getAsyncMarker());

			// The above makes a copy of the event, so it's safe to cancel it
			event.setCancelled(true);
		}
	}
	
	// NULL marker mean we're dealing with no asynchronous listeners 
	private boolean hasAsyncCancelled(AsyncMarker marker) {
		return marker == null || marker.isAsyncCancelled();
	}
	
	/**
	 * Enables packet events for a given packet ID.
	 * <p>
	 * Note that all packets are disabled by default.
	 * 
	 * @param listener - the listener that requested to enable these filters.
	 * @param side - which side the event will arrive from.
	 * @param packets - the packet id(s).
	 */
	private void enablePacketFilters(PacketListener listener, ConnectionSide side, Iterable<Integer> packets) {
		if (side == null)
			throw new IllegalArgumentException("side cannot be NULL.");

		// Note the difference between unsupported and valid.
		// Every packet ID between and including 0 - 255 is valid, but only a subset is supported.
		
		for (int packetID : packets) {
			// Only register server packets that are actually supported by Minecraft
			if (side.isForServer()) {
				if (serverPackets != null && serverPackets.contains(packetID))
					playerInjection.addPacketHandler(packetID);
				else
					logger.warning(String.format(
							"[%s] Unsupported server packet ID in current Minecraft version: %s",
							PacketAdapter.getPluginName(listener), packetID
					));
			}
			
			// As above, only for client packets
			if (side.isForClient() && packetInjector != null) {
				if (clientPackets != null && clientPackets.contains(packetID))
					packetInjector.addPacketHandler(packetID);
				else
					logger.warning(String.format(
							"[%s] Unsupported client packet ID in current Minecraft version: %s",
							PacketAdapter.getPluginName(listener), packetID
					));
			}
		}
	}

	/**
	 * Disables packet events from a given packet ID.
	 * @param packets - the packet id(s).
	 * @param side - which side the event no longer should arrive from.
	 */
	private void disablePacketFilters(ConnectionSide side, Iterable<Integer> packets) {
		if (side == null)
			throw new IllegalArgumentException("side cannot be NULL.");
		
		for (int packetID : packets) {
			if (side.isForServer())
				playerInjection.removePacketHandler(packetID);
			if (side.isForClient() && packetInjector != null) 
				packetInjector.removePacketHandler(packetID);
		}
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet) throws InvocationTargetException {
		sendServerPacket(reciever, packet, true);
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
		if (reciever == null)
			throw new IllegalArgumentException("reciever cannot be NULL.");
		if (packet == null)
			throw new IllegalArgumentException("packet cannot be NULL.");
		
		playerInjection.sendServerPacket(reciever, packet, filters);
	}

	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
		recieveClientPacket(sender, packet, true);
	}
	
	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
		
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NULL.");
		if (packet == null)
			throw new IllegalArgumentException("packet cannot be NULL.");
		
		Packet mcPacket = packet.getHandle();
		
		// Make sure the packet isn't cancelled
		packetInjector.undoCancel(packet.getID(), mcPacket);
		
		if (filters) {
			PacketEvent event  = packetInjector.packetRecieved(packet, sender);
			
			if (!event.isCancelled())
				mcPacket = event.getPacket().getHandle();
			else
				return;
		}
		
		playerInjection.processPacket(sender, mcPacket);
	}
	
	@Override
	public PacketContainer createPacket(int id) {
		return createPacket(id, true);
	}
	
	@Override
	public PacketContainer createPacket(int id, boolean forceDefaults) {
		PacketContainer packet = new PacketContainer(id);
		
		// Use any default values if possible
		if (forceDefaults) {
			try {
				packet.getModifier().writeDefaults();
			} catch (FieldAccessException e) {
				throw new RuntimeException("Security exception.", e);
			}
		}
		
		return packet;
	}
	
	@Override
	public PacketConstructor createPacketConstructor(int id, Object... arguments) {
		return PacketConstructor.DEFAULT.withPacket(id, arguments);
	}

	@Override
	public Set<Integer> getSendingFilters() {
		return playerInjection.getSendingFilters();
	}
	
	@Override
	public Set<Integer> getReceivingFilters() {
		return ImmutableSet.copyOf(packetInjector.getPacketHandlers());
	}
	
	@Override
	public void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException {
		EntityUtilities.updateEntity(entity, observers);
	}
	
	/**
	 * Initialize the packet injection for every player.
	 * @param players - list of players to inject. 
	 */
	public void initializePlayers(Player[] players) {
		for (Player player : players)
			playerInjection.injectPlayer(player);
	}
		
	/**
	 * Register this protocol manager on Bukkit.
	 * @param manager - Bukkit plugin manager that provides player join/leave events.
	 * @param plugin - the parent plugin.
	 */
	public void registerEvents(PluginManager manager, final Plugin plugin) {
		
		try {
			manager.registerEvents(new Listener() {
				
				@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			    public void onPlayerJoin(PlayerJoinEvent event) {
					playerInjection.injectPlayer(event.getPlayer());
			    }
				
				@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			    public void onPlayerQuit(PlayerQuitEvent event) {
					playerInjection.uninjectPlayer(event.getPlayer());
			    }
				
				@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
			    public void onPluginDisabled(PluginDisableEvent event) {
					// Clean up in case the plugin forgets
					if (event.getPlugin() != plugin) {
						removePacketListeners(event.getPlugin());
					}
			    }
				
			}, plugin);
		
		} catch (NoSuchMethodError e) {
			// Oh wow! We're running on 1.0.0 or older.
			registerOld(manager, plugin);
		}
	}
	
	@Override
	public int getPacketID(Packet packet) {
		if (packet == null)
			throw new IllegalArgumentException("Packet cannot be NULL.");
		
		return MinecraftRegistry.getPacketToID().get(packet.getClass());
	}
	
	// Yes, this is crazy.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerOld(PluginManager manager, Plugin plugin) {
		
		try {
			ClassLoader loader = manager.getClass().getClassLoader();
			
			// The different enums we are going to need
			Class eventTypes = loader.loadClass("org.bukkit.event.Event$Type");
			Class eventPriority = loader.loadClass("org.bukkit.event.Event$Priority");
			
			// Get the priority
			Object priorityNormal = Enum.valueOf(eventPriority, "Highest");
			
			// Get event types
			Object playerJoinType = Enum.valueOf(eventTypes, "PLAYER_JOIN");
			Object playerQuitType = Enum.valueOf(eventTypes, "PLAYER_QUIT");
			Object pluginDisabledType = Enum.valueOf(eventTypes, "PLUGIN_DISABLE");
			
			// The player listener! Good times.
			Class<?> playerListener = loader.loadClass("org.bukkit.event.player.PlayerListener");
			Class<?> serverListener = loader.loadClass("org.bukkit.event.server.ServerListener");
			
			// Find the register event method
			Method registerEvent = FuzzyReflection.fromObject(manager).getMethodByParameters("registerEvent", 
					eventTypes, Listener.class, eventPriority, Plugin.class);

			Enhancer playerEx = new Enhancer();
			Enhancer serverEx = new Enhancer();
			
			playerEx.setSuperclass(playerListener);
			playerEx.setClassLoader(classLoader);
			playerEx.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					// Must have a parameter
					if (args.length == 1) {
						Object event = args[0];
						
						// Check for the correct event
						if (event instanceof PlayerJoinEvent)
							playerInjection.injectPlayer(((PlayerJoinEvent) event).getPlayer());
						else if (event instanceof PlayerQuitEvent)
							playerInjection.uninjectPlayer(((PlayerQuitEvent) event).getPlayer());
					}
					return null;
				}
			});
			
			serverEx.setSuperclass(serverListener);
			serverEx.setClassLoader(classLoader);
			serverEx.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args,
						MethodProxy proxy) throws Throwable {
					// Must have a parameter
					if (args.length == 1) {
						Object event = args[0];
						
						if (event instanceof PluginDisableEvent)
							removePacketListeners(((PluginDisableEvent) event).getPlugin());
					}
					return null;
				}
			});
			
			// Create our listener
			Object playerProxy = playerEx.create();
			Object serverProxy = serverEx.create();
			
			registerEvent.invoke(manager, playerJoinType, playerProxy, priorityNormal, plugin);
			registerEvent.invoke(manager, playerQuitType, playerProxy, priorityNormal, plugin);
			registerEvent.invoke(manager, pluginDisabledType, serverProxy, priorityNormal, plugin);
			
			// A lot can go wrong
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Retrieve every known and supported server packet.
	 * @return An immutable set of every known server packet.
	 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
	 */
	public static Set<Integer> getServerPackets() throws FieldAccessException {
		return MinecraftRegistry.getServerPackets();
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	public static Set<Integer> getClientPackets() throws FieldAccessException {
		return MinecraftRegistry.getClientPackets();
	}
	
	/**
	 * Retrieves the current plugin class loader.
	 * @return Class loader.
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	@Override
	public boolean isClosed() {
		return hasClosed;
	}
	
	/**
	 * Called when ProtocolLib is closing.
	 */
	public void close() {
		// Guard
		if (hasClosed)
			return;

		// Remove packet handlers
		if (packetInjector != null)
			packetInjector.cleanupAll();
		
		// Remove server handler
		playerInjection.close();
		hasClosed = true;
		
		// Remove listeners
		packetListeners.clear();
		
		// Clean up async handlers. We have to do this last.
		asyncFilterManager.cleanupAll();
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
