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

package com.comphenix.protocol;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.metrics.Statistics;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;

public class ProtocolLibrary extends JavaPlugin {
	
	// There should only be one protocol manager, so we'll make it static
	private static PacketFilterManager protocolManager;
	
	// Error logger
	private Logger logger;
	
	// Metrics and statistisc
	private Statistics statistisc;
	
	// Structure compiler
	private BackgroundCompiler backgroundCompiler;
	
	// Used to (mostly) clean up packets that have expired
	private int asyncPacketTask = -1;
	
	// Number of ticks between each cleanup. We don't need to do this often,
	// as it's only indeeded to detected timeouts.
	private static final int ASYNC_PACKET_DELAY = 10;
	
	@Override
	public void onLoad() {
		logger = getLoggerSafely();
		protocolManager = new PacketFilterManager(getClassLoader(), logger);
	}
	
	@Override
	public void onEnable() {
		Server server = getServer();
		PluginManager manager = server.getPluginManager();
		
		// Initialize background compiler
		if (backgroundCompiler == null) {
			backgroundCompiler = new BackgroundCompiler(getClassLoader());
			BackgroundCompiler.setInstance(backgroundCompiler);
		}

		// Notify server managers of incompatible plugins
		checkForIncompatibility(manager);
		
		// Player login and logout events
		protocolManager.registerEvents(manager, this);
		
		// Inject our hook into already existing players
		protocolManager.initializePlayers(server.getOnlinePlayers());
		
		// Timeout
		createAsyncTask(server);
		
		// Try to enable statistics
		try {
			statistisc = new Statistics(this);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Unable to enable metrics.", e);
		} catch (Throwable e) {
			logger.log(Level.SEVERE, "Metrics cannot be enabled. Incompatible Bukkit version.", e);
		}
	}
	
	private void createAsyncTask(Server server) {
		try {
			if (asyncPacketTask < 0)
				throw new IllegalStateException("Async task has already been created");
			
			// Attempt to create task
			asyncPacketTask = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					AsyncFilterManager manager = (AsyncFilterManager) protocolManager.getAsynchronousManager();
					manager.sendProcessedPackets();
				}
			}, ASYNC_PACKET_DELAY, ASYNC_PACKET_DELAY);
		
		} catch (Throwable e) {
			if (asyncPacketTask == -1) {
				logger.log(Level.SEVERE, "Unable to create packet timeout task.", e);
			}
		}
	}
	
	private void checkForIncompatibility(PluginManager manager) {
		// Plugin authors: Notify me to remove these
		String[] incompatiblePlugins = {};
		
		for (String plugin : incompatiblePlugins) {
			if (manager.getPlugin(plugin) != null) {
				// Check for versions, ect.
				logger.severe("Detected incompatible plugin: " + plugin);
			}
		}
	}
	
	@Override
	public void onDisable() {
		// Disable compiler
		if (backgroundCompiler != null) {
			backgroundCompiler.shutdownAll();
			backgroundCompiler = null;
			BackgroundCompiler.setInstance(null);
		}
		
		// Clean up
		if (asyncPacketTask >= 0) {
			getServer().getScheduler().cancelTask(asyncPacketTask);
			asyncPacketTask = -1;
		}
		
		protocolManager.close();
		protocolManager = null;
		statistisc = null;
	}
	
	/**
	 * Retrieves the packet protocol manager.
	 * @return Packet protocol manager, or NULL if it has been disabled.
	 */
	public static ProtocolManager getProtocolManager() {
		return protocolManager;
	}
	
	/**
	 * Retrieve the metrics instance used to measure users of this library.
	 * <p>
	 * Note that this method may return NULL when the server is reloading or shutting down.
	 * @return Metrics instance container.
	 */
	public Statistics getStatistics() {
		return statistisc;
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
