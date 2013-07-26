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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.bukkit.Server;
import org.bukkit.World;
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
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.packet.PacketInjectorBuilder;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.injector.player.PlayerInjectorBuilder;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler.ConflictStrategy;
import com.comphenix.protocol.injector.spigot.SpigotPacketInjector;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

public final class PacketFilterManager implements ProtocolManager, ListenerInvoker, InternalManager {
	
	public static final ReportType REPORT_CANNOT_LOAD_PACKET_LIST = new ReportType("Cannot load server and client packet list.");
	public static final ReportType REPORT_CANNOT_INITIALIZE_PACKET_INJECTOR = new ReportType("Unable to initialize packet injector");
	
	public static final ReportType REPORT_PLUGIN_DEPEND_MISSING = 
			new ReportType("%s doesn't depend on ProtocolLib. Check that its plugin.yml has a 'depend' directive.");
	
	// Registering packet IDs that are not supported
	public static final ReportType REPORT_UNSUPPORTED_SERVER_PACKET_ID = new ReportType("[%s] Unsupported server packet ID in current Minecraft version: %s");
	public static final ReportType REPORT_UNSUPPORTED_CLIENT_PACKET_ID = new ReportType("[%s] Unsupported client packet ID in current Minecraft version: %s");
	
	// Problems injecting and uninjecting players
	public static final ReportType REPORT_CANNOT_UNINJECT_PLAYER = new ReportType("Unable to uninject net handler for player.");
	public static final ReportType REPORT_CANNOT_UNINJECT_OFFLINE_PLAYER = new ReportType("Unable to uninject logged off player.");
	public static final ReportType REPORT_CANNOT_INJECT_PLAYER = new ReportType("Unable to inject player.");

	public static final ReportType REPORT_CANNOT_UNREGISTER_PLUGIN = new ReportType("Unable to handle disabled plugin.");
	public static final ReportType REPORT_PLUGIN_VERIFIER_ERROR = new ReportType("Verifier error: %s");
	
	/**
	 * Sets the inject hook type. Different types allow for maximum compatibility.
	 * @author Kristian
	 */
	public enum PlayerInjectHooks {
		/**
		 * The injection hook that does nothing. Set when every other inject hook fails.
		 */
		NONE,
		
		/**
		 * Override the network handler object itself. Only works in 1.3.
		 * <p>
		 * Cannot intercept MapChunk packets. 
		 */
		NETWORK_MANAGER_OBJECT,
		
		/**
		 * Override the packet queue lists in NetworkHandler. 
		 * <p>
		 * Cannot intercept MapChunk packets. 
		 */
		NETWORK_HANDLER_FIELDS,
		
		/**
		 * Override the server handler object. Versatile, but a tad slower.
		 */
		NETWORK_SERVER_OBJECT;
	}
	
	// The amount of time to wait until we actually unhook every player
	private static final int TICKS_PER_SECOND = 20;
	private static final int UNHOOK_DELAY = 5 * TICKS_PER_SECOND; 
	
	// Delayed unhook
	private DelayedSingleTask unhookTask;
	
	// Create a concurrent set
	private Set<PacketListener> packetListeners = 
			Collections.newSetFromMap(new ConcurrentHashMap<PacketListener, Boolean>());
		
	// Packet injection
	private PacketInjector packetInjector;
	
	// Different injection types per game phase
	private PlayerInjectionHandler playerInjection;

	// Intercepting write packet methods
	private InterceptWritePacket interceptWritePacket;
	
	// Whether or not a packet must be input buffered
	private volatile IntegerSet inputBufferedPackets = new IntegerSet(Packets.MAXIMUM_PACKET_ID + 1);
	
	// The two listener containers
	private SortedPacketListenerList recievedListeners;
	private SortedPacketListenerList sendingListeners;
	
	// Whether or not this class has been closed
	private volatile boolean hasClosed;
	
	// The default class loader
	private ClassLoader classLoader;
		
	// Error repoter
	private ErrorReporter reporter;
	
	// The current server
	private Server server;
	
	// The async packet handler
	private AsyncFilterManager asyncFilterManager;
	
	// Valid server and client packets
	private boolean knowsServerPackets;
	private boolean knowsClientPackets;
	
