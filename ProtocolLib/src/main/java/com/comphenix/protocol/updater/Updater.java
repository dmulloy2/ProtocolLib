/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.updater;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.Util;
import com.google.common.base.Preconditions;

/**
 * @author dmulloy2
 */

public abstract class Updater {
	protected Plugin plugin;

	protected String versionName;
	protected String versionLink;
	protected String versionType;
	protected String versionGameVersion;
	protected String versionFileName;

	protected UpdateType type;
	protected boolean announce;

	protected Thread thread;
	protected UpdateResult result = UpdateResult.SUCCESS;
	protected List<Runnable> listeners = new CopyOnWriteArrayList<Runnable>();

	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot update ProtocolLib.");

	protected Updater(Plugin plugin, UpdateType type, boolean announce) {
		this.plugin = plugin;
		this.type = type;
		this.announce = announce;
	}

	protected boolean versionCheck(String title) {
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            String version = this.plugin.getDescription().getVersion();

            boolean devBuild = false;
            if (version.contains("-SNAPSHOT") || version.contains("-BETA")) {
            	devBuild = true;
            	version = version.substring(0, version.indexOf("-"));
            }

            final String[] splitTitle = title.split(" ");
            String remoteVersion;

            if (splitTitle.length == 2) {
            	remoteVersion = splitTitle[1].split("-")[0];
            } else if (this instanceof SpigotUpdater) {
            	remoteVersion = splitTitle[0];
			} else {
				// The file's name did not contain the string 'vVersion'
				final String authorInfo = this.plugin.getDescription().getAuthors().size() == 0 ? "" : " (" + this.plugin.getDescription().getAuthors().get(0) + ")";
				this.plugin.getLogger().warning("The author of this plugin " + authorInfo + " has misconfigured their Auto Update system");
				this.plugin.getLogger().warning("File versions should follow the format 'PluginName VERSION[-SNAPSHOT]'");
				this.plugin.getLogger().warning("Please notify the author of this error.");
				this.result = BukkitUpdater.UpdateResult.FAIL_NOVERSION;
				return false;
			}

			// Parse the version
			MinecraftVersion parsedRemote = new MinecraftVersion(remoteVersion);
			MinecraftVersion parsedCurrent = new MinecraftVersion(plugin.getDescription().getVersion());

			if (devBuild && parsedRemote.equals(parsedCurrent)) {
				// They're using a dev build and this version has been released
				return true;
			}

			// The remote version has to be greater
			if (parsedRemote.compareTo(parsedCurrent) <= 0) {
				// We already have the latest version, or this build is tagged for no-update
				this.result = BukkitUpdater.UpdateResult.NO_UPDATE;
				return false;
			}
		}
		return true;
	}

    /**
     * Add a listener to be executed when we have determined if an update is available.
     * <p>
     * The listener will be executed on the main thread.
     * @param listener - the listener to add.
     */
    public void addListener(Runnable listener) {
    	listeners.add(Preconditions.checkNotNull(listener, "listener cannot be NULL"));
    }
    
    /**
     * Remove a listener.
     * @param listener - the listener to remove.
     * @return TRUE if the listener was removed, FALSE otherwise.
     */
    public boolean removeListener(Runnable listener) {
    	return listeners.remove(listener);
    }
    
    /**
     * Get the result of the update process.
     */
    public String getResult() {
        this.waitForThread();
        return this.result.toString();
    }

    /**
     * Get the latest version's release type (release, beta, or alpha).
     */
    public String getLatestType() {
        this.waitForThread();
        return this.versionType;
    }

    /**
     * Get the latest version's game version.
     */
    public String getLatestGameVersion() {
        this.waitForThread();
        return this.versionGameVersion;
    }

    /**
     * Get the latest version's name.
     */
    public String getLatestName() {
        this.waitForThread();
        return this.versionName;
    }

    /**
     * Get the latest version's file link.
     */
    public String getLatestFileLink() {
        this.waitForThread();
        return this.versionLink;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    protected void waitForThread() {
    	if (thread != null && thread.isAlive()) {
    		try {
    			thread.join();
    		} catch (InterruptedException ex) {
    			ex.printStackTrace();
    		}
    	}
    }

    /**
     * Determine if we are already checking for an update.
     * @return TRUE if we are, FALSE otherwise.
     */
    public boolean isChecking() {
    	return this.thread != null && this.thread.isAlive();
    }

    /**
     * Allows the dev to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }

    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS("The updater found an update, and has readied it to be loaded the next time the server restarts/reloads."),
        
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE("The updater did not find an update, and nothing was downloaded."),
        
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED("The server administrator has disabled the updating system"),
        
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD("The updater found an update, but was unable to download it."),
        
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO("For some reason, the updater was unable to contact dev.bukkit.org to download the file.")
        ,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION("When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'."),
        
        /**
         * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID("The id provided by the plugin running the updater was invalid and doesn't exist on DBO."),
        
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY("The server administrator has improperly configured their API key in the configuration"),
        
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE("The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded."),

        /**
         * The updater found an update at Spigot
         */
        SPIGOT_UPDATE_AVAILABLE("The updater found an update: %s (Running %s). Download at %s");

        private final String description;
        
        private UpdateResult(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
        	return description;
        }
    }

	public static Updater create(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
		if (Util.isUsingSpigot()) {
			return new SpigotUpdater(plugin, type, announce);
		} else {
			return new BukkitUpdater(plugin, id, file, type, announce);
		}
	}

	public abstract void start(UpdateType type);

	public boolean shouldNotify() {
		switch (result) {
			case SPIGOT_UPDATE_AVAILABLE:
			case SUCCESS:
			case UPDATE_AVAILABLE:
				return true;
			default:
				return false;
		}
	}
}
