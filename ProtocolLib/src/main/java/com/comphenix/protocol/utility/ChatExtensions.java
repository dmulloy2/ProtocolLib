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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMatchers;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

/**
 * Utility methods for sending chat messages.
 * 
 * @author Kristian
 */
public class ChatExtensions {
	// Used to sent chat messages
	private PacketConstructor chatConstructor;
	private ProtocolManager manager;
	
	// Whether or not we have to use the post-1.6.1 chat format
	private static Constructor<?> jsonConstructor = getJsonFormatConstructor();
	private static Method messageFactory;
	
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
	 * @throws InvocationTargetException If we were unable to send the message.
	 */
	private void sendMessageSilently(Player player, String message) throws InvocationTargetException {
		if (jsonConstructor != null) {
			sendMessageAsJson(player, message);
		} else {
			sendMessageAsString(player, message);
		}
	}

	/**
	 * Send a message using the new JSON format in 1.6.1.
	 * @param player - the player to send it to.
	 * @param message - the message to send.
	 * @throws InvocationTargetException InvocationTargetException If we were unable to send the message.
	 */
	private void sendMessageAsJson(Player player, String message) throws InvocationTargetException {
		Object messageObject = null;
		
		if (chatConstructor == null) {
			Class<?> messageClass = jsonConstructor.getParameterTypes()[0];
			chatConstructor = manager.createPacketConstructor(Packets.Server.CHAT, messageClass);
			
			// Try one of the string constructors
			messageFactory = FuzzyReflection.fromClass(messageClass).getMethod(
					FuzzyMethodContract.newBuilder().
					requireModifier(Modifier.STATIC).
					parameterCount(1).
					parameterExactType(String.class).
					returnTypeMatches(FuzzyMatchers.matchParent()).
					build());
		}
		
		try {
			 messageObject = messageFactory.invoke(null, message);
		} catch (Exception e) {
			throw new InvocationTargetException(e);
		}
		
		try {
			manager.sendServerPacket(player, chatConstructor.createPacket(messageObject), false);
		} catch (FieldAccessException e) {
			throw new InvocationTargetException(e);
		}
	}
	
	/**
	 * Send a message as a pure string.
	 * @param player - the player.
	 * @param message - the message to send.
	 * @throws InvocationTargetException If anything went wrong.
	 */
	private void sendMessageAsString(Player player, String message) throws InvocationTargetException {
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
	
	/**
	 * Print a flower box around a given message.
	 * @param message - the message to print.
	 * @param marginChar - the character to use as margin.
	 * @param marginWidth - the width (in characters) of the left and right margin.
	 * @param marginHeight - the height (in characters) of the top and buttom margin.
	 */
	public static String[] toFlowerBox(String[] message, String marginChar, int marginWidth, int marginHeight) {
		String[] output = new String[message.length + marginHeight * 2];
		int width = getMaximumLength(message);
		
		// Margins
		String topButtomMargin = Strings.repeat(marginChar, width + marginWidth * 2);
		String leftRightMargin = Strings.repeat(marginChar, marginWidth);
		
		// Add left and right margin
		for (int i = 0; i < message.length; i++) {
			output[i + marginHeight] = leftRightMargin + Strings.padEnd(message[i], width, ' ') + leftRightMargin;
		}
		
		// Insert top and bottom margin
		for (int i = 0; i < marginHeight; i++) {
			output[i] = topButtomMargin;
			output[output.length - i - 1] = topButtomMargin;
		}
		return output;
	}
	
	/**
	 * Retrieve the longest line lenght in a list of strings.
	 * @param lines - the lines.
	 * @return Longest line lenght.
	 */
	private static int getMaximumLength(String[] lines) {
		int current = 0;
		
		// Find the longest line
		for (int i = 0; i < lines.length; i++) {
			if (current < lines[i].length())
				current = lines[i].length();
		}
		return current;
	}
	
	/**
	 * Retrieve a constructor for post-1.6.1 chat packets.
	 * @return A constructor for JSON-based packets.
	 */
	static Constructor<?> getJsonFormatConstructor() {
		Class<?> chatPacket = PacketRegistry.getPacketClassFromID(3, true);
		List<Constructor<?>> list = FuzzyReflection.fromClass(chatPacket).getConstructorList(
			FuzzyMethodContract.newBuilder().
				parameterCount(1).
				parameterMatches(MinecraftReflection.getMinecraftObjectMatcher()).
				build()
		);
		
		// First element or NULL
		return Iterables.getFirst(list, null);
	}
}
