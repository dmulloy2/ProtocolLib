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
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;

import net.sf.cglib.proxy.Factory;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.ListenerInvoker;
import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.comphenix.protocol.injector.packet.InterceptWritePacket;
import com.comphenix.protocol.injector.server.SocketInjector;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.VolatileField;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.MapMaker;

public abstract class PlayerInjector implements SocketInjector {
	// Disconnect method related reports
	public static final ReportType REPORT_ASSUME_DISCONNECT_METHOD = new ReportType("Cannot find disconnect method by name. Assuming %s.");
	public static final ReportType REPORT_INVALID_ARGUMENT_DISCONNECT = new ReportType("Invalid argument passed to disconnect method: %s");
	public static final ReportType REPORT_CANNOT_ACCESS_DISCONNECT = new ReportType("Unable to access disconnect method.");
	
	public static final ReportType REPORT_CANNOT_CLOSE_SOCKET = new ReportType("Unable to close socket.");
	public static final ReportType REPORT_ACCESS_DENIED_CLOSE_SOCKET = new ReportType("Insufficient permissions. Cannot close socket.");
	
	public static final ReportType REPORT_DETECTED_CUSTOM_SERVER_HANDLER =
			new ReportType("Detected server handler proxy type by another plugin. Conflict may occur!");
	public static final ReportType REPORT_CANNOT_PROXY_SERVER_HANDLER = new ReportType("Unable to load server handler from proxy type.");
	
	public static final ReportType REPORT_CANNOT_UPDATE_PLAYER = new ReportType("Cannot update player in PlayerEvent.");
	public static final ReportType REPORT_CANNOT_HANDLE_PACKET = new ReportType("Cannot handle server packet.");
	
	public static final ReportType REPORT_INVALID_NETWORK_MANAGER = new ReportType("NetworkManager doesn't appear to be valid.");
	
	// Net login handler stuff
	private static Field netLoginNetworkField;
	
	// Different disconnect methods
	private static Method loginDisconnect;
	private static Method serverDisconnect;
	
	// Cache previously retrieved fields
	protected static Field serverHandlerField;
	protected static Field proxyServerField;

	protected static Field networkManagerField;
	protected static Field netHandlerField;
	protected static Field socketField;
	protected static Field socketAddressField;
	
	private static Field inputField;
	private static Field entityPlayerField;
	
	// Whether or not we're using a proxy type
	private static boolean hasProxyType;
	
	// To add our injected array lists
	protected static StructureModifier<Object> networkModifier;
	
	// And methods
	protected static Method queueMethod;
	protected static Method processMethod;
		
	protected volatile Player player;
	protected boolean hasInitialized;
	
	// Reference to the player's network manager
	protected VolatileField networkManagerRef;
	protected VolatileField serverHandlerRef;
	protected Object networkManager;
	
	// Current net handler
	protected Object loginHandler;
	protected Object serverHandler;
	protected Object netHandler;
	
	// Current socket and address
	protected Socket socket;
	protected SocketAddress socketAddress;
	
	// The packet manager and filters
	protected ListenerInvoker invoker;
	
	// Previous data input
	protected DataInputStream cachedInput;
	
	// Handle errors
	protected ErrorReporter reporter;
	
	// Previous markers
	protected Map<Object, NetworkMarker> queuedMarkers = new MapMaker().weakKeys().makeMap();
	protected InterceptWritePacket writePacketInterceptor;
	
	// Whether or not the injector has been cleaned
	private boolean clean;
	
	// Whether or not to update the current player on the first Packet1Login
	boolean updateOnLogin;
	volatile Player updatedPlayer;
	
	public PlayerInjector(ErrorReporter reporter, Player player, ListenerInvoker invoker) {
		this.reporter = reporter;
		this.player = player;
		this.invoker = invoker;
		this.writePacketInterceptor = invoker.getInterceptWritePacket();
	}

	/**
	 * Retrieve the notch (NMS) entity player object.
	 * @param player - the player to retrieve.
	 * @return Notch player object.
	 */
	protected Object getEntityPlayer(Player player) {
		BukkitUnwrapper unwrapper = new BukkitUnwrapper();
		return unwrapper.unwrapItem(player);
	}
	
	/**
	 * Initialize all fields for this player injector, if it hasn't already.
	 * @param injectionSource - Injection source
	 * @throws IllegalAccessException An error has occured.
	 */
	public void initialize(Object injectionSource) throws IllegalAccessException {
		if (injectionSource == null)
			throw new IllegalArgumentException("injectionSource cannot be NULL");
		
		//Dispatch to the correct injection method
		if (injectionSource instanceof Player)
			initializePlayer((Player) injectionSource);
		else if (MinecraftReflection.isLoginHandler(injectionSource))
			initializeLogin(injectionSource);
		else
			throw new IllegalArgumentException("Cannot initialize a player hook using a " + injectionSource.getClass().getName());
	}
	
