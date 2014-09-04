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

package com.comphenix.protocol;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.async.AsyncMarker;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.ImmutableSet;

/**
 * Represents an API for accessing the Minecraft protocol.
 * @author Kristian
 */
public interface ProtocolManager extends PacketStream {
	/**
	 * Retrieve the protocol version of a given player.
	 * <p>
	 * This only really makes sense of a server that support clients of multiple Minecraft versions, such as Spigot #1628.
	 * @param player - the player.
	 * @return The associated protocol version, or {@link Integer#MIN_VALUE} if unknown.
	 */
	public int getProtocolVersion(Player player);
	
	/**
	 * Send a packet to the given player.
	 * <p>
	 * Re-sending a previously cancelled packet is discouraged. Use {@link AsyncMarker#incrementProcessingDelay()} 
	 * to delay a packet until a certain condition has been met.
	 * 
	 * @param receiver - the receiver.
	 * @param packet - packet to send.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException - if an error occurred when sending the packet.
	 */
	@Override
	public void sendServerPacket(Player receiver, PacketContainer packet, boolean filters) 
			throws InvocationTargetException;
	
	/**
	 * Simulate receiving a certain packet from a given player.
	 * <p>
	 * Receiving a previously cancelled packet is discouraged. Use {@link AsyncMarker#incrementProcessingDelay()} 
	 * to delay a packet until a certain condition has been met.
	 * 
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	@Override
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters) 
			throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * Broadcast a given packet to every connected player on the server.
	 * @param packet - the packet to broadcast.
	 * @throws FieldAccessException If we were unable to send the packet due to reflection problems.
	 */
	public void broadcastServerPacket(PacketContainer packet);
	
	/**
	 * Broadcast a packet to every player that is receiving information about a given entity. 
	 * <p>
	 * This is usually every player in the same world within an observable distance. If the entity is a 
	 * player, it will only be included if <i>includeTracker</i> is TRUE.
	 * @param packet - the packet to broadcast.
	 * @param entity - the entity whose trackers we will inform.
	 * @param includeTracker - whether or not to also transmit the packet to the entity, if it is a tracker.
	 * @throws FieldAccessException If we were unable to send the packet due to reflection problems.
	 */
	public void broadcastServerPacket(PacketContainer packet, Entity entity, boolean includeTracker);
	
	/**
	 * Broadcast a packet to every player within the given maximum observer distance.
	 * @param packet - the packet to broadcast.
	 * @param origin - the origin to consider when calculating the distance to each observer.
	 * @param maxObserverDistance - the maximum distance to the origin.
	 */
	public void broadcastServerPacket(PacketContainer packet, Location origin, int maxObserverDistance);
	
	/**
	 * Retrieves a list of every registered packet listener.
	 * @return Every registered packet listener.
	 */
	public ImmutableSet<PacketListener> getPacketListeners();

	/**
	 * Adds a packet listener. 
	 * <p>
	 * Adding an already registered listener has no effect. If you need to change the packets 
	 * the current listener is observing, you must first remove the packet listener before you 
	 * can register it again.
	 * @param listener - new packet listener.
	 */
	public void addPacketListener(PacketListener listener);

	/**
	 * Removes a given packet listener. 
	 * <p>
	 * Attempting to remove a listener that doesn't exist has no effect.
	 * @param listener - the packet listener to remove.
	 */
	public void removePacketListener(PacketListener listener);

	/**
	 * Removes every listener associated with the given plugin.
	 * @param plugin - the plugin to unload.
	 */
	public void removePacketListeners(Plugin plugin);

	/**
	 * Constructs a new encapsulated Minecraft packet with the given ID.
	 * <p>
	 * Deprecated: Use {@link #createPacket(PacketType)} instead.
	 * @param id - packet ID.
	 * @return New encapsulated Minecraft packet.
	 */
	@Deprecated
	public PacketContainer createPacket(int id);
	
	/**
	 * Constructs a new encapsulated Minecraft packet with the given ID.
	 * @param  type - packet  type.
	 * @return New encapsulated Minecraft packet.
	 */
	public PacketContainer createPacket(PacketType type);
	
