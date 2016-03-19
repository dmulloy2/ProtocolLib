package com.comphenix.protocol;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

public class ProtocolLibrary {
	public static final long MILLI_PER_SECOND = 1000;
	public static final List<String> INCOMPATIBLE = Arrays.asList("TagAPI");

	/**
	 * The minimum version ProtocolLib has been tested with.
	 */
	public static final String MINIMUM_MINECRAFT_VERSION = "1.9";

	/**
	 * The maximum version ProtocolLib has been tested with,
	 */
	public static final String MAXIMUM_MINECRAFT_VERSION = "1.9";

	/**
	 * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version (1.9) was released.
	 */
	public static final String MINECRAFT_LAST_RELEASE_DATE = "2016-02-29";

	private static Plugin plugin;
	private static ProtocolConfig config;
	private static ProtocolManager manager;
	private static ErrorReporter reporter = new BasicErrorReporter();

	private static ListeningScheduledExecutorService executorAsync;
	private static ListeningScheduledExecutorService executorSync;

	private static boolean updatesDisabled;

	protected static void init(Plugin plugin, ProtocolConfig config, ProtocolManager manager, ErrorReporter reporter,
			ListeningScheduledExecutorService executorAsync, ListeningScheduledExecutorService executorSync) {
		ProtocolLibrary.plugin = plugin;
		ProtocolLibrary.config = config;
		ProtocolLibrary.manager = manager;
		ProtocolLibrary.reporter = reporter;
		ProtocolLibrary.executorAsync = executorAsync;
		ProtocolLibrary.executorSync = executorSync;
	}

	public static Plugin getPlugin() {
		return plugin;
	}

	public static ProtocolConfig getConfig() {
		return config;
	}

	public static ProtocolManager getProtocolManager() {
		return manager;
	}

	public static ErrorReporter getErrorReporter() {
		return reporter;
	}

	public static void disableUpdates() {
		updatesDisabled = true;
	}

	public static boolean updatesDisabled() {
		return updatesDisabled;
	}

	public static ListeningScheduledExecutorService getExecutorAsync() {
		return executorAsync;
	}

	public static ListeningScheduledExecutorService getExecutorSync() {
		return executorSync;
	}

	public static void log(Level level, String message, Object... args) {
		plugin.getLogger().log(level, MessageFormat.format(message, args));
	}

	public static void log(String message, Object... args) {
		log(Level.INFO, message, args);
	}

	public static void log(Level level, String message, Throwable ex) {
		plugin.getLogger().log(level, message, ex);
	}
}