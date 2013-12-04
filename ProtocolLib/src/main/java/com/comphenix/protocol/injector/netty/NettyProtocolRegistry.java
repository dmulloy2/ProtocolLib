package com.comphenix.protocol.injector.netty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Represents a way of accessing the new netty Protocol enum.
 * @author Kristian
 */
// TODO: Handle modifications to the BiMap
public class NettyProtocolRegistry {
	private Class<?> enumProtocol;
	
	// The main lookup table
	private BiMap<PacketType, Class<?>> typeToClass = HashBiMap.create();
	private Set<PacketType> serverPackets = Sets.newHashSet();
	private Set<PacketType> clientPackets = Sets.newHashSet();
	
	public NettyProtocolRegistry() {
		enumProtocol = MinecraftReflection.getEnumProtocolClass();
		initialize();
	}
	
	/**
	 * Retrieve an immutable view of the packet type lookup.
	 * @return The packet type lookup.
	 */
	public Map<PacketType, Class<?>> getPacketTypeLookup() {
		return Collections.unmodifiableMap(typeToClass);
	}
	
	/**
	 * Retrieve an immutable view of the class to packet tyåe lookup.
	 * @return The packet type lookup.
	 */
	public Map<Class<?>, PacketType> getPacketClassLookup() {
		return Collections.unmodifiableMap(typeToClass.inverse());
	}
	
	/**
	 * Retrieve every known client packet, from every protocol.
	 * @return Every client packet.
	 */
	public Set<PacketType> getClientPackets() {
		return Collections.unmodifiableSet(clientPackets);
	}
	
	/**
	 * Retrieve every known server packet, from every protocol.
	 * @return Every server packet.
	 */
	public Set<PacketType> getServerPackets() {
		return Collections.unmodifiableSet(serverPackets);
	}
	
	/**
	 * Load the packet lookup tables in each protocol.
	 */
	private void initialize() {
		final Object[] protocols = enumProtocol.getEnumConstants();
		List<Map<Integer, Class<?>>> serverPackets = Lists.newArrayList();
		List<Map<Integer, Class<?>>> clientPackets = Lists.newArrayList();
		StructureModifier<Object> modifier = null;
		
		for (Object protocol : protocols) {
			if (modifier == null)
				modifier = new StructureModifier<Object>(protocol.getClass().getSuperclass(), false);
			StructureModifier<Map<Integer, Class<?>>> maps = modifier.withTarget(protocol).withType(Map.class);
			
			serverPackets.add(maps.read(0));
			clientPackets.add(maps.read(1));
		}
		
		// Heuristic - there are more server packets than client packets
		if (sum(clientPackets) > sum(serverPackets)) {
			// Swap if this is violated
			List<Map<Integer, Class<?>>> temp = serverPackets;
			serverPackets = clientPackets;
			clientPackets = temp;
		}
		
		for (int i = 0; i < protocols.length; i++) {
			Enum<?> enumProtocol = (Enum<?>) protocols[i];
			Protocol equivalent = Protocol.fromVanilla(enumProtocol);
			
			// Associate known types
			associatePackets(serverPackets.get(i), equivalent, Sender.SERVER);
			associatePackets(clientPackets.get(i), equivalent, Sender.CLIENT);
		}
	}
	
	private void associatePackets(Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender) {
		for (Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			PacketType type = PacketType.fromCurrent(protocol, sender, entry.getKey(), PacketType.UNKNOWN_PACKET);
			typeToClass.put(type, entry.getValue());
			
			if (sender == Sender.SERVER)
				serverPackets.add(type);
			if (sender == Sender.CLIENT)
				clientPackets.add(type);
		}
	}
	
	/**
	 * Retrieve the number of mapping in all the maps.
	 * @param maps - iterable of maps.
	 * @return The sum of all the entries.
	 */
	private int sum(Iterable<? extends Map<Integer, Class<?>>> maps) {
		int count = 0;
		
		for (Map<Integer, Class<?>> map : maps) 
			count += map.size();
		return count;
	}
}
