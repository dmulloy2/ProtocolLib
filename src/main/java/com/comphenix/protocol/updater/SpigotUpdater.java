/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 * Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package com.comphenix.protocol.updater;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.utility.Closer;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Adapted version of the Bukkit updater for use with Spigot resources
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
                if (ProtocolLibrary.getConfig().isDebug()) {
                    ProtocolLibrary.getErrorReporter().reportDetailed(
                            SpigotUpdater.this, Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(ex).callerParam(this));
                } else {
                    // People don't care
                    // plugin.getLogger().log(Level.WARNING, "Failed to check for updates: " + ex);
                }

                ProtocolLibrary.disableUpdates();
            } finally {
                // Invoke the listeners on the main thread
                for (Runnable listener : listeners) {
                    ProtocolLibrary.getScheduler().runTask(listener);
                }
            }
        }
    }

    private static final String RESOURCE_URL = "https://www.spigotmc.org/resources/protocollib.1997/";
    private static final String UPDATE_URL = "https://api.spigotmc.org/legacy/update.php?resource=1997";
    private static final String ACTION = "GET";

    public String getSpigotVersion() throws IOException {
        try (Closer closer = Closer.create()) {
            HttpURLConnection con = (HttpURLConnection) new URL(UPDATE_URL).openConnection();
            con.setDoOutput(true);
            con.setRequestMethod(ACTION);

            InputStreamReader isr = closer.register(new InputStreamReader(con.getInputStream()));
            BufferedReader br = closer.register(new BufferedReader(isr));
            return br.readLine();
        }
    }

    @Override
    public String getRemoteVersion() {
        return remoteVersion;
    }
}
