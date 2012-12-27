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

package com.comphenix.protocol.utility;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Utility methods for sending chat messages.
 * 
 * @author Kristian
 */
public class ChatExtensions {
	
	// Used to sent chat messages
	private PacketConstructor chatConstructor;
	private ProtocolManager manager;
	
	public ChatExtensions(ProtocolManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Send a message without invoking the packet listeners.
	 * @param receiver - the receiver.
	 * @param message - the message to send.
	 * @throws InvocationTargetException If we were unable to send the message.
	 */
	public void sendMessageSilently(CommandSender receiver, String message) throws InvocationTargetException { 
		if (receiver == null)
			throw new IllegalArgumentException("receiver cannot be NULL.");
		if (message == null)
			throw new IllegalArgumentException("message cannot be NULL.");
		
		// Handle the player case by manually sending packets
		if (receiver instanceof Player) {
			sendMessageSilently((Player) receiver, message);
		} else {
			receiver.sendMessage(message);
		}
	}
	
	/**
	 * Send a message without invoking the packet listeners.
	 * @param player - the player to send it to.
	 * @param message - the message to send.
	 * @return TRUE if the message was sent successfully, FALSE otherwise.
	 * @throws InvocationTargetException If we were unable to send the message.
	 */
	private void sendMessageSilently(Player player, String message) throws InvocationTargetException {
		if (chatConstructor == null)
			chatConstructor = manager.createPacketConstructor(Packets.Server.CHAT, message);
		
		try {
			manager.sendServerPacket(player, chatConstructor.createPacket(message), false);
		} catch (FieldAccessException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	/**
	 * Broadcast a message without invoking any packet listeners.
	 * @param message - message to send.
	 * @param permission - permission required to receieve the message. NULL to target everyone.
	 * @throws InvocationTargetException If we were unable to send the message.
	 */
	public void broadcastMessageSilently(String message, String permission) throws InvocationTargetException {
		if (message == null)
			throw new IllegalArgumentException("message cannot be NULL.");
		
		// Send this message to every online player
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			if (permission == null || player.hasPermission(permission)) {
				sendMessageSilently(player, message);
			}
		}
	}
}
