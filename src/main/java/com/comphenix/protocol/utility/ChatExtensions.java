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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Utility methods for sending chat messages.
 *
 * @author Kristian
 */
public final class ChatExtensions {

	private static final UUID SERVER_UUID = new UUID(0L, 0L);

	// Used to sent chat messages
	private final ProtocolManager manager;

	public ChatExtensions(ProtocolManager manager) {
		this.manager = manager;
	}

	/**
	 * Construct chat packet to send in order to display a given message.
	 *
	 * @param message - the message to send.
	 * @return The packets.
	 */
	public static List<PacketContainer> createChatPackets(String message) {
		List<PacketContainer> packets = new ArrayList<>();
		WrappedChatComponent[] components = WrappedChatComponent.fromChatMessage(message);
		for (WrappedChatComponent component : components) {
			PacketContainer packet;
			if (MinecraftVersion.WILD_UPDATE.atOrAbove()) {
				// since 1.19 system chat is extracted into a separate packet
				packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);

				packet.getStrings().write(0, component.getJson());
				packet.getBooleans().write(0, false);
			} else {
				packet = new PacketContainer(PacketType.Play.Server.CHAT);
				packet.getChatComponents().write(0, component);

				// 1.12+
				packet.getChatTypes().writeSafely(0, EnumWrappers.ChatType.SYSTEM);
				// 1.8-1.12
				packet.getBytes().writeSafely(0, (byte) 1);
				// 1.16+
				packet.getUUIDs().writeSafely(0, SERVER_UUID);
			}

			packets.add(packet);
		}

		return packets;
	}

	/**
	 * Print a flower box around a given message.
	 *
	 * @param message      - the message to print.
	 * @param marginChar   - the character to use as margin.
	 * @param marginWidth  - the width (in characters) of the left and right margin.
	 * @param marginHeight - the height (in characters) of the top and buttom margin.
	 * @return Flowerboxed message
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
	 *
	 * @param lines - the lines.
	 * @return Longest line lenght.
	 */
	private static int getMaximumLength(String[] lines) {
		int current = 0;

		// Find the longest line
		for (String line : lines) {
			if (current < line.length()) {
				current = line.length();
			}
		}

		return current;
	}

	/**
	 * Send a message without invoking the packet listeners.
	 *
	 * @param receiver - the receiver.
	 * @param message  - the message to send.
	 */
	public void sendMessageSilently(CommandSender receiver, String message) {
		if (receiver == null) {
			throw new IllegalArgumentException("receiver cannot be NULL.");
		}
		if (message == null) {
			throw new IllegalArgumentException("message cannot be NULL.");
		}

		// Handle the player case by manually sending packets
		if (receiver instanceof Player) {
			this.sendMessageSilently((Player) receiver, message);
		} else {
			receiver.sendMessage(message);
		}
	}

	/**
	 * Send a message without invoking the packet listeners.
	 *
	 * @param player  - the player to send it to.
	 * @param message - the message to send.
	 */
	private void sendMessageSilently(Player player, String message) {
		for (PacketContainer packet : createChatPackets(message)) {
			this.manager.sendServerPacket(player, packet, false);
		}
	}

	/**
	 * Broadcast a message without invoking any packet listeners.
	 *
	 * @param message    - message to send.
	 * @param permission - permission required to receieve the message. NULL to target everyone.
	 */
	public void broadcastMessageSilently(String message, String permission) {
		if (message == null) {
			throw new IllegalArgumentException("message cannot be NULL.");
		}

		// Send this message to every online player
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (permission == null || player.hasPermission(permission)) {
				this.sendMessageSilently(player, message);
			}
		}
	}
}
