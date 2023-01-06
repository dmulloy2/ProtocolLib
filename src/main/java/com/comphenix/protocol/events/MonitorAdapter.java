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

import java.util.Collection;
import java.util.logging.Logger;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import org.bukkit.plugin.Plugin;

/**
 * Represents a listener that is notified of every sent and received packet.
 *
 * @author Kristian
 */
public abstract class MonitorAdapter implements PacketListener {

	private final Plugin plugin;
	private final ListeningWhitelist sending;
	private final ListeningWhitelist receiving;

	public MonitorAdapter(Plugin plugin, ConnectionSide side) {
		this.plugin = plugin;

		// check the connection side and register the packets for the given side
		this.sending = side.isForServer() ? buildWhitelist(PacketRegistry.getServerPacketTypes()) : ListeningWhitelist.EMPTY_WHITELIST;
		this.receiving = side.isForClient() ? buildWhitelist(PacketRegistry.getClientPacketTypes()) : ListeningWhitelist.EMPTY_WHITELIST;
	}

	@Deprecated
	public MonitorAdapter(Plugin plugin, ConnectionSide side, Logger logger) {
		this(plugin, side);
	}

	private static ListeningWhitelist buildWhitelist(Collection<PacketType> packetTypes) {
		return ListeningWhitelist.newBuilder().monitor().gamePhaseBoth().types(packetTypes).build();
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return this.sending;
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return this.receiving;
	}

	@Override
	public Plugin getPlugin() {
		return this.plugin;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
	}
}
