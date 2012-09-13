package com.comphenix.protocol;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.standard.Severity;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.metrics.MetricsLite;

public class ProtocolLibrary extends JavaPlugin {
	
	// There should only be one protocol manager, so we'll make it static
	private static PacketFilterManager protocolManager;
	
	// Error logger
	private Logger logger;

	// Metrics
	private MetricsLite metrics;
	
	@Override
	public void onLoad() {
		logger = getLoggerSafely();
		protocolManager = new PacketFilterManager(getClassLoader(), logger);
		
		try {
			metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to enable metrics.", e);
		}
	}
	
	@Override
	public void onEnable() {
		Server server = getServer();
		PluginManager manager = server.getPluginManager();
		
		// Player login and logout events
		protocolManager.registerEvents(manager, this);
		protocolManager.initializePlayers(server.getOnlinePlayers());
	}
		
	@Override
	public void onDisable() {
		protocolManager.close();
		protocolManager = null;
	}

	/**
	 * Retrieves the packet protocol manager.
	 * @return Packet protocol manager, or NULL if it has been disabled.
	 */
	public static ProtocolManager getProtocolManager() {
		return protocolManager;
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
