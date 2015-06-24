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

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class ItemDisguiseMod extends JavaPlugin {

	private ProtocolManager protocolManager;
	private Logger logger;

	private HideEnchantmentsListener enchantmentsListener;

	@Override
	public void onEnable() {
		logger = getLoggerSafely();
		protocolManager = ProtocolLibrary.getProtocolManager();

		enchantmentsListener = new HideEnchantmentsListener(getServer(), logger);
		enchantmentsListener.addListener(protocolManager, this);
	}

	@Override
	public void onDisable() {
		enchantmentsListener.removeListener(protocolManager, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (label.equalsIgnoreCase("explosion")) {
				PacketContainer fakeExplosion = protocolManager.createPacket(PacketType.Play.Server.EXPLOSION);

				// Set the coordinates
				try {
					fakeExplosion.getSpecificModifier(double.class).write(0, player.getLocation().getX()).write(1, player.getLocation().getY()).write(2, player.getLocation().getZ());
					fakeExplosion.getSpecificModifier(float.class).write(0, 3.0F);

					protocolManager.sendServerPacket(player, fakeExplosion);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return true;
		}

		return false;
	}

	// Get the Bukkit logger first, before we try to create our own
	private Logger getLoggerSafely() {

		Logger log = null;

		try {
			log = getLogger();
		} catch (Throwable e) {
			// We'll handle it
		}

		if (log == null)
			log = Logger.getLogger("Minecraft");
		return log;
	}
}