	/**
	 * Initialize the player injector using an actual player instance.
	 * @param player - the player to hook.
	 */
	public void initializePlayer(Player player) {
		Object notchEntity = getEntityPlayer(player);
		
		// Save the player too
		this.player = player;
		
		if (!hasInitialized) {
			// Do this first, in case we encounter an exception
			hasInitialized = true;
			
			// Retrieve the server handler
			if (serverHandlerField == null) {
				serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType(
									  "NetServerHandler", MinecraftReflection.getPlayerConnectionClass());
				proxyServerField = getProxyField(notchEntity, serverHandlerField);
			}
			
			// Yo dawg
			serverHandlerRef = new VolatileField(serverHandlerField, notchEntity);
			serverHandler = serverHandlerRef.getValue();

			// Next, get the network manager
			if (networkManagerField == null)
				networkManagerField = FuzzyReflection.fromObject(serverHandler).getFieldByType(
									   "networkManager", MinecraftReflection.getNetworkManagerClass());
			initializeNetworkManager(networkManagerField, serverHandler);
		}
	}
	
	/**
	 * Initialize the player injector from a NetLoginHandler.
	 * @param netLoginHandler - the net login handler to inject.
	 */
	public void initializeLogin(Object netLoginHandler) {
		if (!hasInitialized) {
			// Just in case
			if (!MinecraftReflection.isLoginHandler(netLoginHandler))
				throw new IllegalArgumentException("netLoginHandler (" + netLoginHandler + ") is not a " +
							MinecraftReflection.getNetLoginHandlerName());
			
			hasInitialized = true;
			loginHandler = netLoginHandler;
			
			if (netLoginNetworkField == null)
				netLoginNetworkField =  FuzzyReflection.fromObject(netLoginHandler).
										  getFieldByType("networkManager", MinecraftReflection.getNetworkManagerClass());
			initializeNetworkManager(netLoginNetworkField, netLoginHandler);
		}
	}
	
	private void initializeNetworkManager(Field reference, Object container) {
		networkManagerRef = new VolatileField(reference, container);
		networkManager = networkManagerRef.getValue();
		
		// No, don't do it
		if (networkManager instanceof Factory) {
			return;
		}
		
		// Create the network manager modifier from the actual object type
		if (networkManager != null && networkModifier == null)
			networkModifier = new StructureModifier<Object>(networkManager.getClass(), null, false);
		
		// And the queue method
		if (queueMethod == null)
			queueMethod = FuzzyReflection.fromClass(reference.getType()).
							getMethodByParameters("queue", MinecraftReflection.getPacketClass());
	}
	
	/**
	 * Retrieve whether or not the server handler is a proxy object.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	protected boolean hasProxyServerHandler() {
		return hasProxyType;
	}
	
	/**
	 * Retrieve the current network manager.
	 * @return Current network manager.
	 */
	public Object getNetworkManager() {
		return networkManagerRef.getValue();
	}
	
	/**
	 * Retrieve the current server handler (PlayerConnection).
	 * @return Current server handler.
	 */
	public Object getServerHandler() {
		return serverHandlerRef.getValue();
	}
	
	/**
	 * Set the current network manager.
	 * @param value - new network manager.
	 * @param force - whether or not to save this value.
	 */
	public void setNetworkManager(Object value, boolean force) {
		networkManagerRef.setValue(value);
		
		if (force)
			networkManagerRef.saveValue();
		initializeNetworkManager(networkManagerField, serverHandler);
	}
	
	/**
	 * Retrieve the associated socket of this player.
	 * @return The associated socket.
	 * @throws IllegalAccessException If we're unable to read the socket field.
	 */
	@Override
	public Socket getSocket() throws IllegalAccessException {
		try {
			if (socketField == null)
				socketField = FuzzyReflection.fromObject(networkManager, true).
								getFieldListByType(Socket.class).get(0);
			if (socket == null)
				socket = (Socket) FieldUtils.readField(socketField, networkManager, true);
			return socket;
			
		} catch (IndexOutOfBoundsException e) {
			throw new IllegalAccessException("Unable to read the socket field.");
		}
	}
	
