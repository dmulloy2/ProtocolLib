package com.comphenix.protocol;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

/**
 * Represents the configuration of ProtocolLib.
 * 
 * @author Kristian
 */
class ProtocolConfig {
	
	private static final String SECTION_GLOBAL = "global";
	private static final String SECTION_AUTOUPDATER = "auto updater";
	
	private static final String UPDATER_NOTIFY = "notify";
	private static final String UPDATER_DOWNLAD = "download";
	private static final String UPDATER_DELAY = "delay";
	private static final String UPDATER_LAST_TIME = "last";
	
	// Defaults
	private static final long DEFAULT_UPDATER_DELAY = 60;
	
	private Plugin plugin;
	private Configuration config;
	
	private ConfigurationSection global;
	private ConfigurationSection updater;
	
	public ProtocolConfig(Plugin plugin) {
		this(plugin, plugin.getConfig());
	}
	
	public ProtocolConfig(Plugin plugin, Configuration config) {
		this.config = config;
		loadSections(true);
	}

	/**
	 * Load data sections.
	 * @param copyDefaults - whether or not to copy configuration defaults.
	 */
	private void loadSections(boolean copyDefaults) {
		if (config != null) {
			global = config.getConfigurationSection(SECTION_GLOBAL);
		}
		if (global != null) {
			updater = global.getConfigurationSection(SECTION_AUTOUPDATER);
		}
		
		// Automatically copy defaults
		if (copyDefaults && (global == null || updater == null)) {
			config.options().copyDefaults(true);
			loadSections(false);
		}
	}
	
	/**
	 * Retrieve whether or not ProtocolLib should determine if a new version has been released.
	 * @return TRUE if it should do this automatically, FALSE otherwise.
	 */
	public boolean isAutoNotify() {
		return updater.getBoolean(UPDATER_NOTIFY, true);
	}
	
	/**
	 * Set whether or not ProtocolLib should determine if a new version has been released.
	 * @param value - TRUE to do this automatically, FALSE otherwise.
	 */
	public void setAutoNotify(boolean value) {
		updater.set(UPDATER_NOTIFY, value);
	}
	
	/**
	 * Retrieve whether or not ProtocolLib should automatically download the new version.
	 * @return TRUE if it should, FALSE otherwise.
	 */
	public boolean isAutoDownload() {
		return updater != null && updater.getBoolean(UPDATER_DOWNLAD, true);
	}
	
	/**
	 * Set whether or not ProtocolLib should automatically download the new version.
	 * @param value - TRUE if it should. FALSE otherwise.
	 */
	public void setAutoDownload(boolean value) {
		updater.set(UPDATER_DOWNLAD, value);
	}

	/**
	 * Retrieve the amount of time to wait until checking for a new update.
	 * @return The amount of time to wait.
	 */
	public long getAutoDelay() {
		// Note that the delay must be greater than 59 seconds
		return Math.max(updater.getInt(UPDATER_DELAY, 0), DEFAULT_UPDATER_DELAY);
	}
	
	/**
	 * Set the amount of time to wait until checking for a new update.
	 * <p>
	 * This time must be greater than 59 seconds.
	 * @param delaySeconds - the amount of time to wait.
	 */
	public void setAutoDelay(long delaySeconds) {
		// Silently fix the delay
		if (delaySeconds < DEFAULT_UPDATER_DELAY)
			delaySeconds = DEFAULT_UPDATER_DELAY;
		updater.set(UPDATER_DELAY, delaySeconds);
	}
	
	/**
	 * Retrieve the last time we updated, in seconds since 1970.01.01 00:00.
	 * @return Last update time.
	 */
	public long getAutoLastTime() {
		return updater.getLong(UPDATER_LAST_TIME, 0);
	}
	
	/**
	 * Set the last time we updated, in seconds since 1970.01.01 00:00.
	 * @param lastTimeSeconds - new last update time.
	 */
	public void setAutoLastTime(long lastTimeSeconds) { 
		updater.set(UPDATER_LAST_TIME, lastTimeSeconds);
	}
	
	/**
	 * Save the current configuration file.
	 */
	public void saveAll() {
		plugin.saveConfig();
	}
}
