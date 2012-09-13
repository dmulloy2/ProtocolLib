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
		Object[] copy = stack.getEnchantments().keySet().toArray();
		
		for (Object enchantment : copy) {
			stack.removeEnchantment((Enchantment) enchantment);
		}
	}
}
