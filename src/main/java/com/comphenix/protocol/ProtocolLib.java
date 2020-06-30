/**
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
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.comphenix.protocol.async.AsyncFilterManager;
import com.comphenix.protocol.error.*;
import com.comphenix.protocol.injector.DelayedSingleTask;
import com.comphenix.protocol.injector.InternalManager;
import com.comphenix.protocol.injector.PacketFilterManager;
import com.comphenix.protocol.injector.PlayerInjectHooks;
import com.comphenix.protocol.metrics.Statistics;
import com.comphenix.protocol.reflect.compiler.BackgroundCompiler;
import com.comphenix.protocol.updater.Updater;
import com.comphenix.protocol.updater.Updater.UpdateType;
import com.comphenix.protocol.utility.ChatExtensions;
import com.comphenix.protocol.utility.EnhancerFactory;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main entry point for ProtocolLib.
 *
 * @author Kristian
 */
public class ProtocolLib extends JavaPlugin {
	// Every possible error or warning report type
	public static final ReportType REPORT_CANNOT_LOAD_CONFIG = new ReportType("Cannot load configuration");
	public static final ReportType REPORT_CANNOT_DELETE_CONFIG = new ReportType("Cannot delete old ProtocolLib configuration.");
	public static final ReportType REPORT_CANNOT_PARSE_INJECTION_METHOD = new ReportType("Cannot parse injection method. Using default.");

	public static final ReportType REPORT_PLUGIN_LOAD_ERROR = new ReportType("Cannot load ProtocolLib.");
	public static final ReportType REPORT_PLUGIN_ENABLE_ERROR = new ReportType("Cannot enable ProtocolLib.");

	public static final ReportType REPORT_METRICS_IO_ERROR = new ReportType("Unable to enable metrics due to network problems.");
	public static final ReportType REPORT_METRICS_GENERIC_ERROR = new ReportType("Unable to enable metrics due to network problems.");

	public static final ReportType REPORT_CANNOT_PARSE_MINECRAFT_VERSION = new ReportType("Unable to retrieve current Minecraft version. Assuming %s");
	public static final ReportType REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS = new ReportType("Unable to detect conflicting plugin versions.");
	public static final ReportType REPORT_CANNOT_REGISTER_COMMAND = new ReportType("Cannot register command %s: %s");

	public static final ReportType REPORT_CANNOT_CREATE_TIMEOUT_TASK = new ReportType("Unable to create packet timeout task.");
	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot perform automatic updates.");

	// Update information
	static final String BUKKIT_DEV_SLUG = "protocollib";
	static final int BUKKIT_DEV_ID = 45564;

	// Different commands
	private enum ProtocolCommand {
		FILTER,
		PACKET,
		PROTOCOL,
		LOGGING;
	}

	/**
	 * The number of milliseconds per second.
	 */
	static final long MILLI_PER_SECOND = 1000;

	private static final String PERMISSION_INFO = "protocol.info";

	// There should only be one protocol manager, so we'll make it static
	private static InternalManager protocolManager;

	// Error reporter
	private static ErrorReporter reporter = new BasicErrorReporter();

	// Strongly typed configuration
	private static ProtocolConfig config;

	// Metrics and statistics
	private Statistics statistics;

	// Structure compiler
	private BackgroundCompiler backgroundCompiler;

	// Used to clean up server packets that have expired, but mostly required to simulate
	// recieving client packets.
	private int packetTask = -1;
	private int tickCounter = 0;
	private static final int ASYNC_MANAGER_DELAY = 1;

	// Used to unhook players after a delay
	private DelayedSingleTask unhookTask;

	// Settings/options
	private int configExpectedMod = -1;

	// Updater
	private Updater updater;
	public static boolean UPDATES_DISABLED;

	// Logger
	private static Logger logger;
	private Handler redirectHandler;

	// Commands
	private CommandProtocol commandProtocol;
	private CommandPacket commandPacket;
	private CommandFilter commandFilter;
	private PacketLogging packetLogging;

	// Whether or not disable is not needed
	private boolean skipDisable;

	@Override
	public void onLoad() {
		// Logging
		logger = getLogger();
		ProtocolLogger.init(this);

		// Initialize enhancer factory
		EnhancerFactory.getInstance().setClassLoader(getClassLoader());

		// Add global parameters
		DetailedErrorReporter detailedReporter = new DetailedErrorReporter(this);
		reporter = getFilteredReporter(detailedReporter);

		// Configuration
		saveDefaultConfig();
		reloadConfig();

		try {
			config = new ProtocolConfig(this);
		} catch (Exception e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_LOAD_CONFIG).error(e));

