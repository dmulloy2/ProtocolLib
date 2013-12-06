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

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;

/**
 * Represents a object capable of sending or receiving packets.
 * 
 * @author Kristian
 */
public interface PacketStream {
	/**
	 * Send a packet to the given player.
	 * @param receiver - the reciever.
	 * @param packet - packet to send.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	public void sendServerPacket(Player receiver, PacketContainer packet) 
			throws InvocationTargetException;

	/**
	 * Send a packet to the given player.
	 * @param receiver - the reciever.
	 * @param packet - packet to send.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	public void sendServerPacket(Player receiver, PacketContainer packet, boolean filters)
			throws InvocationTargetException;
	
	/**
	 * Send a packet to the given player.
	 * @param receiver - the receiver.
	 * @param packet - packet to send.
	 * @param marker - the network marker to use.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	public void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters)
			throws InvocationTargetException;


	/**
	 * Simulate recieving a certain packet from a given player.
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	public void recieveClientPacket(Player sender, PacketContainer packet) 
			throws IllegalAccessException, InvocationTargetException;

	/**
	 * Simulate recieving a certain packet from a given player.
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters)
			throws IllegalAccessException, InvocationTargetException;
	
	/**
	 * Simulate recieving a certain packet from a given player.
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @param marker - the network marker to use.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	public void recieveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters)
			throws IllegalAccessException, InvocationTargetException;
}