	/**
	 * Retrieve the associated remote address of a player.
	 * @return The associated remote address.
	 * @throws IllegalAccessException If we're unable to read the socket address field.
	 */
	@Override
	public SocketAddress getAddress() throws IllegalAccessException {
		try {
			if (socketAddressField == null)
				socketAddressField = FuzzyReflection.fromObject(networkManager, true).
										getFieldListByType(SocketAddress.class).get(0);
			if (socketAddress == null)
				socketAddress = (SocketAddress) FieldUtils.readField(socketAddressField, networkManager, true);
			return socketAddress;
			
		} catch (IndexOutOfBoundsException e) {
			// Inform about the state of the network manager too
			reporter.reportWarning(
					this, Report.newBuilder(REPORT_INVALID_NETWORK_MANAGER).callerParam(networkManager).build());
			throw new IllegalAccessException("Unable to read the socket address field.");
		}
	}
	
	/**
	 * Attempt to disconnect the current client.
	 * @param message - the message to display.
	 * @throws InvocationTargetException If disconnection failed.
	 */
	@Override
	public void disconnect(String message) throws InvocationTargetException {
		// Get a non-null handler
		boolean usingNetServer = serverHandler != null;
		
		Object handler = usingNetServer ? serverHandler : loginHandler;
		Method disconnect = usingNetServer ? serverDisconnect : loginDisconnect;
		
		// Execute disconnect on it
		if (handler != null) {
			if (disconnect == null) {
				try {
					disconnect = FuzzyReflection.fromObject(handler).getMethodByName("disconnect.*");
				} catch (IllegalArgumentException e) {
					// Just assume it's the first String method
					disconnect = FuzzyReflection.fromObject(handler).getMethodByParameters("disconnect", String.class);
					reporter.reportWarning(this, Report.newBuilder(REPORT_ASSUME_DISCONNECT_METHOD).messageParam(disconnect));
				}
				
				// Save the method for later
				if (usingNetServer)
					serverDisconnect = disconnect;
				else
					loginDisconnect = disconnect;
			}
			
			try {
				disconnect.invoke(handler, message);
				return;
			} catch (IllegalArgumentException e) {
				reporter.reportDetailed(this, Report.newBuilder(REPORT_INVALID_ARGUMENT_DISCONNECT).error(e).messageParam(message).callerParam(handler));
			} catch (IllegalAccessException e) {
				reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_ACCESS_DISCONNECT).error(e));
			}
		}
		
		// Fuck it
		try {
			Socket socket = getSocket();
			
			try {
				socket.close();
			} catch (IOException e) {
				reporter.reportDetailed(this, Report.newBuilder(REPORT_CANNOT_CLOSE_SOCKET).error(e).callerParam(socket));
			}
			
		} catch (IllegalAccessException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_ACCESS_DENIED_CLOSE_SOCKET).error(e));
		}
	}
	
	private Field getProxyField(Object notchEntity, Field serverField) {
		try {
			Object currentHandler = FieldUtils.readField(serverHandlerField, notchEntity, true);

			// This is bad
			if (currentHandler == null)
				throw new ServerHandlerNull();
			
			// See if this isn't a standard net handler class
			if (!isStandardMinecraftNetHandler(currentHandler)) {
				// This is our proxy object
				if (currentHandler instanceof Factory)
					return null;
				
				hasProxyType = true;
				reporter.reportWarning(this, Report.newBuilder(REPORT_DETECTED_CUSTOM_SERVER_HANDLER).callerParam(serverField));
				
				// No? Is it a Proxy type?
				try {
					FuzzyReflection reflection = FuzzyReflection.fromObject(currentHandler, true);
					
					// It might be
					return reflection.getFieldByType("NetServerHandler", MinecraftReflection.getPlayerConnectionClass());
					
				} catch (RuntimeException e) {
					// Damn
				}
			}
			
		} catch (IllegalAccessException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_PROXY_SERVER_HANDLER).error(e).callerParam(notchEntity, serverField));
		}

		// Nope, just go with it
		return null;
	}
	
	/**
	 * Determine if a given object is a standard Minecraft net handler.
	 * @param obj the object to test.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	private boolean isStandardMinecraftNetHandler(Object obj) {
		if (obj == null)
			return false;
		Class<?> clazz = obj.getClass();
		
		return MinecraftReflection.getNetLoginHandlerClass().equals(clazz) ||
			   MinecraftReflection.getPlayerConnectionClass().equals(clazz);
	}
	
	/**
	 * Retrieves the current net handler for this player.
	 * @return Current net handler.
	 * @throws IllegalAccessException Unable to find or retrieve net handler.
	 */
	protected Object getNetHandler() throws IllegalAccessException {
		return getNetHandler(false);
	}
	
	/**
	 * Retrieves the current net handler for this player.
	 * @param refresh - Whether or not to refresh
	 * @return Current net handler.
	 * @throws IllegalAccessException Unable to find or retrieve net handler.
	 * @return The current net handler for this player
	 */
	protected Object getNetHandler(boolean refresh) throws IllegalAccessException {
		// What a mess
		try {
			if (netHandlerField == null)
				netHandlerField = FuzzyReflection.fromClass(networkManager.getClass(), true).
									getFieldByType("NetHandler", MinecraftReflection.getNetHandlerClass());
		} catch (RuntimeException e1) {
			// Swallow it
		}
		
		// Second attempt
		if (netHandlerField == null) {
			try {
				// Well, that sucks. Try just Minecraft objects then.
				netHandlerField = FuzzyReflection.fromClass(networkManager.getClass(), true).
									 getFieldByType(MinecraftReflection.getMinecraftObjectRegex());
				
			} catch (RuntimeException e2) {
				throw new IllegalAccessException("Cannot locate net handler. " + e2.getMessage());
			}
		}
		
		// Get the handler
		if (netHandler == null || refresh)
			netHandler = FieldUtils.readField(netHandlerField, networkManager, true);
		return netHandler;
	}
	
	/**
	 * Retrieve the stored entity player from a given NetHandler.
	 * @param netHandler - the nethandler to retrieve it from.
	 * @return The stored entity player.
	 * @throws IllegalAccessException If the reflection failed.
	 */
	private Object getEntityPlayer(Object netHandler) throws IllegalAccessException {
		if (entityPlayerField == null)
			entityPlayerField = FuzzyReflection.fromObject(netHandler).getFieldByType(
								 "EntityPlayer", MinecraftReflection.getEntityPlayerClass());
		return FieldUtils.readField(entityPlayerField, netHandler);
	}
	
	/**
	 * Processes the given packet as if it was transmitted by the current player.
	 * @param packet - packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Object packet) throws IllegalAccessException, InvocationTargetException {
		
		Object netHandler = getNetHandler();
		
		// Get the process method
		if (processMethod == null) {
			try {
				processMethod = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass()).
								  getMethodByParameters("processPacket", netHandlerField.getType());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Cannot locate process packet method: " + e.getMessage());
			}
		}
	
		// We're ready
		try {
			processMethod.invoke(packet, netHandler);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Method " + processMethod.getName() + " is not compatible.");
		} catch (InvocationTargetException e) {
			throw e;
		}
	}
	
	/**
	 * Send a packet to the client.
	 * @param packet - server packet to send.
	 * @param marker - the network marker.
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @throws InvocationTargetException If an error occured when sending the packet.
	 */
	@Override
	public abstract void sendServerPacket(Object packet, NetworkMarker marker, boolean filtered) throws InvocationTargetException;
	
	/**
	 * Inject a hook to catch packets sent to the current player.
	 */
	public abstract void injectManager();
	
	/**
	 * Remove all hooks and modifications.
	 */
	public final void cleanupAll() {
		if (!clean) {
			cleanHook();
			writePacketInterceptor.cleanup();
		}
		clean = true;
	}

	/**
	 * Clean up after the player has disconnected.
	 */
	public abstract void handleDisconnect();
	
	/**
	 * Override to add custom cleanup behavior.
	 */
	protected abstract void cleanHook();
	
	/**
	 * Determine whether or not this hook has already been cleaned.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isClean() {
		return clean;
	}
	
	/**
	 * Determine if this inject method can even be attempted.
	 * @param state - Game phase
	 * @return TRUE if can be attempted, though possibly with failure, FALSE otherwise.
	 */
	public abstract boolean canInject(GamePhase state);
	
	/**
	 * Retrieve the hook type this class represents.
	 * @return Hook type this class represents.
	 */
	public abstract PlayerInjectHooks getHookType();
	
	/**
	 * Invoked before a new listener is registered.
	 * <p>
	 * The player injector should only return a non-null value if some or all of the packet IDs are unsupported.
	 * @param version - the current Minecraft version, or NULL if unknown.
	 * @param listener - the listener that is about to be registered.
	 * @return A error message with the unsupported packet IDs, or NULL if this listener is valid.
	 */
	public abstract UnsupportedListener checkListener(MinecraftVersion version, PacketListener listener);
	
	/**
	 * Allows a packet to be sent by the listeners.
	 * @param packet - packet to sent.
	 * @return The given packet, or the packet replaced by the listeners.
	 */
	@SuppressWarnings("deprecation")
	public Object handlePacketSending(Object packet) {
		try {
			// Get the packet ID too
			Integer id = invoker.getPacketID(packet);
			Player currentPlayer = player;
			
			// Hack #1
			if (updateOnLogin) {
				if (updatedPlayer == null) {
					try {
						final Object handler = getNetHandler(true);
						
						// Is this a net server class?
						if (MinecraftReflection.getPlayerConnectionClass().isAssignableFrom(handler.getClass())) {
							setUpdatedPlayer(
								(Player) MinecraftReflection.getBukkitEntity(getEntityPlayer(handler))
							);
						}
					} catch (IllegalAccessException e) {
						reporter.reportDetailed(this, Report.newBuilder(REPORT_CANNOT_UPDATE_PLAYER).error(e).callerParam(packet));
					}
				}
				
				// This will only occur in the NetLoginHandler injection
				if (updatedPlayer != null) {
					currentPlayer = updatedPlayer;
					updateOnLogin = false;
				}
			}
			
			// Make sure we're listening
			if (id != null && hasListener(id)) {
				NetworkMarker marker = queuedMarkers.remove(packet);
				
				// A packet has been sent guys!
				PacketType type = PacketType.findLegacy(id, Sender.SERVER);
				PacketContainer container = new PacketContainer(type, packet);
				PacketEvent event = PacketEvent.fromServer(invoker, container, marker, currentPlayer);
				invoker.invokePacketSending(event);
				
				// Cancelling is pretty simple. Just ignore the packet.
				if (event.isCancelled())
					return null;
				
				// Right, remember to replace the packet again
				Object result = event.getPacket().getHandle();
				marker = NetworkMarker.getNetworkMarker(event);

				// See if we need to proxy the write method
				if (result != null && (NetworkMarker.hasOutputHandlers(marker) || NetworkMarker.hasPostListeners(marker))) {
					result = writePacketInterceptor.constructProxy(result, event, marker);
				}
				return result;
			}
			
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_CANNOT_HANDLE_PACKET).error(e).callerParam(packet));
		}
		
		return packet;
	}
	
	/**
	 * Determine if the given injector is listening for this packet ID.
	 * @param packetID - packet ID to check.
	 * @return TRUE if it is, FALSE oterhwise.
	 */
	protected abstract boolean hasListener(int packetID);
	
	/**
	 * Retrieve the current player's input stream.
	 * @param cache - whether or not to cache the result of this method.
	 * @return The player's input stream.
	 */
	public DataInputStream getInputStream(boolean cache) {
		// And the data input stream that we'll use to identify a player
		if (networkManager == null)
			throw new IllegalStateException("Network manager is NULL.");
		if (inputField == null)
			inputField = FuzzyReflection.fromObject(networkManager, true).
							getFieldByType("java\\.io\\.DataInputStream");

		// Get the associated input stream
		try {
			if (cache && cachedInput != null)
				return cachedInput;
			
			// Save to cache
			cachedInput = (DataInputStream) FieldUtils.readField(inputField, networkManager, true);
			return cachedInput;
			
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to read input stream.", e);
		}
	}
	
	/**
	 * Retrieve the hooked player.
	 */
	@Override
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Set the hooked player.
	 * <p>
	 * Should only be called during the creation of the injector.
	 * @param player - the new hooked player.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	/**
	 * Object that can invoke the packet events.
	 * @return Packet event invoker.
	 */
	public ListenerInvoker getInvoker() {
		return invoker;
	}
	
	/**
	 * Retrieve the hooked player object OR the more up-to-date player instance.
	 * @return The hooked player, or a more up-to-date instance.
	 */
	@Override
	public Player getUpdatedPlayer() {
		if (updatedPlayer != null)
			return updatedPlayer;
		else
			return player;
	}
	
	@Override
	public void transferState(SocketInjector delegate) {
		// Do nothing
	}
	
	@Override
	public void setUpdatedPlayer(Player updatedPlayer) {
		this.updatedPlayer = updatedPlayer;
	}
	
	/**
	 * Indicates that a player's NetServerHandler or PlayerConnection was NULL.
	 * <p>
	 * This is usually because the player has just logged out, or due to it being a "fake" player in MCPC+/Cauldron.
	 * @author Kristian
	 */
	public static class ServerHandlerNull extends IllegalAccessError {
		private static final long serialVersionUID = 1L;

		public ServerHandlerNull() {
			super("Unable to fetch server handler: was NUll.");
		}

		public ServerHandlerNull(String s) {
			super(s);
		}
	}
}
