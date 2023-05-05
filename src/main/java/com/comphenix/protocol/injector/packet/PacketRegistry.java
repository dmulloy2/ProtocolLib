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

package com.comphenix.protocol.injector.packet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

/**
 * Static packet registry in Minecraft.
 * @author Kristian
 */
public class PacketRegistry {
	// Whether or not the registry has been initialized
	private static volatile boolean INITIALIZED = false;

	/**
	 * Represents a register we are currently building.
	 * @author Kristian
	 */
	protected static class Register {
		// The main lookup table
		final Map<PacketType, Optional<Class<?>>> typeToClass = new ConcurrentHashMap<>();
		final Map<Class<?>, PacketType> classToType = new ConcurrentHashMap<>();

		volatile Set<PacketType> serverPackets = new HashSet<>();
		volatile Set<PacketType> clientPackets = new HashSet<>();
		final List<MapContainer> containers = new ArrayList<>();

		public Register() {}

		public void registerPacket(PacketType type, Class<?> clazz, Sender sender) {
			typeToClass.put(type, Optional.of(clazz));
			classToType.put(clazz, type);
			if (sender == Sender.CLIENT) {
				clientPackets.add(type);
			} else {
				serverPackets.add(type);
			}
		}

		public void addContainer(MapContainer container) {
			containers.add(container);
		}

		/**
		 * Determine if the current register is outdated.
		 * @return TRUE if it is, FALSE otherwise.
		 */
		public boolean isOutdated() {
			for (MapContainer container : containers) {
				if (container.hasChanged()) {
					return true;
				}
			}
			return false;
		}
	}

	protected static final Class<?> ENUM_PROTOCOL = MinecraftReflection.getEnumProtocolClass();

	// Current register
	protected static volatile Register REGISTER;

	/**
	 * Ensure that our local register is up-to-date with Minecraft.
	 * <p>
	 * This operation may block the calling thread.
	 */
	public static synchronized void synchronize() {
		// Check if the packet registry has changed
		if (REGISTER.isOutdated()) {
			initialize();
		}
	}

	protected static synchronized Register createOldRegister() {
		Object[] protocols = ENUM_PROTOCOL.getEnumConstants();

		// ID to Packet class maps
		final Map<Object, Map<Integer, Class<?>>> serverMaps = new LinkedHashMap<>();
		final Map<Object, Map<Integer, Class<?>>> clientMaps = new LinkedHashMap<>();

		Register result = new Register();
		StructureModifier<Object> modifier = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (modifier == null) {
				modifier = new StructureModifier<>(protocol.getClass().getSuperclass());
			}

			StructureModifier<Map<Object, Map<Integer, Class<?>>>> maps = modifier.withTarget(protocol).withType(Map.class);
			for (Map.Entry<Object, Map<Integer, Class<?>>> entry : maps.read(0).entrySet()) {
				String direction = entry.getKey().toString();
				if (direction.contains("CLIENTBOUND")) { // Sent by Server
					serverMaps.put(protocol, entry.getValue());
				} else if (direction.contains("SERVERBOUND")) { // Sent by Client
					clientMaps.put(protocol, entry.getValue());
				}
			}
		}

		// Maps we have to occasionally check have changed
		for (Object map : serverMaps.values()) {
			result.addContainer(new MapContainer(map));
		}

		for (Object map : clientMaps.values()) {
			result.addContainer(new MapContainer(map));
		}

