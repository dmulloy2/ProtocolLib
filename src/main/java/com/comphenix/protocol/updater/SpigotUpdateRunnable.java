/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 * Copyright (C) 2015 dmulloy2
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
