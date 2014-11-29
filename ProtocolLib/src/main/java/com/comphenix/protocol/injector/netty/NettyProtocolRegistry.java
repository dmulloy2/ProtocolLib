package com.comphenix.protocol.injector.netty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.packet.MapContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Represents a way of accessing the new netty Protocol enum.
 * @author Kristian
 */
// TODO: Handle modifications to the BiMap
public class NettyProtocolRegistry {
	/**
	 * Represents a register we are currently building.
	 * @author Kristian
	 */
	private static class Register {
		// The main lookup table
		public BiMap<PacketType, Class<?>> typeToClass = HashBiMap.create();
		public volatile Set<PacketType> serverPackets = Sets.newHashSet();
		public volatile Set<PacketType> clientPackets = Sets.newHashSet();
		public List<MapContainer> containers = Lists.newArrayList();
		
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
	
	private Class<?> enumProtocol;
	
	// Current register
	private volatile Register register;
	
	public NettyProtocolRegistry() {
		enumProtocol = MinecraftReflection.getEnumProtocolClass();
		initialize();
	}
	
	/**
	 * Retrieve an immutable view of the packet type lookup.
	 * @return The packet type lookup.
	 */
	public Map<PacketType, Class<?>> getPacketTypeLookup() {
		return Collections.unmodifiableMap(register.typeToClass);
	}
	
	/**
	 * Retrieve an immutable view of the class to packet type lookup.
	 * @return The packet type lookup.
	 */
	public Map<Class<?>, PacketType> getPacketClassLookup() {
		return Collections.unmodifiableMap(register.typeToClass.inverse());
	}
	
	/**
	 * Retrieve every known client packet, from every protocol.
	 * @return Every client packet.
	 */
	public Set<PacketType> getClientPackets() {
		return Collections.unmodifiableSet(register.clientPackets);
	}
	
	/**
	 * Retrieve every known server packet, from every protocol.
	 * @return Every server packet.
	 */
	public Set<PacketType> getServerPackets() {
		return Collections.unmodifiableSet(register.serverPackets);
	}
	
	/**
	 * Ensure that our local register is up-to-date with Minecraft.
	 * <p>
	 * This operation may block the calling thread.
	 */
	public synchronized void synchronize() {
		// See if the register is outdated
		if (register.isOutdated()) {
			initialize();
		}
	}
	
	/**
	 * Load the packet lookup tables in each protocol.
	 */
	private synchronized void initialize() {
		Object[] protocols = enumProtocol.getEnumConstants();

		// ID to Packet class maps
		Map<Object, Map<Integer, Class<?>>> serverMaps = Maps.newLinkedHashMap();
		Map<Object, Map<Integer, Class<?>>> clientMaps = Maps.newLinkedHashMap();

		Register result = new Register();
		StructureModifier<Object> modifier = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (modifier == null)
				modifier = new StructureModifier<Object>(protocol.getClass().getSuperclass(), false);
			StructureModifier<Map<Object, Map<Integer, Class<?>>>> maps = modifier.withTarget(protocol).withType(Map.class);
			for (Entry<Object, Map<Integer, Class<?>>> entry : maps.read(0).entrySet()) {
				String direction = entry.getKey().toString();
				if (direction.contains("CLIENTBOUND")) { // Sent by Server
					serverMaps.put(protocol, entry.getValue());
				} else if (direction.contains("SERVERBOUND")) { // Sent by Client
					clientMaps.put(protocol, entry.getValue());
				}
			}
		}

		// Maps we have to occationally check have changed
		for (Map<Integer, Class<?>> map : serverMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Map<Integer, Class<?>> map : clientMaps.values()) {
			result.containers.add(new MapContainer(map));
		}
 		
//		// Heuristic - there are more server packets than client packets
//		if (sum(clientMaps) > sum(serverMaps)) {
//			// Swap if this is violated
//			List<Map<Integer, Class<?>>> temp = serverMaps;
//			serverMaps = clientMaps;
//			clientMaps = temp;
//		}

		for (int i = 0; i < protocols.length; i++) {
			Object protocol = protocols[i];
			Enum<?> enumProtocol = (Enum<?>) protocol;
			Protocol equivalent = Protocol.fromVanilla(enumProtocol);
			
			// Associate known types
			if (serverMaps.containsKey(protocol))
				associatePackets(result, serverMaps.get(protocol), equivalent, Sender.SERVER);
			if (clientMaps.containsKey(protocol))
				associatePackets(result, clientMaps.get(protocol), equivalent, Sender.CLIENT);
		}
		// Exchange (thread safe, as we have only one writer)
		this.register = result;
	}
	
	private void associatePackets(Register register, Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender) {
		for (Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			PacketType type = PacketType.fromCurrent(protocol, sender, entry.getKey(), PacketType.UNKNOWN_PACKET);
			register.typeToClass.put(type, entry.getValue());
			
			if (sender == Sender.SERVER)
				register.serverPackets.add(type);
			if (sender == Sender.CLIENT)
				register.clientPackets.add(type);
		}
	}
	
//	/**
//	 * Retrieve the number of mapping in all the maps.
//	 * @param maps - iterable of maps.
//	 * @return The sum of all the entries.
//	 */
//	private int sum(Iterable<? extends Map<Integer, Class<?>>> maps) {
//		int count = 0;
//
//		for (Map<Integer, Class<?>> map : maps)
//			count += map.size();
//		return count;
//	}
}
