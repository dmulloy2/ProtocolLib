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
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.injector.PacketFilterManager.PlayerInjectHooks;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Represents the configuration of ProtocolLib.
 * 
 * @author Kristian
 */
public class ProtocolConfig {
	private static final String LAST_UPDATE_FILE = "lastupdate";

	private static final String SECTION_GLOBAL = "global";
	private static final String SECTION_AUTOUPDATER = "auto updater";

	private static final String METRICS_ENABLED = "metrics";

	private static final String IGNORE_VERSION_CHECK = "ignore version check";
	private static final String BACKGROUND_COMPILER_ENABLED = "background compiler";

	private static final String DEBUG_MODE_ENABLED = "debug";
	private static final String DETAILED_ERROR = "detailed error";
	private static final String INJECTION_METHOD = "injection method";

	private static final String SCRIPT_ENGINE_NAME = "script engine";
	private static final String SUPPRESSED_REPORTS = "suppressed reports";

	private static final String UPDATER_NOTIFY = "notify";
	private static final String UPDATER_DOWNLAD = "download";
	private static final String UPDATER_DELAY = "delay";

	// Defaults
	private static final long DEFAULT_UPDATER_DELAY = 43200;

	private Plugin plugin;
	private Configuration config;
	private boolean loadingSections;

	private ConfigurationSection global;
	private ConfigurationSection updater;

	// Last update time
	private long lastUpdateTime;
	private boolean configChanged;
	private boolean valuesChanged;

	// Modifications
	private int modCount;

	public ProtocolConfig(Plugin plugin) {
		this.plugin = plugin;
		reloadConfig();
	}

	/**
	 * Reload configuration file.
	 */
	public void reloadConfig() {
		// Reset
		configChanged = false;
		valuesChanged = false;
		modCount++;

		this.config = plugin.getConfig();
		this.lastUpdateTime = loadLastUpdate();
		loadSections(!loadingSections);
	}

	/**
	 * Load the last update time stamp from the file system.
	 * 
	 * @return Last update time stamp.
	 */
	private long loadLastUpdate() {
		File dataFile = getLastUpdateFile();

		if (dataFile.exists()) {
			try {
				return Long.parseLong(Files.toString(dataFile, Charsets.UTF_8));
			} catch (NumberFormatException e) {
				plugin.getLogger().warning("Cannot parse " + dataFile + " as a number.");
			} catch (IOException e) {
				plugin.getLogger().warning("Cannot read " + dataFile);
			}
		}
		// Default last update
		return 0;
	}

