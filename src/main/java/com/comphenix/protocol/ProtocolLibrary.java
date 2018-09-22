/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

/**
 * The main entry point for ProtocolLib.
 * @author dmulloy2
 */
public class ProtocolLibrary {
	/**
	 * The minimum version ProtocolLib has been tested with.
	 */
	public static final String MINIMUM_MINECRAFT_VERSION = "1.8";

	/**
	 * The maximum version ProtocolLib has been tested with.
	 */
	public static final String MAXIMUM_MINECRAFT_VERSION = "1.13.1";

	/**
	 * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version (1.13.1) was released.
	 */
	public static final String MINECRAFT_LAST_RELEASE_DATE = "2018-08-22";

	/**
	 * Plugins that are currently incompatible with ProtocolLib.
	 */
	public static final List<String> INCOMPATIBLE = Arrays.asList("TagAPI");

	private static Plugin plugin;
	private static ProtocolConfig config;
	private static ProtocolManager manager;
	private static ErrorReporter reporter = new BasicErrorReporter();

	private static ListeningScheduledExecutorService executorAsync;
	private static ListeningScheduledExecutorService executorSync;

	private static boolean updatesDisabled;
	private static boolean initialized;

	protected static void init(Plugin plugin, ProtocolConfig config, ProtocolManager manager, ErrorReporter reporter,
			ListeningScheduledExecutorService executorAsync, ListeningScheduledExecutorService executorSync) {
		Validate.isTrue(!initialized, "ProtocolLib has already been initialized.");
		ProtocolLibrary.plugin = plugin;
		ProtocolLibrary.config = config;
		ProtocolLibrary.manager = manager;
		ProtocolLibrary.reporter = reporter;
		ProtocolLibrary.executorAsync = executorAsync;
		ProtocolLibrary.executorSync = executorSync;
		initialized = true;
	}

	/**
	 * Gets the ProtocolLib plugin instance.
	 * @return The plugin instance
	 */
	public static Plugin getPlugin() {
		return plugin;
	}

	/**
	 * Gets ProtocolLib's configuration
	 * @return The config
	 */
	public static ProtocolConfig getConfig() {
		return config;
	}

	/**
	 * Retrieves the packet protocol manager.
	 * @return Packet protocol manager
	 */
	public static ProtocolManager getProtocolManager() {
		return manager;
	}

	/**
	 * Retrieve the current error reporter.
	 * @return Current error reporter.
	 */
	public static ErrorReporter getErrorReporter() {
		return reporter;
	}

	/**
	 * Retrieve an executor service for performing asynchronous tasks on the behalf of ProtocolLib.
	 * <p>
	 * Note that this service is NULL if ProtocolLib has not been initialized yet.
	 * @return The executor service, or NULL.
	 */
	public static ListeningScheduledExecutorService getExecutorAsync() {
		return executorAsync;
	}

	/**
	 * Retrieve an executor service for performing synchronous tasks (main thread) on the behalf of ProtocolLib.
	 * <p>
	 * Note that this service is NULL if ProtocolLib has not been initialized yet.
	 * @return The executor service, or NULL.
	 */
	public static ListeningScheduledExecutorService getExecutorSync() {
		return executorSync;
	}

	/**
	 * Disables the ProtocolLib update checker.
	 */
	public static void disableUpdates() {
		updatesDisabled = true;
	}

	/**
	 * Whether or not updates are currently disabled.
	 * @return True if it is, false if not
	 */
	public static boolean updatesDisabled() {
		return updatesDisabled;
	}
}
