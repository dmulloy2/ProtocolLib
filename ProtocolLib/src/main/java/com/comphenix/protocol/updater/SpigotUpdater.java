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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.google.common.base.Charsets;

/**
 * Adapted version of the Bukkit updater for use with Spigot resources
 * 
 * @author dmulloy2
 */

public final class SpigotUpdater extends Updater {
	private String remoteVersion;

	public SpigotUpdater(Plugin plugin, UpdateType type, boolean announce) {
		super(plugin, type, announce);
	}

	@Override
	public void start(UpdateType type) {
    	waitForThread();
  
    	this.type = type;
        this.thread = new Thread(new SpigotUpdateRunnable());
        this.thread.start();
	}

	@Override
	public String getResult() {
		waitForThread();
		return String.format(result.toString(), remoteVersion, plugin.getDescription().getVersion(), RESOURCE_URL);
	}

	private class SpigotUpdateRunnable implements Runnable {

		@Override
		public void run() {
			try {
				String version = getSpigotVersion();
				remoteVersion = version;

				if (versionCheck(version)) {
					result = UpdateResult.SPIGOT_UPDATE_AVAILABLE;
				} else {
					result = UpdateResult.NO_UPDATE;
				}
			} catch (Throwable ex) {
				ProtocolLibrary.getErrorReporter().reportDetailed(
						SpigotUpdater.this, Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(ex).callerParam(this));
        	} finally {
        		// Invoke the listeners on the main thread
        		for (Runnable listener : listeners) {
        			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, listener);
        		}
        	}
		}
	}

	private static final String RESOURCE_URL = "https://www.spigotmc.org/resources/protocollib.1997/";
	private static final String API_URL = "http://www.spigotmc.org/api/general.php";
	private static final String ACTION = "POST";

	private static final int ID = 1997;
	private static final byte[] API_KEY = ("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + ID).getBytes(Charsets.UTF_8);

	private String getSpigotVersion() throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(API_URL).openConnection();
		con.setDoOutput(true);
		con.setRequestMethod(ACTION);
		con.getOutputStream().write(API_KEY);
		String version = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
		if (version.length() <= 7) {
			return version;
		}

		return null;
	}
}