package com.comphenix.protocol.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketListener;
import com.google.common.collect.ImmutableSet;

/**
 * Manages the association between packet types and their corresponding
 * listeners.
 * <p>
 * This class is thread-safe for modifications. All read methods work on a
 * lock-free, best-effort principle, ensuring fast access.
 * </p>
 */
public class PacketTypeListenerSet {

    // Map to store packet types and their associated listeners
    private final Map<PacketType, Set<PacketListener>> typeMap = new HashMap<>();
    // Set to store packet classes of packet types that have listeners
    private final Set<Class<?>> classSet = new HashSet<>();

    /**
     * Adds a listener for a specific packet type.
     *
     * @param packetType the packet type
     * @param listener   the listener to add
     * @return {@code true} if the listener and packetType was added to the set;
     *         {@code false} otherwise
     * @throws NullPointerException if the packetType or listener is null
     */
    public synchronized boolean add(PacketType packetType, PacketListener listener) {
        Objects.requireNonNull(packetType, "packetType cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        Set<PacketListener> listenerSet = this.typeMap.computeIfAbsent(packetType, key -> new HashSet<>());
        if (!listenerSet.add(listener)) {
            return false;
        }

        // we can always add the packet class here as long as the listener got added
        this.classSet.add(packetType.getPacketClass());

        return true;
    }

    /**
     * Removes a listener for a specific packet type.
     *
     * @param packetType the packet type
     * @param listener   the listener to remove
     * @return {@code true} if the set contained the specified listener;
     *         {@code false} otherwise
     * @throws NullPointerException if the packetType or listener is null
     */
    public synchronized boolean remove(PacketType packetType, PacketListener listener) {
        Objects.requireNonNull(packetType, "packetType cannot be null");
        Objects.requireNonNull(listener, "listener cannot be null");

        Set<PacketListener> listenerSet = this.typeMap.get(packetType);
        if (listenerSet == null) {
            // this should never happen so better check for it during unit tests
            assert !classSet.contains(packetType.getPacketClass());
            return false;
        }

        if (!listenerSet.remove(listener)) {
            // return since the listenerSet didn't change
            return false;
        }

        // packet type has no listeners remove type
        if (listenerSet.isEmpty()) {
            this.typeMap.remove(packetType);
            this.classSet.remove(packetType.getPacketClass());
        }

        return true;
    }

    /**
     * Checks if there are any listeners for a specific packet type.
     *
     * @param packetType the packet type
     * @return true if there are listeners for the packet type, false otherwise
     */
    public boolean contains(PacketType packetType) {
        return this.typeMap.containsKey(packetType);
    }

    /**
     * Checks if there are any listeners for a specific packet class.
     *
     * @param packetClass the packet class
     * @return true if there are listeners for the packet class, false otherwise
     */
    public boolean contains(Class<?> packetClass) {
        return this.classSet.contains(packetClass);
    }

    /**
     * Gets all the packet types that have listeners.
     *
     * @return a set of packet types that have listeners
     */
    public ImmutableSet<PacketType> values() {
        return ImmutableSet.copyOf(this.typeMap.keySet());
    }

    /**
     * Clears all listeners and their associated packet types.
     */
    public void clear() {
        this.typeMap.clear();
        this.classSet.clear();
    }
}