	/**
	 * Constructs a new encapsulated Minecraft packet with the given ID.
	 * <p>
	 * If set to true, the <i>forceDefaults</i> option will force the system to automatically 
	 * give non-primitive fields in the packet sensible default values. For instance, certain
	 * packets - like Packet60Explosion - require a List or Set to be non-null. If the
	 * forceDefaults option is true, the List or Set will be automatically created.
	 * <p>
	 * Deprecated: Use {@link #createPacket(PacketType, boolean)} instead.
	 * 
	 * @param id - packet ID.
	 * @param forceDefaults - TRUE to use sensible defaults in most fields, FALSE otherwise.
	 * @return New encapsulated Minecraft packet.
	 */
	@Deprecated
	public PacketContainer createPacket(int id, boolean forceDefaults);
	
	/**
	 * Constructs a new encapsulated Minecraft packet with the given ID.
	 * <p>
	 * If set to true, the <i>forceDefaults</i> option will force the system to automatically 
	 * give non-primitive fields in the packet sensible default values. For instance, certain
	 * packets - like Packet60Explosion - require a List or Set to be non-null. If the
	 * forceDefaults option is true, the List or Set will be automatically created.
	 * 
	 * @param type - packet type.
	 * @param forceDefaults - TRUE to use sensible defaults in most fields, FALSE otherwise.
	 * @return New encapsulated Minecraft packet.
	 */
	public PacketContainer createPacket(PacketType type, boolean forceDefaults);

	/**
	 * Construct a packet using the special builtin Minecraft constructors.
	 * <p>
	 * Deprecated: Use {@link #createPacketConstructor(PacketType, Object...)} instead.
	 * @param id - the packet ID.
	 * @param arguments - arguments that will be passed to the constructor.
	 * @return The packet constructor.
	 */
	@Deprecated
	public PacketConstructor createPacketConstructor(int id, Object... arguments);
	
	/**
	 * Construct a packet using the special builtin Minecraft constructors.
	 * @param id - the packet type.
	 * @param arguments - arguments that will be passed to the constructor.
	 * @return The packet constructor.
	 */
	public PacketConstructor createPacketConstructor(PacketType type, Object... arguments);
	
	/**
	 * Completely resend an entity to a list of clients.
	 * <p>
	 * Note that this method is NOT thread safe. If you call this method from anything 
	 * but the main thread, it will throw an exception.
	 * @param entity - entity to refresh.
	 * @param observers - the clients to update.
	 */
	public void updateEntity(Entity entity, List<Player> observers) throws FieldAccessException;
	
	/**
	 * Retrieve the associated entity.
	 * @param container - the world the entity belongs to.
	 * @param id - the unique ID of the entity.
	 * @return The associated entity.
	 * @throws FieldAccessException Reflection failed.
	 */
	public Entity getEntityFromID(World container, int id) throws FieldAccessException;
	
	/**
	 * Retrieve every client that is receiving information about a given entity.
	 * @param entity - the entity that is being tracked.
	 * @return Every client/player that is tracking the given entity.
	 * @throws FieldAccessException If reflection failed.
	 */
	public List<Player> getEntityTrackers(Entity entity) throws FieldAccessException;
	
	/**
	 * Retrieves a immutable set containing the ID of the sent server packets that will be observed by listeners.
	 * <p>
	 * Deprecated: Use {@link #getSendingFilterTypes()} instead.
	 * @return Every filtered server packet.
	 */
	@Deprecated
	public Set<Integer> getSendingFilters();
	
	/**
	 * Retrieves a immutable set containing the type of the sent server packets that will be observed by listeners.
	 * @return Every filtered server packet.
	 */
	public Set<PacketType> getSendingFilterTypes();
	
	/**
	 * Retrieves a immutable set containing the ID of the received client packets that will be observed by listeners.
	 * <p>
	 * Deprecated: Use {@link #getReceivingFilterTypes()} instead.
	 * @return Every filtered client packet.
	 */
	@Deprecated
	public Set<Integer> getReceivingFilters();
	
	/**
	 * Retrieves a immutable set containing the type of the received client packets that will be observed by listeners.
	 * @return Every filtered client packet.
	 */
	public Set<PacketType> getReceivingFilterTypes();
	
	/**
	 * Retrieve the current Minecraft version.
	 * @return The current version.
	 */
	public MinecraftVersion getMinecraftVersion();
	
	/**
	 * Determines whether or not this protocol manager has been disabled. 
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isClosed();

	/**
	 * Retrieve the current asynchronous packet manager.
	 * @return Asynchronous packet manager.
	 */
	public AsynchronousManager getAsynchronousManager();
}