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
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
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
	/**
	 * The minimum version ProtocolLib has been tested with.
	 */
	private static final String MINIMUM_MINECRAFT_VERSION = "1.0.0";
	
	/**
	 * The maximum version ProtocolLib has been tested with,
	 */
	private static final String MAXIMUM_MINECRAFT_VERSION = "1.4.5";
	
	/**
	 * The number of milliseconds per second.
	 */
	static final long MILLI_PER_SECOND = 1000;
	
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
	private boolean updateDisabled;
	
	// Logger
	private Logger logger;
	private Handler redirectHandler;
	
	// Commands
	private CommandProtocol commandProtocol;
	private CommandPacket commandPacket;
	
	@Override
	public void onLoad() {
		// Load configuration
		logger = getLoggerSafely();
		
		// Add global parameters
		DetailedErrorReporter detailedReporter = new DetailedErrorReporter(this);
		updater = new Updater(this, logger, "protocollib", getFile(), "protocol.info");
		reporter = detailedReporter;
		
		try {
			config = new ProtocolConfig(this);
		} catch (Exception e) {
			detailedReporter.reportWarning(this, "Cannot load configuration", e);

			// Load it again
			if (deleteConfig()) {
				config = new ProtocolConfig(this);
			} else {
				reporter.reportWarning(this, "Cannot delete old ProtocolLib configuration.");
			}
		}
		
		try {
			unhookTask = new DelayedSingleTask(this);
			protocolManager = new PacketFilterManager(getClassLoader(), getServer(), unhookTask, detailedReporter);
			detailedReporter.addGlobalParameter("manager", protocolManager);
			
			// Initialize command handlers
			commandProtocol = new CommandProtocol(detailedReporter, this, updater, config);
			commandPacket = new CommandPacket(detailedReporter, this, logger, protocolManager);
			
			// Send logging information to player listeners too
			broadcastUsers(PERMISSION_INFO);
			
		} catch (Throwable e) {
			detailedReporter.reportDetailed(this, "Cannot load ProtocolLib.", e, protocolManager);
			disablePlugin();
		}
	}
	
	private boolean deleteConfig() {
		return config.getFile().delete();
	}
	
	@Override
	public void reloadConfig() {
		super.reloadConfig();
		
		// Reload configuration
		if (config != null) {
			config.reloadConfig();
		}
	}
	
    private void broadcastUsers(final String permission) {
    	// Guard against multiple calls
    	if (redirectHandler != null)
    		return;
    	
    	// Broadcast information to every user too
    	redirectHandler = new Handler() {
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
		};

        logger.addHandler(redirectHandler);
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
			if (backgroundCompiler == null && config.isBackgroundCompilerEnabled()) {
				backgroundCompiler = new BackgroundCompiler(getClassLoader(), reporter);
				BackgroundCompiler.setInstance(backgroundCompiler);
				
				logger.info("Started structure compiler thread.");
			} else {
				logger.info("Structure compiler thread has been disabled.");
			}
			
			// Handle unexpected Minecraft versions
			verifyMinecraftVersion();
			
			// Set up command handlers
			registerCommand(CommandProtocol.NAME, commandProtocol);
			registerCommand(CommandPacket.NAME, commandPacket);
	
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

	// Used to check Minecraft version
	private void verifyMinecraftVersion() {
		try {
			MinecraftVersion minimum = new MinecraftVersion(MINIMUM_MINECRAFT_VERSION);
			MinecraftVersion maximum = new MinecraftVersion(MAXIMUM_MINECRAFT_VERSION);
			MinecraftVersion current = new MinecraftVersion(getServer());

			// Skip certain versions
			if (!config.getIgnoreVersionCheck().equals(current.getVersion())) {
				// We'll just warn the user for now
				if (current.compareTo(minimum) < 0)
					logger.warning("Version " + current + " is lower than the minimum " + minimum);
				if (current.compareTo(maximum) > 0)
					logger.warning("Version " + current + " has not yet been tested! Proceed with caution.");
	 		}
		} catch (Exception e) {
			reporter.reportWarning(this, "Unable to retrieve current Minecraft version.", e);
		}
	}
		
	private void registerCommand(String name, CommandExecutor executor) {
		try {
			if (executor == null) 
				throw new RuntimeException("Executor was NULL.");
			
			PluginCommand command = getCommand(name);
			
			// Try to load the command
			if (command != null)
				command.setExecutor(executor);
			else
				throw new RuntimeException("plugin.yml might be corrupt.");
		
		} catch (RuntimeException e) {
			reporter.reportWarning(this, "Cannot register command " + name + ": " + e.getMessage());
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
					if (!updateDisabled) {
						checkUpdates();
					}
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
		
		try {
			long updateTime = config.getAutoLastTime() + config.getAutoDelay();

			// Should we update?
			if (currentTime > updateTime) {		
				// Initiate the update as if it came from the console
				if (config.isAutoDownload())
					commandProtocol.updateVersion(getServer().getConsoleSender());
				else if (config.isAutoNotify())
					commandProtocol.checkVersion(getServer().getConsoleSender());
				else 
					commandProtocol.updateFinished();
			}
		} catch (Exception e) {
			reporter.reportDetailed(this, "Cannot perform automatic updates.", e);
			updateDisabled = true;
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
		
		// And redirect handler too
		if (redirectHandler != null) {
			logger.removeHandler(redirectHandler);
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
