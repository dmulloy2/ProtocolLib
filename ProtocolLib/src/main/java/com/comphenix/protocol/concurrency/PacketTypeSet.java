package com.comphenix.protocol.concurrency;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Represents a concurrent set of packet types.
 * @author Kristian
 */
public class PacketTypeSet {
	private Set<PacketType> types = Collections.newSetFromMap(Maps.<PacketType, Boolean>newConcurrentMap());
	private Set<Class<?>> classes = Collections.newSetFromMap(Maps.<Class<?>, Boolean>newConcurrentMap());
	
	public PacketTypeSet() {
		// Do nothing
	}
	
	public PacketTypeSet(Collection<? extends PacketType> values) {
		for (PacketType type : values) {
			addType(type);
		}
	}
	
	/**
	 * Add a particular type to the set.
	 * @param type - the type to add.
	 */
	public synchronized void addType(PacketType type) {
		Class<?> packetClass = getPacketClass(type);
		types.add(Preconditions.checkNotNull(type, "type cannot be NULL."));
		
		if (packetClass != null) {
			classes.add(getPacketClass(type));
		}
	}
	
	/**
	 * Add the given types to the set of packet types.
	 * @param types - the types to add.
	 */
	public synchronized void addAll(Iterable<? extends PacketType> types) {
		for (PacketType type : types) {
			addType(type);
		}
	}
	
	/**
	 * Remove a particular type to the set.
	 * @param type - the type to remove.
	 */
	public synchronized void removeType(PacketType type) {
		Class<?> packetClass = getPacketClass(type);
		types.remove(Preconditions.checkNotNull(type, "type cannot be NULL."));
		
		if (packetClass != null) {
			classes.remove(getPacketClass(type));
		}
	}
	
	/**
	 * Remove the given types from the set.
	 * @param type - the types to remove.
	 */
	public synchronized void removeAll(Iterable<? extends PacketType> types) {
		for (PacketType type : types) {
			removeType(type);
		}
	}
	
	/**
	 * Retrieve the packet class associated with a particular type.
	 * @param type - the packet type.
	 * @return The associated packet type.
	 */
	protected Class<?> getPacketClass(PacketType type) {
		return PacketRegistry.getPacketClassFromType(type);
	}
	
	/**
	 * Determine if the given packet type exists in the set.
	 * @param type - the type to find.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean contains(PacketType type) {
		return types.contains(type);
	}
	
	/**
	 * Determine if a packet type with the given packet class exists in the set.
	 * @param packetClass - the class to find.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean contains(Class<?> packetClass) {
		return classes.contains(packetClass);
	}
	
	/**
	 * Determine if the type of a packet is in the current set. 
	 * @param packet - the packet.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean containsPacket(Object packet) {
		if (packet == null)
			return false;
		return classes.contains(packet.getClass());
	}
	
	/**
	 * Retrieve a view of this packet type set.
	 * @return The packet type values.
	 */
	public Set<PacketType> values() {
		return types;
	}
	
	/**
	 * Retrieve the number of entries in the set.
	 * @return The number of entries.
	 */
	public int size() {
		return types.size();
	}
	
	public synchronized void clear() {
		types.clear();
		classes.clear();
	}
}