		for (Object protocol : protocols) {
			Enum<?> enumProtocol = (Enum<?>) protocol;
			PacketType.Protocol equivalent = PacketType.Protocol.fromVanilla(enumProtocol);

			// Associate known types
			if (serverMaps.containsKey(protocol))
				associatePackets(result, serverMaps.get(protocol), equivalent, Sender.SERVER);
			if (clientMaps.containsKey(protocol))
				associatePackets(result, clientMaps.get(protocol), equivalent, Sender.CLIENT);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	private static synchronized Register createNewRegister() {
		Object[] protocols = ENUM_PROTOCOL.getEnumConstants();

		// ID to Packet class maps
		final Map<Object, Map<Class<?>, Integer>> serverMaps = new LinkedHashMap<>();
		final Map<Object, Map<Class<?>, Integer>> clientMaps = new LinkedHashMap<>();

		Register result = new Register();
		Field mainMapField = null;
		Field packetMapField = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (mainMapField == null) {
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(protocol.getClass(), true);
				mainMapField = fuzzy.getField(FuzzyFieldContract.newBuilder()
						.banModifier(Modifier.STATIC)
						.requireModifier(Modifier.FINAL)
						.typeDerivedOf(Map.class)
						.build());
				mainMapField.setAccessible(true);
			}

			Map<Object, Object> directionMap;

			try {
				directionMap = (Map<Object, Object>) mainMapField.get(protocol);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Failed to access packet map", ex);
			}

			for (Map.Entry<Object, Object> entry : directionMap.entrySet()) {
				Object holder = entry.getValue();
				if (packetMapField == null) {
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(holder.getClass(), true);
					packetMapField = fuzzy.getField(FuzzyFieldContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.requireModifier(Modifier.FINAL)
							.typeDerivedOf(Map.class)
							.build());
					packetMapField.setAccessible(true);
				}

				Map<Class<?>, Integer> packetMap;

				try {
					packetMap = (Map<Class<?>, Integer>) packetMapField.get(holder);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Failed to access packet map", ex);
				}

				String direction = entry.getKey().toString();
				if (direction.contains("CLIENTBOUND")) { // Sent by Server
					serverMaps.put(protocol, packetMap);
				} else if (direction.contains("SERVERBOUND")) { // Sent by Client
					clientMaps.put(protocol, packetMap);
				}
			}
		}

		// Maps we have to occasionally check have changed
		// TODO: Find equivalent in Object2IntMap

		/* for (Object map : serverMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Object map : clientMaps.values()) {
			result.containers.add(new MapContainer(map));
		} */

		for (Object protocol : protocols) {
			Enum<?> enumProtocol = (Enum<?>) protocol;
			PacketType.Protocol equivalent = PacketType.Protocol.fromVanilla(enumProtocol);

			// Associate known types
			if (serverMaps.containsKey(protocol)) {
				associatePackets(result, reverse(serverMaps.get(protocol)), equivalent, Sender.SERVER);
			}
			if (clientMaps.containsKey(protocol)) {
				associatePackets(result, reverse(clientMaps.get(protocol)), equivalent, Sender.CLIENT);
			}
		}

		return result;
	}

