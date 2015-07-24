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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.compat.netty.Netty;
import com.comphenix.protocol.compat.netty.ProtocolInjector;
import com.comphenix.protocol.compat.netty.WrappedChannel;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;
import com.comphenix.protocol.injector.packet.PacketInjector;
import com.comphenix.protocol.injector.packet.PacketInjectorBuilder;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler;
import com.comphenix.protocol.injector.player.PlayerInjectionHandler.ConflictStrategy;
import com.comphenix.protocol.injector.player.PlayerInjector.ServerHandlerNull;
import com.comphenix.protocol.injector.player.PlayerInjectorBuilder;
import com.comphenix.protocol.injector.spigot.SpigotPacketInjector;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.Util;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

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
	 * The number of ticks in a second.
	 */
	public static final int TICKS_PER_SECOND = 20;

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
	private volatile Set<PacketType> inputBufferedPackets = Sets.newHashSet();

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

	// The current ProtocolLib library
	private Plugin library;

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

	// Netty injector (for 1.7.2)
	private ProtocolInjector nettyInjector;

	// Plugin verifier
	private PluginVerifier pluginVerifier;

	// Whether or not Location.distance(Location) exists - we assume this is the case
	private boolean hasRecycleDistance = true;

	// The current Minecraft version
	private MinecraftVersion minecraftVersion;

	// Login packets
	private LoginPackets loginPackets;

	// Debug mode
	private boolean debug;

	/**
	 * Only create instances of this class if ProtocolLib is disabled.
	 * @param builder - PacketFilterBuilder
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

		// The plugin verifier - we don't want to stop ProtocolLib just because its failing
		try {
			this.pluginVerifier = new PluginVerifier(builder.getLibrary());
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_PLUGIN_VERIFIER_ERROR).
					messageParam(e.getMessage()).error(e));
		}

		// Prepare version
		this.minecraftVersion = builder.getMinecraftVersion();
		this.loginPackets = new LoginPackets(minecraftVersion);

		// The write packet interceptor
		this.interceptWritePacket = new InterceptWritePacket(reporter);

		// Use the correct injection type
		if (MinecraftReflection.isUsingNetty()) {
			this.nettyInjector = Netty.getProtocolInjector(builder.getLibrary(), this, reporter);
			this.playerInjection = nettyInjector.getPlayerInjector();
			this.packetInjector = nettyInjector.getPacketInjector();

		} else if (builder.isNettyEnabled()) {
			this.spigotInjector = new SpigotPacketInjector(reporter, this, server);
			this.playerInjection = spigotInjector.getPlayerHandler();
			this.packetInjector = spigotInjector.getPacketInjector();

			// Set real injector, in case we need it
			spigotInjector.setProxyPacketInjector(PacketInjectorBuilder.newBuilder().
					invoker(this).
					reporter(reporter).
					playerInjection(playerInjection).
					buildInjector()
			);

		} else {
			// Initialize standard injection mangers
			this.playerInjection = PlayerInjectorBuilder.newBuilder().
					invoker(this).
					server(server).
					reporter(reporter).
					packetListeners(packetListeners).
					injectionFilter(isInjectionNecessary).
					version(builder.getMinecraftVersion()).
					buildHandler();

			this.packetInjector = PacketInjectorBuilder.newBuilder().
					invoker(this).
					reporter(reporter).
					playerInjection(playerInjection).
					buildInjector();
		}
		this.asyncFilterManager = builder.getAsyncManager();
		this.library = builder.getLibrary();

		// Attempt to load the list of server and client packets
		try {
			knowsServerPackets = PacketRegistry.getClientPacketTypes() != null;
			knowsClientPackets = PacketRegistry.getServerPacketTypes() != null;
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
	public int getProtocolVersion(Player player) {
		return playerInjection.getProtocolVersion(player);
	}

	@Override
	public MinecraftVersion getMinecraftVersion() {
		return minecraftVersion;
	}

	@Override
	public AsynchronousManager getAsynchronousManager() {
		return asyncFilterManager;
	}

	@Override
	public boolean isDebug() {
		return debug;
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;

		// Inform components that can handle debug mode
		if (nettyInjector != null) {
			nettyInjector.setDebug(debug);
		}
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
		if (pluginVerifier == null)
			return;

		try {
			switch (pluginVerifier.verify(plugin)) {
				case NO_DEPEND:
					reporter.reportWarning(this, Report.newBuilder(REPORT_PLUGIN_DEPEND_MISSING).messageParam(plugin.getName()));
				case VALID:
					// Do nothing
					break;
			}
		} catch (Exception e) {
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
		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();
		boolean hasSending = sending != null && sending.isEnabled();
		boolean hasReceiving = receiving != null && receiving.isEnabled();

		// Check plugin
		if (!(hasSending && sending.getOptions().contains(ListenerOptions.SKIP_PLUGIN_VERIFIER)) &&
			!(hasReceiving && receiving.getOptions().contains(ListenerOptions.SKIP_PLUGIN_VERIFIER))) {

			printPluginWarnings(listener.getPlugin());
		}

		if (hasSending || hasReceiving) {
			// Add listeners and hooks
			if (hasSending) {
				// This doesn't make any sense
				if (sending.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
					throw new IllegalArgumentException("Sending whitelist cannot require input bufferes to be intercepted.");
				}

				verifyWhitelist(listener, sending);
				sendingListeners.addListener(listener, sending);
				enablePacketFilters(listener, sending.getTypes());

				// Make sure this is possible
				playerInjection.checkListener(listener);
			}
			if (hasSending)
				incrementPhases(processPhase(sending));

			// Handle receivers after senders
			if (hasReceiving) {
				verifyWhitelist(listener, receiving);
				recievedListeners.addListener(listener, receiving);
				enablePacketFilters(listener, receiving.getTypes());
			}
			if (hasReceiving)
				incrementPhases(processPhase(receiving));

			// Inform our injected hooks
			packetListeners.add(listener);
			updateRequireInputBuffers();
		}
	}

	private GamePhase processPhase(ListeningWhitelist whitelist) {
		// Determine if this is a login packet, ensuring that gamephase detection is enabled
		if (!whitelist.getGamePhase().hasLogin() &&
			!whitelist.getOptions().contains(ListenerOptions.DISABLE_GAMEPHASE_DETECTION)) {

			for (PacketType type : whitelist.getTypes()) {
				if (loginPackets.isLoginPacket(type)) {
					return GamePhase.BOTH;
				}
			}
		}
		return whitelist.getGamePhase();
	}

	/**
	 * Invoked when we need to update the input buffer set.
	 */
	private void updateRequireInputBuffers() {
		Set<PacketType> updated = Sets.newHashSet();

		for (PacketListener listener : packetListeners) {
			ListeningWhitelist whitelist = listener.getReceivingWhitelist();

			// We only check the recieving whitelist
			if (whitelist.getOptions().contains(ListenerOptions.INTERCEPT_INPUT_BUFFER)) {
				for (PacketType type : whitelist.getTypes()) {
					updated.add(type);
				}
			}
		}
		// Update it
		this.inputBufferedPackets = updated;
		this.packetInjector.inputBuffersChanged(updated);
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
					initializePlayers(Util.getOnlinePlayers());
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
						uninitializePlayers(Util.getOnlinePlayers());
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
		for (PacketType type : whitelist.getTypes()) {
			if (type == null) {
				throw new IllegalArgumentException(String.format("Packet type in in listener %s was NULL.",
						PacketAdapter.getPluginName(listener))
				);
			}
		}
	}

	@Override
	public void removePacketListener(PacketListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener cannot be NULL");

		List<PacketType> sendingRemoved = null;
		List<PacketType> receivingRemoved = null;

		ListeningWhitelist sending = listener.getSendingWhitelist();
		ListeningWhitelist receiving = listener.getReceivingWhitelist();

		// Remove from the overal list of listeners
		if (!packetListeners.remove(listener))
			return;

		// Remove listeners and phases
		if (sending != null && sending.isEnabled()) {
			sendingRemoved = sendingListeners.removeListener(listener, sending);
			decrementPhases(processPhase(sending));
		}
		if (receiving != null && receiving.isEnabled()) {
			receivingRemoved = recievedListeners.removeListener(listener, receiving);
			decrementPhases(processPhase(receiving));
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
		return inputBufferedPackets.contains(PacketType.findLegacy(packetId, Sender.CLIENT));
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
	private void enablePacketFilters(PacketListener listener, Iterable<PacketType> packets) {
		// Note the difference between unsupported and valid.
		// Every packet ID between and including 0 - 255 is valid, but only a subset is supported.

		for (PacketType type : packets) {
			// Only register server packets that are actually supported by Minecraft
			if (type.getSender() == Sender.SERVER) {
				// Note that we may update the packet list here
				if (!knowsServerPackets || PacketRegistry.getServerPacketTypes().contains(type))
					playerInjection.addPacketHandler(type, listener.getSendingWhitelist().getOptions());
				else
					reporter.reportWarning(this,
							Report.newBuilder(REPORT_UNSUPPORTED_SERVER_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), type)
					);
			}

			// As above, only for client packets
			if (type.getSender() == Sender.CLIENT && packetInjector != null) {
				if (!knowsClientPackets || PacketRegistry.getClientPacketTypes().contains(type))
					packetInjector.addPacketHandler(type, listener.getReceivingWhitelist().getOptions());
				else
					reporter.reportWarning(this,
							Report.newBuilder(REPORT_UNSUPPORTED_CLIENT_PACKET_ID).messageParam(PacketAdapter.getPluginName(listener), type)
					);
			}
		}
	}

	/**
	 * Disables packet events from a given packet ID.
	 * @param packets - the packet id(s).
	 * @param side - which side the event no longer should arrive from.
	 */
	private void disablePacketFilters(ConnectionSide side, Iterable<PacketType> packets) {
		if (side == null)
			throw new IllegalArgumentException("side cannot be NULL.");

		for (PacketType type : packets) {
			if (side.isForServer())
				playerInjection.removePacketHandler(type);
			if (side.isForClient() && packetInjector != null)
				packetInjector.removePacketHandler(type);
		}
	}

	@Override
	public void broadcastServerPacket(PacketContainer packet) {
		Preconditions.checkNotNull(packet, "packet cannot be NULL.");
		broadcastServerPacket(packet, Util.getOnlinePlayers());
	}

	@Override
	public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker) {
		Preconditions.checkNotNull(packet, "packet cannot be NULL.");
 		Preconditions.checkNotNull(entity, "entity cannot be NULL.");
 		List<Player> trackers = getEntityTrackers(entity);

 		// Only add it if it's a player
 		if (includeTracker && entity instanceof Player) {
 			trackers.add((Player) entity);
 		}
		broadcastServerPacket(packet, trackers);
	}

	@Override
	public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance) {
		try {
			// Square the maximum too
			int maxDistance = maxObserverDistance * maxObserverDistance;

			World world = origin.getWorld();
			Location recycle = origin.clone();

			// Only broadcast the packet to nearby players
			for (Player player : Util.getOnlinePlayers()) {
				if (world.equals(player.getWorld()) &&
				    getDistanceSquared(origin, recycle, player) <= maxDistance) {

					sendServerPacket(player, packet);
				}
			}

		} catch (InvocationTargetException e) {
			throw new FieldAccessException("Unable to send server packet.", e);
		}
	}

	/**
	 * Retrieve the squared distance between a location and a player.
	 * @param origin - the origin location.
	 * @param recycle - a location object to be recycled, if supported.
	 * @param player - the player.
	 * @return The squared distance between the player and the origin,
	 */
	private double getDistanceSquared(Location origin, Location recycle, Player player) {
		if (hasRecycleDistance) {
			try {
				return player.getLocation(recycle).distanceSquared(origin);
			} catch (Error e) {
				// Damn it
				hasRecycleDistance = false;
			}
		}

		// The fallback method
		return player.getLocation().distanceSquared(origin);
	}

	/**
	 * Broadcast a packet to a given iterable of players.
	 * @param packet - the packet to broadcast.
	 * @param players - the iterable of players.
	 */
	private void broadcastServerPacket(PacketContainer packet, Iterable<Player> players) {
		try {
			for (Player player : players) {
				sendServerPacket(player, packet);
			}
		} catch (InvocationTargetException e) {
			throw new FieldAccessException("Unable to send server packet.", e);
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
	public void sendServerPacket(final Player receiver, final PacketContainer packet, NetworkMarker marker, final boolean filters) throws InvocationTargetException {
		if (receiver == null)
			throw new IllegalArgumentException("receiver cannot be NULL.");
		if (packet == null)
			throw new IllegalArgumentException("packet cannot be NULL.");
		if (packet.getType().getSender() == Sender.CLIENT)
			throw new IllegalArgumentException("Packet of sender CLIENT cannot be sent to a client.");

		// We may have to enable player injection indefinitely after this
		if (packetCreation.compareAndSet(false, true))
			incrementPhases(GamePhase.PLAYING);

		if (!filters) {
			// We may have to delay the packet due to non-asynchronous monitor listeners
			if (!filters && !Bukkit.isPrimaryThread() && playerInjection.hasMainThreadListener(packet.getType())) {
				final NetworkMarker copy = marker;

				server.getScheduler().scheduleSyncDelayedTask(library, new Runnable() {
					@Override
					public void run() {
						try {
							// Prevent infinite loops
							if (!Bukkit.isPrimaryThread())
								throw new IllegalStateException("Scheduled task was not executed on the main thread!");
							sendServerPacket(receiver, packet, copy, filters);
						} catch (Exception e) {
							reporter.reportMinimal(library, "sendServerPacket-run()", e);
						}
					}
				});
				return;
			}

			PacketEvent event = PacketEvent.fromServer(this, packet, marker, receiver, false);
			sendingListeners.invokePacketSending(reporter, event, ListenerPriority.MONITOR);
			marker = NetworkMarker.getNetworkMarker(event);
		}
		playerInjection.sendServerPacket(receiver, packet, marker, filters);
	}

	@Override
	public void sendWirePacket(Player receiver, int id, byte[] bytes) throws InvocationTargetException {
		WirePacket packet = new WirePacket(id, bytes);
		sendWirePacket(receiver, packet);
	}

	@Override
	public void sendWirePacket(Player receiver, WirePacket packet) throws InvocationTargetException {
		WrappedChannel channel = playerInjection.getChannel(receiver);
		if (channel == null) {
			throw new InvocationTargetException(new NullPointerException(), "Failed to obtain channel for " + receiver.getName());
		}

		channel.writeAndFlush(packet);
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
		if (packet.getType().getSender() == Sender.SERVER)
			throw new IllegalArgumentException("Packet of sender SERVER cannot be sent to the server.");

		// And here too
		if (packetCreation.compareAndSet(false, true))
			incrementPhases(GamePhase.PLAYING);

		Object mcPacket = packet.getHandle();
		boolean cancelled = packetInjector.isCancelled(mcPacket);

		// Make sure the packet isn't cancelled
		if (cancelled) {
			packetInjector.setCancelled(mcPacket, false);
		}

		if (filters) {
			byte[] data = NetworkMarker.getByteBuffer(marker);
			PacketEvent event = packetInjector.packetRecieved(packet, sender, data);

			if (!event.isCancelled())
				mcPacket = event.getPacket().getHandle();
			else
				return;

		} else {
			// Let the monitors know though
			recievedListeners.invokePacketSending(
					reporter,
					PacketEvent.fromClient(this, packet, marker, sender, false),
					ListenerPriority.MONITOR);
		}

		playerInjection.recieveClientPacket(sender, mcPacket);

		// Let it stay cancelled
		if (cancelled) {
			packetInjector.setCancelled(mcPacket, true);
		}
	}

	@Override
	@Deprecated
	public PacketContainer createPacket(int id) {
		return createPacket(PacketType.findLegacy(id), true);
	}

	@Override
	public PacketContainer createPacket(PacketType type) {
		return createPacket(type, true);
	}

	@Override
	@Deprecated
	public PacketContainer createPacket(int id, boolean forceDefaults) {
		return createPacket(PacketType.findLegacy(id), forceDefaults);
	}

	@Override
	public PacketContainer createPacket(PacketType type, boolean forceDefaults) {
		PacketContainer packet = new PacketContainer(type);

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
	@Deprecated
	public PacketConstructor createPacketConstructor(int id, Object... arguments) {
		return PacketConstructor.DEFAULT.withPacket(id, arguments);
	}

	@Override
	public PacketConstructor createPacketConstructor(PacketType type, Object... arguments) {
		return PacketConstructor.DEFAULT.withPacket(type, arguments);
	}

	@Override
	@Deprecated
	public Set<Integer> getSendingFilters() {
		return PacketRegistry.toLegacy(playerInjection.getSendingFilters());
	}

	@Override
	public Set<Integer> getReceivingFilters() {
		return PacketRegistry.toLegacy(packetInjector.getPacketHandlers());
	}
	@Override
	public Set<PacketType> getSendingFilterTypes() {
		return Collections.unmodifiableSet(playerInjection.getSendingFilters());
	}

	@Override
	public Set<PacketType> getReceivingFilterTypes() {
		return Collections.unmodifiableSet(packetInjector.getPacketHandlers());
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
	public void initializePlayers(List<Player> players) {
		for (Player player : players)
			playerInjection.injectPlayer(player, ConflictStrategy.OVERRIDE);
	}

	/**
	 * Uninitialize the packet injection of every player.
	 * @param players - list of players to uninject.
	 */
	public void uninitializePlayers(List<Player> players) {
		for (Player player : players) {
			playerInjection.uninjectPlayer(player);
		}
	}

	/**
	 * Register this protocol manager on Bukkit.
	 * 
	 * @param manager - Bukkit plugin manager that provides player join/leave events.
	 * @param plugin - the parent plugin.
	 */
	@Override
	public void registerEvents(PluginManager manager, final Plugin plugin) {
		if (spigotInjector != null && !spigotInjector.register(plugin))
			throw new IllegalArgumentException("Spigot has already been registered.");
		if (nettyInjector != null)
			nettyInjector.inject();

		try {
			manager.registerEvents(new Listener() {

				@EventHandler(priority = EventPriority.LOWEST)
				public void onPlayerLogin(PlayerLoginEvent event) {
					PacketFilterManager.this.onPlayerLogin(event);
				}

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

    private void onPlayerLogin(PlayerLoginEvent event) {
		playerInjection.updatePlayer(event.getPlayer());
    }

    private void onPrePlayerJoin(PlayerJoinEvent event) {
		playerInjection.updatePlayer(event.getPlayer());
    }

    private void onPlayerJoin(PlayerJoinEvent event) {
		try {
			// Let's clean up the other injection first.
			playerInjection.uninjectPlayer(event.getPlayer().getAddress());
			playerInjection.injectPlayer(event.getPlayer(), ConflictStrategy.OVERRIDE);
		} catch (ServerHandlerNull e) {
			// Caused by logged out players, or fake login events in MCPC+/Cauldron. Ignore it.
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
	@Deprecated
	public int getPacketID(Object packet) {
		return PacketRegistry.getPacketID(packet.getClass());
	}

	@Override
	public PacketType getPacketType(Object packet) {
		if (packet == null)
			throw new IllegalArgumentException("Packet cannot be NULL.");
		if (!MinecraftReflection.isPacketClass(packet))
			throw new IllegalArgumentException("The given object " + packet + " is not a packet.");

		PacketType type = PacketRegistry.getPacketType(packet.getClass());

		if (type != null) {
			return type;
		} else {
			throw new IllegalArgumentException(
					"Unable to find associated packet of " + packet + ": Lookup returned NULL.");
		}
	}

	@Override
	@Deprecated
	public void registerPacketClass(Class<?> clazz, int packetID) {
		PacketRegistry.getPacketToID().put(clazz, packetID);
	}

	@Override
	@Deprecated
	public void unregisterPacketClass(Class<?> clazz) {
		PacketRegistry.getPacketToID().remove(clazz);
	}

	@Override
	@Deprecated
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
			Object priorityLowest  = Enum.valueOf(eventPriority, "Lowest");
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

			Enhancer playerLow = EnhancerFactory.getInstance().createEnhancer();
			Enhancer playerEx = EnhancerFactory.getInstance().createEnhancer();
			Enhancer serverEx = EnhancerFactory.getInstance().createEnhancer();

			playerLow.setSuperclass(playerListener);
			playerLow.setClassLoader(classLoader);
			playerLow.setCallback(new MethodInterceptor() {
				@Override
				public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
						throws Throwable {
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
	@Deprecated
	public static Set<Integer> getServerPackets() throws FieldAccessException {
		return PacketRegistry.getServerPackets();
	}

	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	@Deprecated
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
		if (spigotInjector != null)
			spigotInjector.cleanupAll();
		if (nettyInjector != null)
			nettyInjector.close();

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
