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

import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.NetworkMarker;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import org.bukkit.entity.Player;

/**
 * Represents a object capable of sending or receiving packets.
 *
 * @author Kristian
 */
public interface PacketStream {

	/**
	 * Send a packet to the given player.
	 *
	 * @param receiver - the reciever.
	 * @param packet   - packet to send.
	 */
	void sendServerPacket(Player receiver, PacketContainer packet);

	/**
	 * Send a packet to the given player.
	 *
	 * @param receiver - the reciever.
	 * @param packet   - packet to send.
	 * @param filters  - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 */
	void sendServerPacket(Player receiver, PacketContainer packet, boolean filters);

	/**
	 * Send a packet to the given player.
	 *
	 * @param receiver - the receiver.
	 * @param packet   - packet to send.
	 * @param marker   - the network marker to use.
	 * @param filters  - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 */
	void sendServerPacket(Player receiver, PacketContainer packet, NetworkMarker marker, boolean filters);

	/**
	 * Send a wire packet to the given player.
	 *
	 * @param receiver - the receiver.
	 * @param id       - packet id.
	 * @param bytes    - packet bytes.
	 */
	void sendWirePacket(Player receiver, int id, byte[] bytes);

	/**
	 * Send a wire packet to the given player.
	 *
	 * @param receiver - the receiver.
	 * @param packet   - packet to send.
	 */
	void sendWirePacket(Player receiver, WirePacket packet);

	/**
	 * Simulate recieving a certain packet from a given player.
	 *
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 */
	void receiveClientPacket(Player sender, PacketContainer packet);

	/**
	 * Simulate recieving a certain packet from a given player.
	 *
	 * @param sender  - the sender.
	 * @param packet  - the packet that was sent.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 */
	void receiveClientPacket(Player sender, PacketContainer packet, boolean filters);

	/**
	 * Simulate recieving a certain packet from a given player.
	 *
	 * @param sender  - the sender.
	 * @param packet  - the packet that was sent.
	 * @param marker  - the network marker to use.
	 * @param filters - whether or not to invoke any packet filters below {@link ListenerPriority#MONITOR}.
	 */
	void receiveClientPacket(Player sender, PacketContainer packet, NetworkMarker marker, boolean filters);
}
