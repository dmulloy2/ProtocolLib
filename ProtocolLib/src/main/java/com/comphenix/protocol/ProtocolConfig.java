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
	
	private static final String METRICS_ENABLED = "metrics";
	
	private static final String IGNORE_VERSION_CHECK = "ignore version check";
	
	private static final String BACKGROUND_COMPILER_ENABLED = "background compiler";
	
	private static final String UPDATER_NOTIFY = "notify";
	private static final String UPDATER_DOWNLAD = "download";
	private static final String UPDATER_DELAY = "delay";
	private static final String UPDATER_LAST_TIME = "last";
	
	// Defaults
	private static final long DEFAULT_UPDATER_DELAY = 43200;
	
	private Plugin plugin;
	private Configuration config;
	private boolean loadingSections;
	
	private ConfigurationSection global;
	private ConfigurationSection updater;
	
	public ProtocolConfig(Plugin plugin) {
		this(plugin, plugin.getConfig());
	}
	
	public ProtocolConfig(Plugin plugin, Configuration config) {
		this.plugin = plugin;
		reloadConfig();
	}
	
	/**
	 * Reload configuration file.
	 */
	public void reloadConfig() {
		this.config = plugin.getConfig();
		loadSections(!loadingSections);
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
		if (copyDefaults && (!getFile().exists() || global == null || updater == null)) {
			loadingSections = true;
			
			if (config != null)
				config.options().copyDefaults(true);
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			loadingSections = false;	
			
			// Inform the user
			System.out.println("[ProtocolLib] Created default configuration.");
		}
	}

	/**
	 * Retrieve a reference to the configuration file.
	 * @return Configuration file on disk.
	 */
	public File getFile() {
		return new File(plugin.getDataFolder(), "config.yml");
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
	 * The version of Minecraft to ignore the built-in safety feature.
	 * @return The version to ignore ProtocolLib's satefy.
	 */
	public String getIgnoreVersionCheck() {
		return global.getString(IGNORE_VERSION_CHECK, "");
	}
	
	/**
	 * Sets under which version of Minecraft the version safety feature will be ignored.
	 * <p>
	 * This is useful if a server operator has tested and verified that a version of ProtocolLib works,
	 * but doesn't want or can't upgrade to a newer version.
	 * 
	 * @param ignoreVersion - the version of Minecraft where the satefy will be disabled.
	 */
	public void setIgnoreVersionCheck(String ignoreVersion) {
		global.set(IGNORE_VERSION_CHECK, ignoreVersion);
	}
	
	/**
	 * Retrieve whether or not metrics is enabled.
	 * @return TRUE if metrics is enabled, FALSE otherwise.
	 */
	public boolean isMetricsEnabled() {
		return global.getBoolean(METRICS_ENABLED, true);
	}
	
	/**
	 * Set whether or not metrics is enabled.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 * 
	 * @param enabled - whether or not metrics is enabled.
	 */
	public void setMetricsEnabled(boolean enabled) {
		global.set(METRICS_ENABLED, enabled);
	}
	
	/**
	 * Retrieve whether or not the background compiler for structure modifiers is enabled or not.
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isBackgroundCompilerEnabled() {
		return global.getBoolean(BACKGROUND_COMPILER_ENABLED, true);
	}
	
	/**
	 * Set whether or not the background compiler for structure modifiers is enabled or not.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 * 
	 * @param enabled - TRUE if is enabled/running, FALSE otherwise.
	 */
	public void setBackgroundCompilerEnabled(boolean enabled) {
		global.set(BACKGROUND_COMPILER_ENABLED, enabled);
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
