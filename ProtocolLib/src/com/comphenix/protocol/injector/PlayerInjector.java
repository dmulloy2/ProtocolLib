package com.comphenix.protocol.injector;

import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.VolatileField;

class PlayerInjector {

	// Cache previously retrieved fields
	private static Field serverHandlerField;
	private static Field networkManagerField;
	private static Field inputField;
	private static Field netHandlerField;
	
	// And methods
	private static Method queueMethod;
	private static Method processMethod;
		
	private Player player;
	private boolean hasInitialized;

	// Reference to the player's network manager
	private VolatileField networkManager;
	
	// The packet manager and filters
	private PacketFilterManager manager;
	private Set<Integer> packetFilters;
	
	// Previous data input
	private DataInputStream cachedInput;
	
	// Current net handler
	private Object netHandler;

	public PlayerInjector(Player player, PacketFilterManager manager, Set<Integer> packetFilters) throws IllegalAccessException {
		this.player = player;
		this.manager = manager;
		this.packetFilters = packetFilters;
		initialize();
	}

	private void initialize() throws IllegalAccessException {
	
		CraftPlayer craft = (CraftPlayer) player;
		EntityPlayer notchEntity = craft.getHandle();
		
		if (!hasInitialized) {
			// Do this first, in case we encounter an exception
			hasInitialized = true;
			
			// Retrieve the server handler
			if (serverHandlerField == null)
				serverHandlerField = FuzzyReflection.fromObject(notchEntity).getFieldByType(".*NetServerHandler");
			Object serverHandler = FieldUtils.readField(serverHandlerField, notchEntity);
			
			// Next, get the network manager 
			if (networkManagerField == null)
				networkManagerField = FuzzyReflection.fromObject(serverHandler).getFieldByType(".*NetworkManager");
			networkManager = new VolatileField(networkManagerField, serverHandler);
			
			// And the queue method
			if (queueMethod == null)
				queueMethod = FuzzyReflection.fromClass(networkManagerField.getType()).
								getMethodByParameters("queue", Packet.class );
			
			// And the data input stream that we'll use to identify a player
			if (inputField == null)
				inputField = FuzzyReflection.fromObject(networkManager.getOldValue(), true).
								getFieldByType("java\\.io\\.DataInputStream");
		}
	}
	
	/**
	 * Retrieves the current net handler for this player.
	 * @return Current net handler.
	 * @throws IllegalAccessException Unable to find or retrieve net handler.
	 */
	private Object getNetHandler() throws IllegalAccessException {
		
		// What a mess
		try {
			if (netHandlerField == null)
				netHandlerField = FuzzyReflection.fromClass(networkManagerField.getType(), true).
									getFieldByType("net\\.minecraft\\.NetHandler");
		} catch (RuntimeException e1) {
			try {
				// Well, that sucks. Try just Minecraft objects then.
				netHandlerField = FuzzyReflection.fromClass(networkManagerField.getType(), true).
									 getFieldByType(FuzzyReflection.MINECRAFT_OBJECT);
				
			} catch (RuntimeException e2) {
				return new IllegalAccessException("Cannot locate net handler. " + e2.getMessage());
			}
		}
		
		// Get the handler
		if (netHandler != null)
			netHandler = FieldUtils.readField(netHandlerField, networkManager.getOldValue(), true);
		return netHandler;
	}
	
	/**
	 * Processes the given packet as if it was transmitted by the current player.
	 * @param packet - packet to process.
	 * @throws IllegalAccessException If the reflection machinery failed.
	 * @throws InvocationTargetException If the underlying method caused an error.
	 */
	public void processPacket(Packet packet) throws IllegalAccessException, InvocationTargetException {
		
		Object netHandler = getNetHandler();
		
		// Get the process method
		if (processMethod == null) {
			try {
				processMethod = FuzzyReflection.fromClass(Packet.class).
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
	 * @param filtered - whether or not the packet will be filtered by our listeners.
	 * @param InvocationTargetException If an error occured when sending the packet.
	 */
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {
		Object networkDelegate = filtered ? networkManager.getValue() : networkManager.getOldValue();
		
		if (networkDelegate != null) {
			try {
				// Note that invocation target exception is a wrapper for a checked exception
				queueMethod.invoke(networkDelegate, packet);
				
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (InvocationTargetException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("Unable to access queue method.", e);
			}
		} else {
			throw new IllegalStateException("Unable to load network mananager. Cannot send packet.");
		}
	}
	
	public void injectManager() {
		
		if (networkManager != null) {
			final Class<?> networkInterface = networkManagerField.getType();
			final Object networkDelegate = networkManager.getOldValue();
			
			// Create our proxy object
			Object networkProxy = Proxy.newProxyInstance(networkInterface.getClassLoader(), 
					new Class<?>[] { networkInterface }, new InvocationHandler() {
				
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					// OH OH! The queue method!
					if (method.equals(queueMethod)) {
						Packet packet = (Packet) args[0];
						
						if (packet != null) {
							packet = handlePacketRecieved(packet);
							
							// A NULL packet indicate cancelling
							if (packet != null)
								args[0] = packet;
							else
								return null;
						}
					}
					
					// Delegate to our underlying class
					try {
						return method.invoke(networkDelegate, args);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				}
			});
		
			// Inject it, if we can.
			networkManager.setValue(networkProxy);
		}
	}
	
	/**
	 * Allows a packet to be recieved by the listeners.
	 * @param packet - packet to recieve.
	 * @return The given packet, or the packet replaced by the listeners.
	 */
	Packet handlePacketRecieved(Packet packet) {
		// Get the packet ID too
		Integer id = MinecraftRegistry.getPacketToID().get(packet.getClass());

		// Make sure we're listening
		if (packetFilters.contains(id)) {	
			// A packet has been sent guys!
			PacketContainer container = new PacketContainer(id, packet);
			PacketEvent event = PacketEvent.fromServer(manager, container, player);
			manager.invokePacketSending(event);
			
			// Cancelling is pretty simple. Just ignore the packet.
			if (event.isCancelled())
				return null;
			
			// Right, remember to replace the packet again
			return event.getPacket().getHandle();
		}
		
		return packet;
	}
	
	public DataInputStream getInputStream(boolean cache) {
		// Get the associated input stream
		try {
			if (cache && cachedInput != null)
				return cachedInput;
			
			// Save to cache
			cachedInput = (DataInputStream) FieldUtils.readField(inputField, networkManager.getOldValue(), true);
			return cachedInput;
			
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unable to read input stream.", e);
		}
	}
	
	public void cleanupAll() {
		// Clean up
		networkManager.revertValue();
	}
}
