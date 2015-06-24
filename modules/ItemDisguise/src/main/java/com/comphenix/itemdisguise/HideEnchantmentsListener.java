/*
 *  ItemDisguise - A simple Bukkit plugin that illustrates how to use ProtocolLib.
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

package com.comphenix.itemdisguise;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;

public class HideEnchantmentsListener {

	private final Server server;
	private final Logger logger;

	public HideEnchantmentsListener(Server server, Logger logger) {
		this.server = server;
		this.logger = logger;
	}

	public void addListener(ProtocolManager protocolManager, JavaPlugin plugin) {
		// Hide all enchantments
		protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.SET_SLOT, PacketType.Play.Server.WINDOW_ITEMS,
				PacketType.Play.Server.NAMED_SOUND_EFFECT) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();

				try {
					// Item packets
					if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
						removeEnchantments(packet.getItemModifier().read(0));
					} else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
						ItemStack[] elements = packet.getItemArrayModifier().read(0);

						for (int i = 0; i < elements.length; i++) {
							if (elements[i] != null) {
								removeEnchantments(elements[i]);
							}
						}
					} else if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
						event.setCancelled(true);
					}
				} catch (FieldAccessException e) {
					logger.log(Level.SEVERE, "Couldn't access field.", e);
				}
			}
		});

		// Censor
		protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.CHAT) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketType() == PacketType.Play.Client.CHAT) {
					try {
						String message = event.getPacket().getSpecificModifier(String.class).read(0);
						if (message.contains("shit") || message.contains("fuck")) {
							event.setCancelled(true);
							event.getPlayer().sendMessage("Bad manners!");
						}
					} catch (FieldAccessException e) {
						logger.log(Level.SEVERE, "Couldn't access field.", e);
					}
				}
			}
		});
	}

	public Server getServer() {
		return server;
	}

	public void removeListener(ProtocolManager protocolManager, JavaPlugin plugin) {
		// Just remove every adapter with this plugin
		protocolManager.removePacketListeners(plugin);
	}

	private void removeEnchantments(ItemStack stack) {
		if (stack == null)
			return;

		Enchantment[] copy = stack.getEnchantments().keySet().toArray(new Enchantment[0]);
		for (Enchantment enchantment : copy) {
			stack.removeEnchantment(enchantment);
		}
	}
}
