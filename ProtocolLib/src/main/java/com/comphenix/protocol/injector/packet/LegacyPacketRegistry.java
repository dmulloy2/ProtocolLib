package com.comphenix.protocol.injector.packet;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import net.sf.cglib.proxy.Factory;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyClassContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.TroveWrapper;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

@SuppressWarnings("rawtypes")
class LegacyPacketRegistry {	
	private static final int MIN_SERVER_PACKETS = 5;
	private static final int MIN_CLIENT_PACKETS = 5;

	// Fuzzy reflection
	private FuzzyReflection packetRegistry;
	
	// The packet class to packet ID translator
	private Map<Class, Integer> packetToID;
	
	// Packet IDs to classes, grouped by whether or not they're vanilla or custom defined
	private Multimap<Integer, Class> customIdToPacket;
	private Map<Integer, Class> vanillaIdToPacket;
	
	// Whether or not certain packets are sent by the client or the server
	private ImmutableSet<Integer> serverPackets;
	private ImmutableSet<Integer> clientPackets;
	
	// The underlying sets
	private Set<Integer> serverPacketsRef;
	private Set<Integer> clientPacketsRef;
	
	// New proxy values
	private Map<Integer, Class> overwrittenPackets = new HashMap<Integer, Class>();
	
	// Vanilla packets
	private Map<Integer, Class> previousValues = new HashMap<Integer, Class>();
		
	/**
	 * Initialize the registry.
	 */
	@SuppressWarnings({ "unchecked" })
	public void initialize() {
		if (packetToID == null) {
			try {
				Field packetsField = getPacketRegistry().getFieldByType("packetsField", Map.class);
				packetToID = (Map<Class, Integer>) FieldUtils.readStaticField(packetsField, true);
			} catch (IllegalArgumentException e) {
				// Spigot 1.2.5 MCPC workaround
				try {
					packetToID = getSpigotWrapper();
				} catch (Exception e2) {
					// Very bad indeed
					throw new IllegalArgumentException(e.getMessage() + "; Spigot workaround failed.", e2);
				}
				
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to retrieve the packetClassToIdMap", e);
			}
			
			// Create the inverse maps
			customIdToPacket = InverseMaps.inverseMultimap(packetToID, new Predicate<Map.Entry<Class, Integer>>() {
				@Override
				public boolean apply(@Nullable Entry<Class, Integer> entry) {
					return !MinecraftReflection.isMinecraftClass(entry.getKey());
				}
			});
			
			// And the vanilla pack - here we assume a unique ID to class mapping
			vanillaIdToPacket = InverseMaps.inverseMap(packetToID, new Predicate<Map.Entry<Class, Integer>>() {
				@Override
				public boolean apply(@Nullable Entry<Class, Integer> entry) {
					return MinecraftReflection.isMinecraftClass(entry.getKey());
				}
			});
		}
		initializeSets();
	}
	
	@SuppressWarnings("unchecked")
	private void initializeSets() throws FieldAccessException {
		if (serverPacketsRef == null || clientPacketsRef == null) {
			List<Field> sets = getPacketRegistry().getFieldListByType(Set.class);
			
			try {
				if (sets.size() > 1) {
					serverPacketsRef = (Set<Integer>) FieldUtils.readStaticField(sets.get(0), true);
					clientPacketsRef = (Set<Integer>) FieldUtils.readStaticField(sets.get(1), true);
					
					// Impossible
					if (serverPacketsRef == null || clientPacketsRef == null)
						throw new FieldAccessException("Packet sets are in an illegal state.");
					
					// NEVER allow callers to modify the underlying sets
					serverPackets = ImmutableSet.copyOf(serverPacketsRef);
					clientPackets = ImmutableSet.copyOf(clientPacketsRef);
					
					// Check sizes
					if (serverPackets.size() < MIN_SERVER_PACKETS)
						throw new InsufficientPacketsException("Insufficient server packets.", false, serverPackets.size());
					if (clientPackets.size() < MIN_CLIENT_PACKETS)
						throw new InsufficientPacketsException("Insufficient client packets.", true, clientPackets.size());
					
				} else {
					throw new FieldAccessException("Cannot retrieve packet client/server sets.");
				}
				
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Cannot access field.", e);
			}
			
		} else {
			// Copy over again if it has changed
			if (serverPacketsRef != null && serverPacketsRef.size() != serverPackets.size())
				serverPackets = ImmutableSet.copyOf(serverPacketsRef);
			if (clientPacketsRef != null && clientPacketsRef.size() != clientPackets.size())
				clientPackets = ImmutableSet.copyOf(clientPacketsRef);
		}
	}
	
	/**
	 * Retrieve the packet mapping.
	 * @return The packet map.
	 */
	public Map<Class, Integer> getPacketToID() {
		// Initialize it, if we haven't already
		if (packetToID == null) {
			initialize();
		}
		return packetToID;
	}
	
