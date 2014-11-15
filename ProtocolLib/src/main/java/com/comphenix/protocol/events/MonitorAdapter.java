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

package com.comphenix.protocol.events;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.injector.GamePhase;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Represents a listener that is notified of every sent and received packet.
 * 
 * @author Kristian
 */
public abstract class MonitorAdapter implements PacketListener {

	private Plugin plugin;
	private ListeningWhitelist sending = ListeningWhitelist.EMPTY_WHITELIST;
	private ListeningWhitelist receiving = ListeningWhitelist.EMPTY_WHITELIST;

	public MonitorAdapter(Plugin plugin, ConnectionSide side) {
		initialize(plugin, side, getLogger(plugin));
	}
	
	public MonitorAdapter(Plugin plugin, ConnectionSide side, Logger logger) {
		initialize(plugin, side, logger);
	}
	
	@SuppressWarnings("deprecation")
	private void initialize(Plugin plugin, ConnectionSide side, Logger logger) {
		this.plugin = plugin;

		// Recover in case something goes wrong
		try {
			if (side.isForServer())
				this.sending = ListeningWhitelist.newBuilder().monitor().types(PacketRegistry.getServerPacketTypes()).gamePhaseBoth().build();
			if (side.isForClient())
				this.receiving = ListeningWhitelist.newBuilder().monitor().types(PacketRegistry.getClientPacketTypes()).gamePhaseBoth().build();
		} catch (FieldAccessException e) {
			if (side.isForServer())
				this.sending = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Server.getRegistry().values(), GamePhase.BOTH);
			if (side.isForClient())
				this.receiving = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Client.getRegistry().values(), GamePhase.BOTH);
			logger.log(Level.WARNING, "Defaulting to 1.3 packets.", e);
		}
	}
	
	/**
	 * Retrieve a logger, even if we're running in a CraftBukkit version that doesn't support it.
	 * @param plugin - the plugin to retrieve.
	 * @return The logger.
	 */
	private Logger getLogger(Plugin plugin) {
		try {
			return plugin.getLogger();
		} catch (NoSuchMethodError e) {
			return Logger.getLogger("Minecraft");
		}
	}
	
	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return sending;
	}
	
	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return receiving;
	}
	
	@Override
	public Plugin getPlugin() {
		return plugin;
	}
}