	// Ensure that we're not performing too may injections
	private AtomicInteger phaseLoginCount = new AtomicInteger(0);
	private AtomicInteger phasePlayingCount = new AtomicInteger(0);
	
	// Whether or not plugins are using the send/receive methods
	private AtomicBoolean packetCreation = new AtomicBoolean();
	
	// Spigot listener, if in use
	private SpigotPacketInjector spigotInjector;
	
	// Plugin verifier
	private PluginVerifier pluginVerifier;
	
	// The current Minecraft version
	private MinecraftVersion minecraftVersion;
	
	/**
	 * Only create instances of this class if protocol lib is disabled.
	 */
	public PacketFilterManager(PacketFilterBuilder builder) {		
		// Used to determine if injection is needed
		Predicate<GamePhase> isInjectionNecessary = new Predicate<GamePhase>() {
			@Override
			public boolean apply(@Nullable GamePhase phase) {
				boolean result = true;
				
				if (phase.hasLogin())
					result &= getPhaseLoginCount() > 0;
				// Note that we will still hook players if the unhooking has been delayed
				if (phase.hasPlaying())
					result &= getPhasePlayingCount() > 0 || unhookTask.isRunning();
				return result;
			}
		};
		
		// Listener containers
		this.recievedListeners = new SortedPacketListenerList();
		this.sendingListeners = new SortedPacketListenerList();
		
		// References
		this.unhookTask = builder.getUnhookTask();
		this.server = builder.getServer();
		this.classLoader = builder.getClassLoader();
		this.reporter = builder.getReporter();
		
		// The plugin verifier
		this.pluginVerifier = new PluginVerifier(builder.getLibrary());
		this.minecraftVersion = builder.getMinecraftVersion();
		
		// The write packet interceptor
		this.interceptWritePacket = new InterceptWritePacket(classLoader, reporter);
		
		// Use the correct injection type
		if (builder.isNettyEnabled()) {
			this.spigotInjector = new SpigotPacketInjector(classLoader, reporter, this, server);
			this.playerInjection = spigotInjector.getPlayerHandler();
			this.packetInjector = spigotInjector.getPacketInjector();
			
			// Set real injector, in case we need it
			spigotInjector.setProxyPacketInjector(PacketInjectorBuilder.newBuilder().
					invoker(this).
					reporter(reporter).
					classLoader(classLoader).
					playerInjection(playerInjection).
					buildInjector()
			);
			
		} else {
			// Initialize standard injection mangers
			this.playerInjection = PlayerInjectorBuilder.newBuilder().
					invoker(this).
					server(server).
					reporter(reporter).
					classLoader(classLoader).
					packetListeners(packetListeners).
					injectionFilter(isInjectionNecessary).
					version(builder.getMinecraftVersion()).
					buildHandler();
			
			this.packetInjector = PacketInjectorBuilder.newBuilder().
					invoker(this).
					reporter(reporter).
					classLoader(classLoader).
					playerInjection(playerInjection).
					buildInjector();
		}
		this.asyncFilterManager = builder.getAsyncManager();
		
		// Attempt to load the list of server and client packets
		try {
			knowsServerPackets = PacketRegistry.getServerPackets() != null;
			knowsClientPackets = PacketRegistry.getClientPackets() != null;
		} catch (FieldAccessException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_LOAD_PACKET_LIST).error(e));
		}
	}

	/**
	 * Construct a new packet filter builder.
	 * @return New builder.
	 */
	public static PacketFilterBuilder newBuilder() {
		return new PacketFilterBuilder();
	}
	
	@Override
	public MinecraftVersion getMinecraftVersion() {
		return minecraftVersion;
	}
	
	@Override
	public AsynchronousManager getAsynchronousManager() {
		return asyncFilterManager;
	}
	
	/**
	 * Retrieves how the server packets are read.
	 * @return Injection method for reading server packets.
	 */
	@Override
	public PlayerInjectHooks getPlayerHook() {
		return playerInjection.getPlayerHook();
	}

	/**
	 * Sets how the server packets are read.
	 * @param playerHook - the new injection method for reading server packets.
	 */
	@Override
	public void setPlayerHook(PlayerInjectHooks playerHook) {
		playerInjection.setPlayerHook(playerHook);
	}

	@Override
	public ImmutableSet<PacketListener> getPacketListeners() {
		return ImmutableSet.copyOf(packetListeners);
	}

	@Override
	public InterceptWritePacket getInterceptWritePacket() {
		return interceptWritePacket;
	}
	
	/**
	 * Warn of common programming mistakes.
	 * @param plugin - plugin to check.
	 */
	private void printPluginWarnings(Plugin plugin) {
		try {
			switch (pluginVerifier.verify(plugin)) {
				case NO_DEPEND:
					reporter.reportWarning(this, Report.newBuilder(REPORT_PLUGIN_DEPEND_MISSING).messageParam(plugin.getName()));
				case VALID:
					// Do nothing
					break;
			}
		} catch (IllegalStateException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_PLUGIN_VERIFIER_ERROR).messageParam(e.getMessage()));
		}
	}
	
	@Override
	public void addPacketListener(PacketListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL.");
		
		// A listener can only be added once
		if (packetListeners.contains(listener))
			return;
		// Check plugin
		printPluginWarnings(listener.getPlugin());
		
		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();
		boolean hasSending = sending != null && sending.isEnabled();
		boolean hasReceiving = receiving != null && receiving.isEnabled();
		
		if (hasSending || hasReceiving) {
			// Add listeners and hooks
			if (hasSending) {
				// This doesn't make any sense
				if (sending.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
					throw new IllegalArgumentException("Sending whitelist cannot require input bufferes to be intercepted.");
				}
				
				verifyWhitelist(listener, sending);
				sendingListeners.addListener(listener, sending);
				enablePacketFilters(listener, ConnectionSide.SERVER_SIDE, sending.getWhitelist());
				
				// Make sure this is possible
				playerInjection.checkListener(listener);
			}
			if (hasSending)
				incrementPhases(sending.getGamePhase());
			
			// Handle receivers after senders
			if (hasReceiving) {
				verifyWhitelist(listener, receiving);
				recievedListeners.addListener(listener, receiving);
				enablePacketFilters(listener, ConnectionSide.CLIENT_SIDE, receiving.getWhitelist());
			}
			if (hasReceiving)
				incrementPhases(receiving.getGamePhase());
			
			// Inform our injected hooks
			packetListeners.add(listener);
			updateRequireInputBuffers();
		}
	}
	
	/**
	 * Invoked when we need to update the input buffer set.
	 */
	private void updateRequireInputBuffers() {
		IntegerSet updated = new IntegerSet(Packets.MAXIMUM_PACKET_ID + 1);
		
		for (PacketListener listener : packetListeners) {
			ListeningWhitelist whitelist = listener.getReceivingWhitelist();
			
			// We only check the recieving whitelist
			if (whitelist.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
				for (int id : whitelist.getWhitelist()) {
					updated.add(id);
				}
			}
		}
		// Update it
		this.inputBufferedPackets = updated;
		this.packetInjector.inputBuffersChanged(updated.toSet());
	}

	/**
	 * Invoked to handle the different game phases of a added listener.
	 * @param phase - listener's game game phase.
	 */
	private void incrementPhases(GamePhase phase) {
		if (phase.hasLogin())
			phaseLoginCount.incrementAndGet();
		
		// We may have to inject into every current player
		if (phase.hasPlaying()) {
			if (phasePlayingCount.incrementAndGet() == 1) {
				// If we're about to uninitialize every player, cancel that instead
				if (unhookTask.isRunning())
					unhookTask.cancel();
				else
					// Inject our hook into already existing players
					initializePlayers(server.getOnlinePlayers());
			}
		}
	}
	
	/**
	 * Invoked to handle the different game phases of a removed listener.
	 * @param phase - listener's game game phase.
	 */
	private void decrementPhases(GamePhase phase) {
		if (phase.hasLogin())
			phaseLoginCount.decrementAndGet();
		
		// We may have to inject into every current player
		if (phase.hasPlaying()) {
			if (phasePlayingCount.decrementAndGet() == 0) {
				// Schedule unhooking in the future
				unhookTask.schedule(UNHOOK_DELAY, new Runnable() {
					@Override
					public void run() {
						// Inject our hook into already existing players
						uninitializePlayers(server.getOnlinePlayers());
					}
				});
			}
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
		
		// Remove listeners and phases
		if (sending != null && sending.isEnabled()) {
			sendingRemoved = sendingListeners.removeListener(listener, sending);
			decrementPhases(sending.getGamePhase());
		}
		if (receiving != null && receiving.isEnabled()) {
			receivingRemoved = recievedListeners.removeListener(listener, receiving);
			decrementPhases(receiving.getGamePhase());
		}
		
		// Remove hooks, if needed
		if (sendingRemoved != null && sendingRemoved.size() > 0)
			disablePacketFilters(ConnectionSide.SERVER_SIDE, sendingRemoved);
		if (receivingRemoved != null && receivingRemoved.size() > 0)
			disablePacketFilters(ConnectionSide.CLIENT_SIDE, receivingRemoved);
		updateRequireInputBuffers();
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
		if (!hasClosed) {
			handlePacket(recievedListeners, event, false);
		}
	}

	@Override
	public void invokePacketSending(PacketEvent event) {
		if (!hasClosed) {
			handlePacket(sendingListeners, event, true);
		}
	}

	@Override
	public boolean requireInputBuffer(int packetId) {
		return inputBufferedPackets.contains(packetId);
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
			packetListeners.invokePacketSending(reporter, event);
		else
			packetListeners.invokePacketRecieving(reporter, event);
		
		// To cancel asynchronous processing, use the async marker
		if (!event.isCancelled() && !hasAsyncCancelled(event.getAsyncMarker())) {
			asyncFilterManager.enqueueSyncPacket(event, event.getAsyncMarker());

			// The above makes a copy of the event, so it's safe to cancel it
			event.setReadOnly(false);
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
				// Note that we may update the packet list here
				if (!knowsServerPackets || PacketRegistry.getServerPackets().contains(packetID))
					playerInjection.addPacketHandler(packetID);
				else
					reporter.reportWarning(this, 
							Report.newBuilder(REPORT_UNSUPPORTED_SERVER_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), packetID)
					);
			}
			
			// As above, only for client packets
			if (side.isForClient() && packetInjector != null) {
				if (!knowsClientPackets || PacketRegistry.getClientPackets().contains(packetID))
					packetInjector.addPacketHandler(packetID);
				else
					reporter.reportWarning(this, 
							Report.newBuilder(REPORT_UNSUPPORTED_CLIENT_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), packetID)
					);
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
		sendServerPacket(reciever, packet, null, true);
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) throws InvocationTargetException {
		sendServerPacket(reciever, packet, null, filters);
	}
	
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, NetworkMarker marker, boolean filters) throws InvocationTargetException {
		if (reciever == null)
			throw new IllegalArgumentException("reciever cannot be NULL.");
		if (packet == null)
			throw new IllegalArgumentException("packet cannot be NULL.");
		// We may have to enable player injection indefinitely after this
		if (packetCreation.compareAndSet(false, true)) 
			incrementPhases(GamePhase.PLAYING);
	
		// Inform the MONITOR packets
		if (!filters) {
			PacketEvent event = PacketEvent.fromServer(this, packet, marker, reciever);
			
			sendingListeners.invokePacketSending(
					reporter, event, ListenerPriority.MONITOR);
			marker = event.getNetworkMarker();
		}
		playerInjection.sendServerPacket(reciever, packet, marker, filters);
	}

	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet) throws IllegalAccessException, InvocationTargetException {
		recieveClientPacket(sender, packet, null, true);
	}
	
	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) throws IllegalAccessException, InvocationTargetException {
		recieveClientPacket(sender, packet, null, filters);
	}
	
	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters) throws IllegalAccessException, InvocationTargetException {
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NULL.");
		if (packet == null)
			throw new IllegalArgumentException("packet cannot be NULL.");
		// And here too
		if (packetCreation.compareAndSet(false, true)) 
			incrementPhases(GamePhase.PLAYING);
		
		Object mcPacket = packet.getHandle();
		
		// Make sure the packet isn't cancelled
		packetInjector.undoCancel(packet.getID(), mcPacket);
		
		if (filters) {
			byte[] data = NetworkMarker.getByteBuffer(marker);
			PacketEvent event  = packetInjector.packetRecieved(packet, sender, data);
			
			if (!event.isCancelled())
				mcPacket = event.getPacket().getHandle();
			else
				return;
			
		} else {
			// Let the monitors know though
			recievedListeners.invokePacketSending(
					reporter, 
					PacketEvent.fromClient(this, packet, marker, sender), 
					ListenerPriority.MONITOR);
		}
		
		playerInjection.recieveClientPacket(sender, mcPacket);
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
	
	@Override
	public Entity getEntityFromID(World container, int id) throws FieldAccessException {
		return EntityUtilities.getEntityFromID(container, id);
	}
	
	@Override
	public List<Player> getEntityTrackers(Entity entity) throws FieldAccessException {
		return EntityUtilities.getEntityTrackers(entity);
	}
	
	/**
	 * Initialize the packet injection for every player.
	 * @param players - list of players to inject. 
	 */
	public void initializePlayers(Player[] players) {
		for (Player player : players)
			playerInjection.injectPlayer(player, ConflictStrategy.OVERRIDE);
	}
	
	/**
	 * Uninitialize the packet injection of every player.
	 * @param players - list of players to uninject. 
	 */
	public void uninitializePlayers(Player[] players) {
		for (Player player : players)
			playerInjection.uninjectPlayer(player);
	}
	
	/**
	 * Register this protocol manager on Bukkit.
	 * @param manager - Bukkit plugin manager that provides player join/leave events.
	 * @param plugin - the parent plugin.
	 */
	@Override
	public void registerEvents(PluginManager manager, final Plugin plugin) {
		if (spigotInjector != null && !spigotInjector.register(plugin))
			throw new IllegalArgumentException("Spigot has already been registered.");
		
		try {
			manager.registerEvents(new Listener() {
				@EventHandler(priority = EventPriority.LOWEST)
			    public void onPrePlayerJoin(PlayerJoinEvent event) {
					PacketFilterManager.this.onPrePlayerJoin(event);
			    }
				@EventHandler(priority = EventPriority.MONITOR)
			    public void onPlayerJoin(PlayerJoinEvent event) {
					PacketFilterManager.this.onPlayerJoin(event);
			    }
				@EventHandler(priority = EventPriority.MONITOR)
			    public void onPlayerQuit(PlayerQuitEvent event) {
					PacketFilterManager.this.onPlayerQuit(event);
			    }
				@EventHandler(priority = EventPriority.MONITOR)
			    public void onPluginDisabled(PluginDisableEvent event) {
					PacketFilterManager.this.onPluginDisabled(event, plugin);
			    }
				
			}, plugin);
		
		} catch (NoSuchMethodError e) {
			// Oh wow! We're running on 1.0.0 or older.
			registerOld(manager, plugin);
		}
	}
	
    private void onPrePlayerJoin(PlayerJoinEvent event) {
		try {
			// Let's clean up the other injection first.
			playerInjection.uninjectPlayer(event.getPlayer().getAddress());
			playerInjection.updatePlayer(event.getPlayer());
		} catch (Exception e) {
			reporter.reportDetailed(PacketFilterManager.this, 
					Report.newBuilder(REPORT_CANNOT_UNINJECT_PLAYER).callerParam(event).error(e)
			);
		}
    }
	
    private void onPlayerJoin(PlayerJoinEvent event) {
		try {
			// This call will be ignored if no listeners are registered
			playerInjection.injectPlayer(event.getPlayer(), ConflictStrategy.OVERRIDE);
		} catch (Exception e) {
			reporter.reportDetailed(PacketFilterManager.this, 
					Report.newBuilder(REPORT_CANNOT_INJECT_PLAYER).callerParam(event).error(e)
			);
		}
    }
    
    private void onPlayerQuit(PlayerQuitEvent event) {
		try {
			Player player = event.getPlayer();

			asyncFilterManager.removePlayer(player);
			playerInjection.handleDisconnect(player);
			playerInjection.uninjectPlayer(player);
		} catch (Exception e) {
			reporter.reportDetailed(PacketFilterManager.this, 
					Report.newBuilder(REPORT_CANNOT_UNINJECT_OFFLINE_PLAYER).callerParam(event).error(e)
			);
		}
    }
    
    private void onPluginDisabled(PluginDisableEvent event, Plugin protocolLibrary) {
		try {
			// Clean up in case the plugin forgets
			if (event.getPlugin() != protocolLibrary) {
				removePacketListeners(event.getPlugin());
			}
		} catch (Exception e) {
			reporter.reportDetailed(PacketFilterManager.this, 
					Report.newBuilder(REPORT_CANNOT_UNREGISTER_PLUGIN).callerParam(event).error(e)
			);
		}
    }
    
	/**
	 * Retrieve the number of listeners that expect packets during playing.
	 * @return Number of listeners.
	 */
	private int getPhasePlayingCount() {
		return phasePlayingCount.get();
	}
	
	/**
	 * Retrieve the number of listeners that expect packets during login.
	 * @return Number of listeners
	 */
	private int getPhaseLoginCount() {
		return phaseLoginCount.get();
	}
	
	@Override
	public int getPacketID(Object packet) {
		if (packet == null)
			throw new IllegalArgumentException("Packet cannot be NULL.");
		if (!MinecraftReflection.isPacketClass(packet))
			throw new IllegalArgumentException("The given object " + packet + " is not a packet.");
		
		Integer id = PacketRegistry.getPacketToID().get(packet.getClass());
		
		if (id != null) {
			return id;
		} else {
			throw new IllegalArgumentException(
					"Unable to find associated packet of " + packet + ": Lookup returned NULL.");
		}
	}
	
	@Override
	public void registerPacketClass(Class<?> clazz, int packetID) {
		PacketRegistry.getPacketToID().put(clazz, packetID);
	}
	
	@Override
	public void unregisterPacketClass(Class<?> clazz) {
		PacketRegistry.getPacketToID().remove(clazz);
	}

	@Override
	public Class<?> getPacketClassFromID(int packetID, boolean forceVanilla) {
		return PacketRegistry.getPacketClassFromID(packetID, forceVanilla);
	}
	
	// Yes, this is crazy.
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void registerOld(PluginManager manager, final Plugin plugin) {
		
		try {
			ClassLoader loader = manager.getClass().getClassLoader();
			
			// The different enums we are going to need
			Class eventTypes = loader.loadClass("org.bukkit.event.Event$Type");
			Class eventPriority = loader.loadClass("org.bukkit.event.Event$Priority");
			
			// Get the priority
			Object priorityLowest = Enum.valueOf(eventPriority, "Lowest");
			Object priorityMonitor = Enum.valueOf(eventPriority, "Monitor");
			
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

			Enhancer playerLow = new Enhancer();
			Enhancer playerEx = new Enhancer();
			Enhancer serverEx = new Enhancer();
			
			playerLow.setSuperclass(playerListener);
			playerLow.setClassLoader(classLoader);
			playerLow.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					// Must have a parameter
					if (args.length == 1) {
						Object event = args[0];
						
						if (event instanceof PlayerJoinEvent) {
							onPrePlayerJoin((PlayerJoinEvent) event);
						}
					}
					return null;
				}
			});
			
			playerEx.setSuperclass(playerListener);
			playerEx.setClassLoader(classLoader);
			playerEx.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
					if (args.length == 1) {
						Object event = args[0];
						
						// Check for the correct event
						if (event instanceof PlayerJoinEvent) {
							onPlayerJoin((PlayerJoinEvent) event);
						} else if (event instanceof PlayerQuitEvent) {
							onPlayerQuit((PlayerQuitEvent) event);
						}
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
							onPluginDisabled((PluginDisableEvent) event, plugin);
					}
					return null;
				}
			});
			
			// Create our listener
			Object playerProxyLow = playerLow.create();
			Object playerProxy = playerEx.create();
			Object serverProxy = serverEx.create();
			
			registerEvent.invoke(manager, playerJoinType, playerProxyLow, priorityLowest, plugin);
			registerEvent.invoke(manager, playerJoinType, playerProxy, priorityMonitor, plugin);
			registerEvent.invoke(manager, playerQuitType, playerProxy, priorityMonitor, plugin);
			registerEvent.invoke(manager, pluginDisabledType, serverProxy, priorityMonitor, plugin);
			
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
		return PacketRegistry.getServerPackets();
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	public static Set<Integer> getClientPackets() throws FieldAccessException {
		return PacketRegistry.getClientPackets();
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
	
	@Override
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
		recievedListeners = null;
		sendingListeners = null;
		
		// Also cleanup the interceptor for the write packet method
		interceptWritePacket.cleanup();
		
		// Clean up async handlers. We have to do this last.
		asyncFilterManager.cleanupAll();
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