	/**
	 * Reverses a key->value map to value->key
	 * Non-deterministic behavior when multiple keys are mapped to the same value
	 */
	private static <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> newMap = new HashMap<>(map.size());
		for (Map.Entry<K, V> entry : map.entrySet()) {
			newMap.put(entry.getValue(), entry.getKey());
		}
		return newMap;
	}

	protected static void associatePackets(Register register, Map<Integer, Class<?>> lookup, PacketType.Protocol protocol, Sender sender) {
		for (Map.Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			int packetId = entry.getKey();
			Class<?> packetClass = entry.getValue();

			PacketType type = PacketType.fromCurrent(protocol, sender, packetId, packetClass);

			try {
				register.registerPacket(type, packetClass, sender);
			} catch (Exception ex) {
				ProtocolLogger.debug("Encountered an exception associating packet " + type, ex);
			}
		}
	}

	private static void associate(PacketType type, Class<?> clazz) {
		if (clazz != null) {
			REGISTER.typeToClass.put(type, Optional.of(clazz));
			REGISTER.classToType.put(clazz, type);
		} else {
			REGISTER.typeToClass.put(type, Optional.empty());
		}
	}

	private static final Object registryLock = new Object();

	/**
	 * Initializes the packet registry.
	 */
	private static void initialize() {
		if (INITIALIZED) {
			return;
		}

		synchronized (registryLock) {
			if (INITIALIZED) {
				return;
			}

			if (MinecraftVersion.BEE_UPDATE.atOrAbove()) {
				REGISTER = createNewRegister();
			} else {
				REGISTER = createOldRegister();
			}

			INITIALIZED = true;
		}
	}

	/**
	 * Determine if the given packet type is supported on the current server.
	 * @param type - the type to check.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isSupported(PacketType type) {
		initialize();
		return tryGetPacketClass(type).isPresent();
	}

	/**
	 * Retrieve every known and supported server packet type.
	 * @return Every server packet type.
	 */
	public static Set<PacketType> getServerPacketTypes() {
		initialize();
		synchronize();

		return Collections.unmodifiableSet(REGISTER.serverPackets);
	}
	
	/**
	 * Retrieve every known and supported server packet type.
	 * @return Every server packet type.
	 */
	public static Set<PacketType> getClientPacketTypes() {
		initialize();
		synchronize();

		return Collections.unmodifiableSet(REGISTER.clientPackets);
	}

	private static Class<?> searchForPacket(List<String> classNames) {
		for (String name : classNames) {
			try {
				Class<?> clazz = MinecraftReflection.getMinecraftClass(name);
				if (MinecraftReflection.getPacketClass().isAssignableFrom(clazz)
						&& !Modifier.isAbstract(clazz.getModifiers())) {
					return clazz;
				}
			} catch (Exception ignored) {}
		}

		return null;
	}

	/**
	 * Retrieves the correct packet class from a given type.
	 *
	 * @param type - the packet type.
	 * @param forceVanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 * @deprecated forceVanilla no longer has any effect
	 */
	@Deprecated
	public static Class<?> getPacketClassFromType(PacketType type, boolean forceVanilla) {
		return getPacketClassFromType(type);
	}

	public static Optional<Class<?>> tryGetPacketClass(PacketType type) {
		initialize();

		// Try the lookup first (may be null, so check contains)
		Optional<Class<?>> res = REGISTER.typeToClass.get(type);
		if (res != null) {
			if(res.isPresent() && MinecraftReflection.isBundleDelimiter(res.get())) {
				return MinecraftReflection.getPackedBundlePacketClass();
			}
			return res;
		}

		// Then try looking up the class names
		Class<?> clazz = searchForPacket(type.getClassNames());
		if (clazz != null) {
			// we'd like for it to be associated correctly from the get-go; this is OK on older versions though
			ProtocolLogger.warnAbove(type.getCurrentVersion(), "Updating associated class for {0} to {1}", type.name(), clazz);
		}

		// cache it for next time
		associate(type, clazz);
		if(clazz != null && MinecraftReflection.isBundleDelimiter(clazz)) {
			clazz = MinecraftReflection.getPackedBundlePacketClass().orElseThrow(() -> new IllegalStateException("Packet bundle class not found."));
		}
		return Optional.ofNullable(clazz);
	}

	/**
	 * Get the packet class associated with a given type. First attempts to read from the
	 * type-to-class mapping, and tries
	 * @param type the packet type
	 * @return The associated class
	 */
	public static Class<?> getPacketClassFromType(PacketType type) {
		return tryGetPacketClass(type)
				.orElseThrow(() -> new IllegalArgumentException("Could not find packet for type " + type.name()));
	}

	/**
	 * Retrieve the packet type of a given packet.
	 * @param packet - the class of the packet.
	 * @return The packet type, or NULL if not found.
	 */
	public static PacketType getPacketType(Class<?> packet) {
		initialize();

		if (MinecraftReflection.isBundlePacket(packet)) {
			return PacketType.Play.Server.BUNDLE;
		}
		
		return REGISTER.classToType.get(packet);
	}
	
	/**
	 * Retrieve the packet type of a given packet.
	 * @param packet - the class of the packet.
	 * @param sender - the sender of the packet, or NULL.
	 * @return The packet type, or NULL if not found.
	 * @deprecated sender no longer has any effect
	 */
	@Deprecated
	public static PacketType getPacketType(Class<?> packet, Sender sender) {
		return getPacketType(packet);
	}
}
