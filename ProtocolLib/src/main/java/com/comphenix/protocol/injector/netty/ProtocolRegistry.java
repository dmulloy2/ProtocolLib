package com.comphenix.protocol.injector.netty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.packet.MapContainer;
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
public abstract class ProtocolRegistry {
	/**
	 * Represents a register we are currently building.
	 * @author Kristian
	 */
	protected static class Register {
		// The main lookup table
		public BiMap<PacketType, Class<?>> typeToClass = HashBiMap.create();
		public volatile Set<PacketType> serverPackets = Sets.newHashSet();
		public volatile Set<PacketType> clientPackets = Sets.newHashSet();
		public List<MapContainer> containers = Lists.newArrayList();

		public Register() {
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
	
	protected Class<?> enumProtocol;
	
	// Current register
	protected volatile Register register;
	
	public ProtocolRegistry() {
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
	protected abstract void initialize();

	protected abstract void associatePackets(Register register, Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender);

	/**
	 * Retrieve the number of mapping in all the maps.
	 * @param maps - iterable of maps.
	 * @return The sum of all the entries.
	 */
	protected final int sum(Iterable<? extends Map<Integer, Class<?>>> maps) {
		int count = 0;

		for (Map<Integer, Class<?>> map : maps)
			count += map.size();
		return count;
	}
}