			// Load it again
			if (deleteConfig()) {
				config = new ProtocolConfig(this);
			} else {
				reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_DELETE_CONFIG));
			}
		}

		// Print the state of the debug mode
		if (config.isDebug()) {
			logger.warning("Debug mode is enabled!");
		}
		// And the state of the error reporter
		if (config.isDetailedErrorReporting()) {
			detailedReporter.setDetailedReporting(true);
			logger.warning("Detailed error reporting enabled!");
		}

		try {
			// Check for other versions
			checkConflictingVersions();

			// Handle unexpected Minecraft versions
			MinecraftVersion version = verifyMinecraftVersion();

			// Set updater - this will not perform any update automatically
			updater = Updater.create(this, BUKKIT_DEV_ID, getFile(), UpdateType.NO_DOWNLOAD, true);

			unhookTask = new DelayedSingleTask(this);
			protocolManager = PacketFilterManager.newBuilder()
					.classLoader(getClassLoader())
					.server(getServer())
					.library(this)
					.minecraftVersion(version)
					.unhookTask(unhookTask)
					.reporter(reporter)
					.build();

			// Initialize the API
			ProtocolLibrary.init(this, config, protocolManager, reporter);

			// Setup error reporter
			detailedReporter.addGlobalParameter("manager", protocolManager);

			// Update injection hook
			try {
				PlayerInjectHooks hook = config.getInjectionMethod();

				// Only update the hook if it's different
				if (!protocolManager.getPlayerHook().equals(hook)) {
					logger.info("Changing player hook from " + protocolManager.getPlayerHook() + " to " + hook);
					protocolManager.setPlayerHook(hook);
				}
			} catch (IllegalArgumentException e) {
				reporter.reportWarning(config, Report.newBuilder(REPORT_CANNOT_PARSE_INJECTION_METHOD).error(e));
			}

			// Send logging information to player listeners too
			initializeCommands();
			setupBroadcastUsers(PERMISSION_INFO);

		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_PLUGIN_LOAD_ERROR).error(e).callerParam(protocolManager));
			disablePlugin();
		}
	}

	/**
	 * Initialize all command handlers.
	 */
	private void initializeCommands() {
		// Initialize command handlers
		for (ProtocolCommand command : ProtocolCommand.values()) {
			try {
				switch (command) {
				case PROTOCOL:
					commandProtocol = new CommandProtocol(reporter, this, updater, config);
					break;
				case FILTER:
					commandFilter = new CommandFilter(reporter, this, config);
					break;
				case PACKET:
					commandPacket = new CommandPacket(reporter, this, logger, commandFilter, protocolManager);
					break;
				case LOGGING:
					packetLogging = new PacketLogging(this, protocolManager);
					break;
				}
			} catch (OutOfMemoryError e) {
				throw e;
			} catch (LinkageError e) {
				logger.warning("Failed to register command " + command.name() + ": " + e);
			} catch (Throwable e) {
				reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_REGISTER_COMMAND)
						.messageParam(command.name(), e.getMessage()).error(e));
			}
		}
	}

	/**
	 * Retrieve a error reporter that may be filtered by the configuration.
	 * @return The new default error reporter.
	 */
	private ErrorReporter getFilteredReporter(ErrorReporter reporter) {
		return new DelegatedErrorReporter(reporter) {
			private int lastModCount = -1;
			private Set<String> reports = Sets.newHashSet();

			@Override
			protected Report filterReport(Object sender, Report report, boolean detailed) {
				try {
					String canonicalName = ReportType.getReportName(sender, report.getType());
					String reportName = Iterables.getLast(Splitter.on("#").split(canonicalName)).toUpperCase();

					if (config != null && config.getModificationCount() != lastModCount) {
						// Update our cached set again
						reports = Sets.newHashSet(config.getSuppressedReports());
						lastModCount = config.getModificationCount();
					}

					// Cancel reports either on the full canonical name, or just the report name
					if (reports.contains(canonicalName) || reports.contains(reportName))
						return null;

				} catch (Exception e) {
					// Only report this with a minor message
					logger.warning("Error filtering reports: " + e.toString());
				}
				// Don't filter anything
				return report;
			}
		};
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

	private void setupBroadcastUsers(final String permission) {
		// Guard against multiple calls
		if (redirectHandler != null)
			return;

		// Broadcast information to every user too
		redirectHandler = new Handler() {
			@Override
			public void publish(LogRecord record) {
				// Only display warnings and above
				if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
					commandPacket.broadcastMessageSilently(record.getMessage(), permission);
				}
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

			// Silly plugin reloaders!
			if (protocolManager == null) {
				Logger directLogging = Logger.getLogger("Minecraft");
				String[] message = new String[] {
						" ProtocolLib does not support plugin reloaders! ", " Please use the built-in reload command! "
				};

				// Print as severe
				for (String line : ChatExtensions.toFlowerBox(message, "*", 3, 1)) {
					directLogging.severe(line);
				}

				disablePlugin();
				return;
			}

			// Check for incompatible plugins
			checkForIncompatibility(manager);

			// Initialize background compiler
			if (backgroundCompiler == null && config.isBackgroundCompilerEnabled()) {
				backgroundCompiler = new BackgroundCompiler(getClassLoader(), reporter);
				BackgroundCompiler.setInstance(backgroundCompiler);

				logger.info("Started structure compiler thread.");
			} else {
				logger.info("Structure compiler thread has been disabled.");
			}

			// Set up command handlers
			registerCommand(CommandProtocol.NAME, commandProtocol);
			registerCommand(CommandPacket.NAME, commandPacket);
			registerCommand(CommandFilter.NAME, commandFilter);
			registerCommand(PacketLogging.NAME, packetLogging);

			// Player login and logout events
			protocolManager.registerEvents(manager, this);

			// Worker that ensures that async packets are eventually sent
			// It also performs the update check.
			createPacketTask(server);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_PLUGIN_ENABLE_ERROR).error(e));
			disablePlugin();
			return;
		}

		// Try to enable statistics
		try {
			if (config.isMetricsEnabled()) {
				statistics = new Statistics(this);
			}
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (IOException e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_METRICS_IO_ERROR).error(e).callerParam(statistics));
		} catch (Throwable e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_METRICS_GENERIC_ERROR).error(e).callerParam(statistics));
		}
	}

	// Plugin authors: Notify me to remove these

	private void checkForIncompatibility(PluginManager manager) {
		for (String plugin : ProtocolLibrary.INCOMPATIBLE) {
			if (manager.getPlugin(plugin) != null) {
				// Special case for TagAPI and iTag
				if (plugin.equals("TagAPI")) {
					Plugin iTag = manager.getPlugin("iTag");
					if (iTag == null || iTag.getDescription().getVersion().startsWith("1.0")) {
						logger.severe("Detected incompatible plugin: TagAPI");
					}
				} else {
					logger.severe("Detected incompatible plugin: " + plugin);
				}
			}
		}
	}

	// Used to check Minecraft version
	private MinecraftVersion verifyMinecraftVersion() {
		MinecraftVersion minimum = new MinecraftVersion(ProtocolLibrary.MINIMUM_MINECRAFT_VERSION);
		MinecraftVersion maximum = new MinecraftVersion(ProtocolLibrary.MAXIMUM_MINECRAFT_VERSION);

		try {
			MinecraftVersion current = new MinecraftVersion(getServer());

			// Skip certain versions
			if (!config.getIgnoreVersionCheck().equals(current.getVersion())) {
				// We'll just warn the user for now
				if (current.compareTo(minimum) < 0)
					logger.warning("Version " + current + " is lower than the minimum " + minimum);
				if (current.compareTo(maximum) > 0)
					logger.warning("Version " + current + " has not yet been tested! Proceed with caution.");
			}

			return current;
		} catch (Exception e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_PARSE_MINECRAFT_VERSION).error(e).messageParam(maximum));

			// Unknown version - just assume it is the latest
			return maximum;
		}
	}

	private void checkConflictingVersions() {
		Pattern ourPlugin = Pattern.compile("ProtocolLib-(.*)\\.jar");
		MinecraftVersion currentVersion = new MinecraftVersion(getDescription().getVersion());
		MinecraftVersion newestVersion = null;

		// Skip the file that contains this current instance however
		File loadedFile = getFile();

		try {
			// Scan the plugin folder for newer versions of ProtocolLib
			// The plugin folder isn't always plugins/
			File pluginFolder = getDataFolder().getParentFile();

			File[] candidates = pluginFolder.listFiles();
			if (candidates != null) {
				for (File candidate : candidates) {
					if (candidate.isFile() && !candidate.equals(loadedFile)) {
						Matcher match = ourPlugin.matcher(candidate.getName());
						if (match.matches()) {
							MinecraftVersion version = new MinecraftVersion(match.group(1));

							if (candidate.length() == 0) {
								// Delete and inform the user
								logger.info((candidate.delete() ? "Deleted " : "Could not delete ") + candidate);
							} else if (newestVersion == null || newestVersion.compareTo(version) < 0) {
								newestVersion = version;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO This shows [ProtocolLib] and [ProtocolLibrary] in the message
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_DETECT_CONFLICTING_PLUGINS).error(e));
		}

		// See if the newest version is actually higher
		if (newestVersion != null && currentVersion.compareTo(newestVersion) < 0) {
			// We don't need to set internal classes or instances to NULL - that would break the other loaded plugin
			skipDisable = true;

			throw new IllegalStateException(String.format(
					"Detected a newer version of ProtocolLib (%s) in plugin folder than the current (%s). Disabling.",
					newestVersion.getVersion(), currentVersion.getVersion()));
		}
	}

	private void registerCommand(String name, CommandExecutor executor) {
		try {
			// Ignore these - they must have printed an error already
			if (executor == null)
				return;

			PluginCommand command = getCommand(name);

			// Try to load the command
			if (command != null) {
				command.setExecutor(executor);
			} else {
				throw new RuntimeException("plugin.yml might be corrupt.");
			}
		} catch (RuntimeException e) {
			reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_REGISTER_COMMAND).messageParam(name, e.getMessage()).error(e));
		}
	}

	/**
	 * Disable the current plugin.
	 */
	private void disablePlugin() {
		getServer().getPluginManager().disablePlugin(this);
	}

	private void createPacketTask(Server server) {
		try {
			if (packetTask >= 0)
				throw new IllegalStateException("Packet task has already been created");

			// Attempt to create task
			packetTask = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				@Override
				public void run() {
					AsyncFilterManager manager = (AsyncFilterManager) protocolManager.getAsynchronousManager();

					// We KNOW we're on the main thread at the moment
					manager.sendProcessedPackets(tickCounter++, true);

					// House keeping
					updateConfiguration();

					// Check for updates too
					if (!UPDATES_DISABLED && (tickCounter % 20) == 0) {
						checkUpdates();
					}
				}
			}, ASYNC_MANAGER_DELAY, ASYNC_MANAGER_DELAY);
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Throwable e) {
			if (packetTask == -1) {
				reporter.reportDetailed(this, Report.newBuilder(REPORT_CANNOT_CREATE_TIMEOUT_TASK).error(e));
			}
		}
	}

	private void updateConfiguration() {
		if (config != null && config.getModificationCount() != configExpectedMod) {
			configExpectedMod = config.getModificationCount();

			// Update the debug flag
			protocolManager.setDebug(config.isDebug());
		}
	}

	private void checkUpdates() {
		// Ignore milliseconds - it's pointless
		long currentTime = System.currentTimeMillis() / MILLI_PER_SECOND;
		
		try {
			long updateTime = config.getAutoLastTime() + config.getAutoDelay();

			// Should we update?
			if (currentTime > updateTime && !updater.isChecking()) {
				// Initiate the update as if it came from the console
				if (config.isAutoDownload())
					commandProtocol.updateVersion(getServer().getConsoleSender(), false);
				else if (config.isAutoNotify())
					commandProtocol.checkVersion(getServer().getConsoleSender(), false);
				else
					commandProtocol.updateFinished();
			}
		} catch (Exception e) {
			reporter.reportDetailed(this, Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(e));
			UPDATES_DISABLED = true;
		}
	}

	@Override
	public void onDisable() {
		if (skipDisable) {
			return;
		}

		// Disable compiler
		if (backgroundCompiler != null) {
			backgroundCompiler.shutdownAll();
			backgroundCompiler = null;
			BackgroundCompiler.setInstance(null);
		}

		// Clean up
		if (packetTask >= 0) {
			getServer().getScheduler().cancelTask(packetTask);
			packetTask = -1;
		}

		// And redirect handler too
		if (redirectHandler != null) {
			logger.removeHandler(redirectHandler);
		}
		if (protocolManager != null)
			protocolManager.close();
		else
			return; // Plugin reloaders!

		if (unhookTask != null)
			unhookTask.close();
		protocolManager = null;
		statistics = null;

		// To clean up global parameters
		reporter = new BasicErrorReporter();
	}

	/**
	 * Retrieve the metrics instance used to measure users of this library.
	 * <p>
	 * Note that this method may return NULL when the server is reloading or shutting down. It is also
	 * NULL if metrics has been disabled.
	 * @return Metrics instance container.
	 */
	public Statistics getStatistics() {
		return statistics;
	}

	public ProtocolConfig getProtocolConfig() {
		return config;
	}
}
