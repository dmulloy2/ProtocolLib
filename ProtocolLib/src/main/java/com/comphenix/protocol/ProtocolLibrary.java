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

import java.io.File;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.DetailedErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.DelayedSingleTask;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.metrics.Statistics;
import com.comphenix.protocol.metrics.Updater;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;

/**
 * The main entry point for ProtocolLib.
 * 
 * @author Kristian
 */
public class ProtocolLibrary extends JavaPlugin {
	
	private static final long MILLI_PER_SECOND = 1000;
	private static final String PERMISSION_INFO = "protocol.info";
	
	// There should only be one protocol manager, so we'll make it static
	private static PacketFilterManager protocolManager;
	
	// Error reporter
	private ErrorReporter reporter;
	
	// Metrics and statistisc
	private Statistics statistisc;
	
	// Structure compiler
	private BackgroundCompiler backgroundCompiler;
	
	// Used to clean up server packets that have expired. 
	// But mostly required to simulate recieving client packets.
	private int asyncPacketTask = -1;
	private int tickCounter = 0;
	private static final int ASYNC_PACKET_DELAY = 1;
	
	// Used to unhook players after a delay
	private DelayedSingleTask unhookTask;
	
	// Settings/options
	private ProtocolConfig config;
	
	// Updater
	private Updater updater;
	
	// Logger
	private Logger logger;
	
	// Commands
	private CommandProtocol commandProtocol;
	private CommandPacket commandPacket;
	
	@Override
	public void onLoad() {
		// Load configuration
		logger = getLoggerSafely();
		
		// Add global parameters
		DetailedErrorReporter reporter = new DetailedErrorReporter();
		updater = new Updater(this, logger, "protocollib", getFile(), "protocol.info");
		
		try {
			config = new ProtocolConfig(this);
		} catch (Exception e) {
			reporter.reportWarning(this, "Cannot load configuration", e);
			
			// Load it again
			deleteConfig();
			config = new ProtocolConfig(this);
		}
		
		try {
			unhookTask = new DelayedSingleTask(this);
			protocolManager = new PacketFilterManager(getClassLoader(), getServer(), unhookTask, reporter);
			reporter.addGlobalParameter("manager", protocolManager);
			
			// Initialize command handlers
			commandProtocol = new CommandProtocol(this, updater);
			commandPacket = new CommandPacket(this, logger, reporter, protocolManager);
			
			// Send logging information to player listeners too
			broadcastUsers(PERMISSION_INFO);
			
		} catch (Throwable e) {
			reporter.reportDetailed(this, "Cannot load ProtocolLib.", e, protocolManager);
			disablePlugin();
		}
	}
	
	private void deleteConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
		
		// Delete the file
		configFile.delete();
	}
	
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		// Reload configuration
		config = new ProtocolConfig(this);
	}
	
    private void broadcastUsers(final String permission) {
        // Broadcast information to every user too
        logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				commandPacket.broadcastMessageSilently(record.getMessage(), permission);
			}
			
			@Override
			public void flush() {
				// Not needed.
			}
			
			@Override
			public void close() throws SecurityException {
				// Do nothing.
			}
		});
    }
	
	@Override
	public void onEnable() {
		try {
			Server server = getServer();
			PluginManager manager = server.getPluginManager();
			
			// Don't do anything else!
			if (manager == null)
				return;
			
			// Initialize background compiler
			if (backgroundCompiler == null) {
				backgroundCompiler = new BackgroundCompiler(getClassLoader());
				BackgroundCompiler.setInstance(backgroundCompiler);
			}
			
			// Set up command handlers
			getCommand(CommandProtocol.NAME).setExecutor(commandProtocol);
			getCommand(CommandPacket.NAME).setExecutor(commandPacket);
	
			// Notify server managers of incompatible plugins
			checkForIncompatibility(manager);
			
			// Player login and logout events
			protocolManager.registerEvents(manager, this);
				
			// Worker that ensures that async packets are eventually sent
			// It also performs the update check.
			createAsyncTask(server);
		
		} catch (Throwable e) {
			reporter.reportDetailed(this, "Cannot enable ProtocolLib.", e);
			disablePlugin();
			return;
		}
		
		// Try to enable statistics
		try {
			if (config.isMetricsEnabled()) {
				statistisc = new Statistics(this);
			}
		} catch (IOException e) {
			reporter.reportDetailed(this, "Unable to enable metrics.", e, statistisc);
		} catch (Throwable e) {
			reporter.reportDetailed(this, "Metrics cannot be enabled. Incompatible Bukkit version.", e, statistisc);
		}
	}
	
	/**
	 * Disable the current plugin.
	 */
	private void disablePlugin() {
		getServer().getPluginManager().disablePlugin(this);
	}
	
	private void createAsyncTask(Server server) {
		try {
			if (asyncPacketTask >= 0)
				throw new IllegalStateException("Async task has already been created");
			
			// Attempt to create task
			asyncPacketTask = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					AsyncFilterManager manager = (AsyncFilterManager) protocolManager.getAsynchronousManager();
					
					// We KNOW we're on the main thread at the moment
					manager.sendProcessedPackets(tickCounter++, true);
					
					// Check for updates too
					checkUpdates();
				}
			}, ASYNC_PACKET_DELAY, ASYNC_PACKET_DELAY);
		
		} catch (Throwable e) {
			if (asyncPacketTask == -1) {
				reporter.reportDetailed(this, "Unable to create packet timeout task.", e);
			}
		}
	}
	
	private void checkUpdates() {
		// Ignore milliseconds - it's pointless
		long currentTime = System.currentTimeMillis() / MILLI_PER_SECOND;
		
		// Should we update?
		if (currentTime < config.getAutoLastTime() + config.getAutoDelay()) {
			// Great. Save this check.
			config.setAutoLastTime(currentTime);
			config.saveAll();
			
			// Initiate the update from the console
			if (config.isAutoDownload())
				commandProtocol.updateVersion(getServer().getConsoleSender());
			else if (config.isAutoNotify())
				commandProtocol.checkVersion(getServer().getConsoleSender());
		}
	}
	
	private void checkForIncompatibility(PluginManager manager) {
		// Plugin authors: Notify me to remove these
		String[] incompatiblePlugins = {};
		
		for (String plugin : incompatiblePlugins) {
			if (manager.getPlugin(plugin) != null) {
				// Check for versions, ect.
				reporter.reportWarning(this, "Detected incompatible plugin: " + plugin);
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
		
		unhookTask.close();
		protocolManager.close();
		protocolManager = null;
		statistisc = null;
		
		// Leaky ClassLoader begone!
		CleanupStaticMembers cleanup = new CleanupStaticMembers(getClassLoader(), reporter);
		cleanup.resetAll();
	}
	
	// Get the Bukkit logger first, before we try to create our own
	private Logger getLoggerSafely() {
		Logger log = null;

		try {
			log = getLogger();
		} catch (Throwable e) { }

		// Use the default logger instead
		if (log == null)
			log = Logger.getLogger("Minecraft");
		return log;
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
	 * Note that this method may return NULL when the server is reloading or shutting down. It is also
	 * NULL if metrics has been disabled.
	 * @return Metrics instance container.
	 */
	public Statistics getStatistics() {
		return statistisc;
	}
}