	/**
	 * Store the given time stamp.
	 * 
	 * @param value - time stamp to store.
	 */
	private void saveLastUpdate(long value) {
		File dataFile = getLastUpdateFile();

		// The data folder must exist
		dataFile.getParentFile().mkdirs();

		if (dataFile.exists())
			dataFile.delete();

		try {
			Files.write(Long.toString(value), dataFile, Charsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("Cannot write " + dataFile, e);
		}
	}

	/**
	 * Retrieve the file that is used to store the update time stamp.
	 * 
	 * @return File storing the update time stamp.
	 */
	private File getLastUpdateFile() {
		return new File(plugin.getDataFolder(), LAST_UPDATE_FILE);
	}

	/**
	 * Load data sections.
	 * 
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
			ProtocolLibrary.log("Created default configuration.");
		}
	}

	/**
	 * Set a particular configuration key value pair.
	 * 
	 * @param section - the configuration root.
	 * @param path - the path to the key.
	 * @param value - the value to set.
	 */
	private void setConfig(ConfigurationSection section, String path, Object value) {
		configChanged = true;
		section.set(path, value);
	}

	@SuppressWarnings("unchecked")
	private <T> T getGlobalValue(String path, T def) {
		try {
			return (T) global.get(path, def);
		} catch (Throwable ex) {
			return def;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getUpdaterValue(String path, T def) {
		try {
			return (T) updater.get(path, def);
		} catch (Throwable ex) {
			return def;
		}
	}

	/**
	 * Retrieve a reference to the configuration file.
	 * 
	 * @return Configuration file on disk.
	 */
	public File getFile() {
		return new File(plugin.getDataFolder(), "config.yml");
	}

	/**
	 * Determine if detailed error reporting is enabled. Default FALSE.
	 * 
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isDetailedErrorReporting() {
		return getGlobalValue(DETAILED_ERROR, false);
	}

	/**
	 * Set whether or not detailed error reporting is enabled.
	 * 
	 * @param value - TRUE if it is enabled, FALSE otherwise.
	 */
	public void setDetailedErrorReporting(boolean value) {
		global.set(DETAILED_ERROR, value);
	}

	/**
	 * Retrieve whether or not ProtocolLib should determine if a new version has been released.
	 * 
	 * @return TRUE if it should do this automatically, FALSE otherwise.
	 */
	public boolean isAutoNotify() {
		return getUpdaterValue(UPDATER_NOTIFY, true);
	}

	/**
	 * Set whether or not ProtocolLib should determine if a new version has been released.
	 * 
	 * @param value - TRUE to do this automatically, FALSE otherwise.
	 */
	public void setAutoNotify(boolean value) {
		setConfig(updater, UPDATER_NOTIFY, value);
		modCount++;
	}

	/**
	 * Retrieve whether or not ProtocolLib should automatically download the new version.
	 * 
	 * @return TRUE if it should, FALSE otherwise.
	 */
	public boolean isAutoDownload() {
		return updater != null && getUpdaterValue(UPDATER_DOWNLAD, true);
	}

	/**
	 * Set whether or not ProtocolLib should automatically download the new version.
	 * 
	 * @param value - TRUE if it should. FALSE otherwise.
	 */
	public void setAutoDownload(boolean value) {
		setConfig(updater, UPDATER_DOWNLAD, value);
		modCount++;
	}

	/**
	 * Determine whether or not debug mode is enabled.
	 * <p>
	 * This grants access to the filter command.
	 * 
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isDebug() {
		return getGlobalValue(DEBUG_MODE_ENABLED, false);
	}

	/**
	 * Set whether or not debug mode is enabled.
	 * 
	 * @param value - TRUE if it is enabled, FALSE otherwise.
	 */
	public void setDebug(boolean value) {
		setConfig(global, DEBUG_MODE_ENABLED, value);
		modCount++;
	}

	/**
	 * Retrieve an immutable list of every suppressed report type.
	 * 
	 * @return Every suppressed report type.
	 */
	public ImmutableList<String> getSuppressedReports() {
		return ImmutableList.copyOf(getGlobalValue(SUPPRESSED_REPORTS, new ArrayList<String>()));
	}

	/**
	 * Set the list of suppressed report types,
	 * 
	 * @param reports - suppressed report types.
	 */
	public void setSuppressedReports(List<String> reports) {
		global.set(SUPPRESSED_REPORTS, Lists.newArrayList(reports));
		modCount++;
	}

	/**
	 * Retrieve the amount of time to wait until checking for a new update.
	 * 
	 * @return The amount of time to wait.
	 */
	public long getAutoDelay() {
		// Note that the delay must be greater than 59 seconds
		return Math.max(getUpdaterValue(UPDATER_DELAY, 0), DEFAULT_UPDATER_DELAY);
	}

	/**
	 * Set the amount of time to wait until checking for a new update.
	 * <p>
	 * This time must be greater than 59 seconds.
	 * 
	 * @param delaySeconds - the amount of time to wait.
	 */
	public void setAutoDelay(long delaySeconds) {
		// Silently fix the delay
		if (delaySeconds < DEFAULT_UPDATER_DELAY)
			delaySeconds = DEFAULT_UPDATER_DELAY;
		setConfig(updater, UPDATER_DELAY, delaySeconds);
		modCount++;
	}

	/**
	 * The version of Minecraft to ignore the built-in safety feature.
	 * 
	 * @return The version to ignore ProtocolLib's satefy.
	 */
	public String getIgnoreVersionCheck() {
		return getGlobalValue(IGNORE_VERSION_CHECK, "");
	}

	/**
	 * Sets under which version of Minecraft the version safety feature will be ignored.
	 * <p>
	 * This is useful if a server operator has tested and verified that a version of ProtocolLib works, but doesn't want or can't upgrade to a newer version.
	 * 
	 * @param ignoreVersion - the version of Minecraft where the satefy will be disabled.
	 */
	public void setIgnoreVersionCheck(String ignoreVersion) {
		setConfig(global, IGNORE_VERSION_CHECK, ignoreVersion);
		modCount++;
	}

	/**
	 * Retrieve whether or not metrics is enabled.
	 * 
	 * @return TRUE if metrics is enabled, FALSE otherwise.
	 */
	public boolean isMetricsEnabled() {
		return getGlobalValue(METRICS_ENABLED, true);
	}

	/**
	 * Set whether or not metrics is enabled.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 * 
	 * @param enabled - whether or not metrics is enabled.
	 */
	public void setMetricsEnabled(boolean enabled) {
		setConfig(global, METRICS_ENABLED, enabled);
		modCount++;
	}

	/**
	 * Retrieve whether or not the background compiler for structure modifiers is enabled or not.
	 * 
	 * @return TRUE if it is enabled, FALSE otherwise.
	 */
	public boolean isBackgroundCompilerEnabled() {
		return getGlobalValue(BACKGROUND_COMPILER_ENABLED, true);
	}

	/**
	 * Set whether or not the background compiler for structure modifiers is enabled or not.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 * 
	 * @param enabled - TRUE if is enabled/running, FALSE otherwise.
	 */
	public void setBackgroundCompilerEnabled(boolean enabled) {
		setConfig(global, BACKGROUND_COMPILER_ENABLED, enabled);
		modCount++;
	}

	/**
	 * Retrieve the last time we updated, in seconds since 1970.01.01 00:00.
	 * 
	 * @return Last update time.
	 */
	public long getAutoLastTime() {
		return lastUpdateTime;
	}

	/**
	 * Set the last time we updated, in seconds since 1970.01.01 00:00.
	 * <p>
	 * Note that this is not considered to modify the configuration, so the modification count will not be incremented.
	 * 
	 * @param lastTimeSeconds - new last update time.
	 */
	public void setAutoLastTime(long lastTimeSeconds) {
		this.valuesChanged = true;
		this.lastUpdateTime = lastTimeSeconds;
	}

	/**
	 * Retrieve the unique name of the script engine to use for filtering.
	 * 
	 * @return Script engine to use.
	 */
	public String getScriptEngineName() {
		return getGlobalValue(SCRIPT_ENGINE_NAME, "JavaScript");
	}

	/**
	 * Set the unique name of the script engine to use for filtering.
	 * <p>
	 * This setting will take effect next time ProtocolLib is started.
	 * 
	 * @param name - name of the script engine to use.
	 */
	public void setScriptEngineName(String name) {
		setConfig(global, SCRIPT_ENGINE_NAME, name);
		modCount++;
	}

	/**
	 * Retrieve the default injection method.
	 * 
	 * @return Default method.
	 */
	public PlayerInjectHooks getDefaultMethod() {
		return PlayerInjectHooks.NETWORK_SERVER_OBJECT;
	}

	/**
	 * Retrieve the injection method that has been set in the configuration, or use a default value.
	 * 
	 * @return Injection method to use.
	 * @throws IllegalArgumentException If the configuration option is malformed.
	 */
	public PlayerInjectHooks getInjectionMethod() throws IllegalArgumentException {
		String text = global.getString(INJECTION_METHOD);

		// Default hook if nothing has been set
		PlayerInjectHooks hook = getDefaultMethod();

		if (text != null)
			hook = PlayerInjectHooks.valueOf(text.toUpperCase().replace(" ", "_"));
		return hook;
	}

	/**
	 * Set the starting injection method to use.
	 * 
	 * @return Injection method.
	 */
	public void setInjectionMethod(PlayerInjectHooks hook) {
		setConfig(global, INJECTION_METHOD, hook.name());
		modCount++;
	}

	/**
	 * Retrieve the number of modifications made to this configuration.
	 * 
	 * @return The number of modifications.
	 */
	public int getModificationCount() {
		return modCount;
	}

	/**
	 * Save the current configuration file.
	 */
	public void saveAll() {
		if (valuesChanged)
			saveLastUpdate(lastUpdateTime);
		if (configChanged)
			plugin.saveConfig();

		// And we're done
		valuesChanged = false;
		configChanged = false;
	}
}