	private Map<Class, Integer> getSpigotWrapper() throws IllegalAccessException {
		// If it talks like a duck, etc.
		// Perhaps it would be nice to have a proper duck typing library as well
		FuzzyClassContract mapLike = FuzzyClassContract.newBuilder().
				method(FuzzyMethodContract.newBuilder().
						nameExact("size").returnTypeExact(int.class)).
				method(FuzzyMethodContract.newBuilder().
						nameExact("put").parameterCount(2)).
				method(FuzzyMethodContract.newBuilder().
						nameExact("get").parameterCount(1)).
				build();
		
		Field packetsField = getPacketRegistry().getField(
				FuzzyFieldContract.newBuilder().typeMatches(mapLike).build());
		Object troveMap = FieldUtils.readStaticField(packetsField, true);
		
		// Fix incorrect no entry values
		TroveWrapper.transformNoEntryValue(troveMap, new Function<Integer, Integer>() {
			public Integer apply(Integer value) {
				if (value >= 0 && value < 256) {
					// Someone forgot to set the no entry value. Let's help them.
					return -1;
				}
				return value;
			}
		});
		
		// We'll assume this a Trove map
		return TroveWrapper.getDecoratedMap(troveMap);
	}
	
	/**
	 * Retrieve the cached fuzzy reflection instance allowing access to the packet registry.
	 * @return Reflected packet registry.
	 */ 
	private FuzzyReflection getPacketRegistry() {
		if (packetRegistry == null)
			packetRegistry = FuzzyReflection.fromClass(MinecraftReflection.getPacketClass(), true);
		return packetRegistry;
	}
	
	/**
	 * Retrieve the injected proxy classes handlig each packet ID.
	 * @return Injected classes.
	 */
	public Map<Integer, Class> getOverwrittenPackets() {
		return overwrittenPackets;
	}
	
	/**
	 * Retrieve the vanilla classes handling each packet ID.
	 * @return Vanilla classes.
	 */
	public Map<Integer, Class> getPreviousPackets() {
		return previousValues;
	}
	
	/**
	 * Retrieve every known and supported server packet.
	 * @return An immutable set of every known server packet.
	 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
	 */
	public Set<Integer> getServerPackets() throws FieldAccessException {
		initializeSets();
		
		// Sanity check. This is impossible!
		if (serverPackets != null && serverPackets.size() < MIN_SERVER_PACKETS) 
			throw new FieldAccessException("Server packet list is empty. Seems to be unsupported");
		return serverPackets;
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	public Set<Integer> getClientPackets() throws FieldAccessException {
		initializeSets();
		
		// As above
		if (clientPackets != null && clientPackets.size() < MIN_CLIENT_PACKETS) 
			throw new FieldAccessException("Client packet list is empty. Seems to be unsupported");
		return clientPackets;
	}
		
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * @param packetID - the packet ID.
	 * @return The associated class.
	 */
	public Class getPacketClassFromID(int packetID) {
		return getPacketClassFromID(packetID, false);
	}
	
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * @param packetID - the packet ID.
 	 * @param forceVanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 */
	public Class getPacketClassFromID(int packetID, boolean forceVanilla) {
		Map<Integer, Class> lookup = forceVanilla ? previousValues : overwrittenPackets;
		Class<?> result = null;
		
		// Optimized lookup
		if (lookup.containsKey(packetID)) {
			return removeEnhancer(lookup.get(packetID), forceVanilla);
		}
		
		// Refresh lookup tables
		getPacketToID();

		// See if we can look for non-vanilla classes
		if (!forceVanilla) {
			result = Iterables.getFirst(customIdToPacket.get(packetID), null);
		}
		if (result == null) {
			result = vanillaIdToPacket.get(packetID);
		}
		
		// See if we got it
		if (result != null)
			return result;
		else
			throw new IllegalArgumentException("The packet ID " + packetID + " is not registered.");
	}
	
	/**
	 * Retrieve the packet ID of a given packet.
	 * @param packet - the type of packet to check.
	 * @return The ID of the given packet.
	 * @throws IllegalArgumentException If this is not a valid packet.
	 */
	public int getPacketID(Class<?> packet) {
		if (packet == null)
			throw new IllegalArgumentException("Packet type class cannot be NULL.");
		if (!MinecraftReflection.getPacketClass().isAssignableFrom(packet))
			throw new IllegalArgumentException("Type must be a packet.");
		
		// The registry contains both the overridden and original packets
		return getPacketToID().get(packet);
	}
	
	/**
	 * Find the first superclass that is not a CBLib proxy object.
	 * @param clazz - the class whose hierachy we're going to search through.
	 * @param remove - whether or not to skip enhanced (proxy) classes.
	 * @return If remove is TRUE, the first superclass that is not a proxy.
	 */
	private static Class removeEnhancer(Class clazz, boolean remove) {
		if (remove) {
			// Get the underlying vanilla class
			while (Factory.class.isAssignableFrom(clazz) && !clazz.equals(Object.class)) {
				clazz = clazz.getSuperclass();
			}
		}
		return clazz;
	}
	
	/**
	 * Occurs when we were unable to retrieve all the packets in the registry.
	 * @author Kristian
	 */
	public static class InsufficientPacketsException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		private final boolean client;
		private final int packetCount;
		
		private InsufficientPacketsException(String message, boolean client, int packetCount) {
			super(message);
			this.client = client;
			this.packetCount = packetCount;
		}

		public boolean isClient() {
			return client;
		}
		
		public int getPacketCount() {
			return packetCount;
		}
	}
}
