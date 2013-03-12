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
import com.google.common.collect.ImmutableSet;

/**
 * Represents an API for accessing the Minecraft protocol.
 * @author Kristian
 */
public interface ProtocolManager extends PacketStream {

	/**
	 * Send a packet to the given player.
	 * <p>
	 * Re-sending a previously cancelled packet is discuraged. Use {@link AsyncMarker#incrementProcessingDelay()} 
	 * to delay a packet until a certain condition has been met.
	 * 
	 * @param reciever - the reciever.
	 * @param packet - packet to send.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	@Override
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters) 
			throws InvocationTargetException;
	
	/**
	 * Simulate recieving a certain packet from a given player.
	 * <p>
	 * Receiving a previously cancelled packet is discuraged. Use {@link AsyncMarker#incrementProcessingDelay()} 
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
	 * @param id - packet ID.
	 * @return New encapsulated Minecraft packet.
	 */
	public PacketContainer createPacket(int id);
	
	/**
	 * Constructs a new encapsulated Minecraft packet with the given ID.
	 * <p>
	 * If set to true, the <i>forceDefaults</i> option will force the system to automatically 
	 * give non-primitive fields in the packet sensible default values. For instance, certain
	 * packets - like Packet60Explosion - require a List or Set to be non-null. If the
	 * forceDefaults option is true, the List or Set will be automatically created.
	 * 
	 * @param id - packet ID.
	 * @param forceDefaults - TRUE to use sensible defaults in most fields, FALSE otherwise.
	 * @return New encapsulated Minecraft packet.
	 */
	public PacketContainer createPacket(int id, boolean forceDefaults);

	/**
	 * Construct a packet using the special builtin Minecraft constructors.
	 * @param id - the packet ID.
	 * @param arguments - arguments that will be passed to the constructor.
	 * @return The packet constructor.
	 */
	public PacketConstructor createPacketConstructor(int id, Object... arguments);
	
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
	 * @return Every filtered server packet.
	 */
	public Set<Integer> getSendingFilters();
	
	/**
	 * Retrieves a immutable set containing the ID of the recieved client packets that will be observed by listeners.
	 * @return Every filtered client packet.
	 */
	public Set<Integer> getReceivingFilters();
	
	/**
	 * Determines whether or not this protocol mananger has been disabled. 
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isClosed();

	/**
	 * Retrieve the current asyncronous packet manager.
	 * @return Asyncronous packet manager.
	 */
	public AsynchronousManager getAsynchronousManager();
}