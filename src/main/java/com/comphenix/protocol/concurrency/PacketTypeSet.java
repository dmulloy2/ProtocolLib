package com.comphenix.protocol.concurrency;

import com.comphenix.protocol.PacketType;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a concurrent set of packet types.
 *
 * @author Kristian
 */
public class PacketTypeSet {

	private final Set<PacketType> types;
	private final Set<Class<?>> classes;

	public PacketTypeSet() {
		this.types = new HashSet<>(16, 0.9f);
		this.classes = new HashSet<>(16, 0.9f);
	}

	public PacketTypeSet(Collection<? extends PacketType> values) {
		this.types = new HashSet<>(values.size(), 0.9f);
		this.classes = new HashSet<>(values.size(), 0.9f);

		this.addAll(values);
	}

	/**
	 * Add a particular type to the set.
	 *
	 * @param type - the type to add.
	 */
	public void addType(PacketType type) {
		this.types.add(type);

		Class<?> packetClass = type.getPacketClass();
		if (packetClass != null) {
			this.classes.add(packetClass);
		}
	}

	/**
	 * Add the given types to the set of packet types.
	 *
	 * @param types - the types to add.
	 */
	public void addAll(Iterable<? extends PacketType> types) {
		for (PacketType type : types) {
			this.addType(type);
		}
	}

	/**
	 * Remove a particular type to the set.
	 *
	 * @param type - the type to remove.
	 */
	public void removeType(PacketType type) {
		this.types.remove(type);

		Class<?> packetClass = type.getPacketClass();
		if (packetClass != null) {
			this.classes.remove(packetClass);
		}
	}

	/**
	 * Remove the given types from the set.
	 *
	 * @param types Types to remove
	 */
	public void removeAll(Iterable<? extends PacketType> types) {
		for (PacketType type : types) {
			this.removeType(type);
		}
	}

	/**
	 * Determine if the given packet type exists in the set.
	 *
	 * @param type - the type to find.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean contains(PacketType type) {
		return this.types.contains(type);
	}

	/**
	 * Determine if a packet type with the given packet class exists in the set.
	 *
	 * @param packetClass - the class to find.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean contains(Class<?> packetClass) {
		return this.classes.contains(packetClass);
	}

	/**
	 * Determine if the type of a packet is in the current set.
	 *
	 * @param packet - the packet.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean containsPacket(Object packet) {
		return packet != null && this.classes.contains(packet.getClass());
	}

	/**
	 * Retrieve a view of this packet type set.
	 *
	 * @return The packet type values.
	 */
	public Set<PacketType> values() {
		return ImmutableSet.copyOf(this.types);
	}

	/**
	 * Retrieve the number of entries in the set.
	 *
	 * @return The number of entries.
	 */
	public int size() {
		return this.types.size();
	}

	public void clear() {
		this.types.clear();
		this.classes.clear();
	}
}
