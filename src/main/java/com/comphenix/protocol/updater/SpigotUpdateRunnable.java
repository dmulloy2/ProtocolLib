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

/**
 * Update check worker (top-level class so the runtime always loads a single {@code .class} entry).
 */
final class SpigotUpdateRunnable implements Runnable {

    private final SpigotUpdater updater;

    SpigotUpdateRunnable(SpigotUpdater updater) {
        this.updater = updater;
    }

    @Override
    public void run() {
        try {
            String version = this.updater.getSpigotVersion();
            this.updater.setRemoteVersion(version);

            if (this.updater.versionCheck(version)) {
                this.updater.result = Updater.UpdateResult.SPIGOT_UPDATE_AVAILABLE;
            } else {
                this.updater.result = Updater.UpdateResult.NO_UPDATE;
            }
        } catch (Throwable ex) {
            if (ProtocolLibrary.getConfig().isDebug()) {
                ProtocolLibrary.getErrorReporter().reportDetailed(
                        this.updater,
                        Report.newBuilder(Updater.REPORT_CANNOT_UPDATE_PLUGIN).error(ex).callerParam(this));
            }

            ProtocolLibrary.disableUpdates();
        } finally {
            for (Runnable listener : this.updater.listeners) {
                ProtocolLibrary.getScheduler().runTask(listener);
            }
        }
    }
}
