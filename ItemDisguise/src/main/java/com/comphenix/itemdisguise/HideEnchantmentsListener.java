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

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
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
		protocolManager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.SERVER_SIDE, 0x67, 0x68, 0x3E) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer packet = event.getPacket();
				
				try {
					// Item packets
					switch (event.getPacketID()) {
					case 0x67: // Set slot
						removeEnchantments(packet.getItemModifier().read(0));
						break;
						
					case 0x68: // Set Window Items
						ItemStack[] elements = packet.getItemArrayModifier().read(0);
	
						for (int i = 0; i < elements.length; i++) {
							if (elements[i] != null) {
								removeEnchantments(elements[i]);
							}
						}
						break;
						
					case 0x3E: // Sound effect
						event.setCancelled(true);
						break;
					}
				
				} catch (FieldAccessException e) {
					logger.log(Level.SEVERE, "Couldn't access field.", e);
				}
			}
		});
		
		// Censor
		protocolManager.addPacketListener(new PacketAdapter(plugin, ConnectionSide.CLIENT_SIDE, 0x3) {
			@Override
			public void onPacketReceiving(PacketEvent event) {
				if (event.getPacketID() == 0x3) {
					try {
						String message = event.getPacket().
											getSpecificModifier(String.class).read(0);
						
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
		
		Object[] copy = stack.getEnchantments().keySet().toArray();
		
		for (Object enchantment : copy) {
			stack.removeEnchantment((Enchantment) enchantment);
		}
	}
}
