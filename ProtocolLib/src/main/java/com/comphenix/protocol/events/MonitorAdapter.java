package com.comphenix.protocol.events;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Represents a listener that is notified of every sent and recieved packet.
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
	
	private void initialize(Plugin plugin, ConnectionSide side, Logger logger) {
		this.plugin = plugin;

		// Recover in case something goes wrong
		try {
			if (side.isForServer())
				this.sending = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Server.getSupported());
			if (side.isForClient())
				this.receiving = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Client.getSupported());
		} catch (FieldAccessException e) {
			if (side.isForServer())
				this.sending = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Server.getRegistry().values());
			if (side.isForClient())
				this.receiving = new ListeningWhitelist(ListenerPriority.MONITOR, Packets.Client.getRegistry().values());
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
	public void onPacketReceiving(PacketEvent event) {
		// Empty for now
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		// Empty for now
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

