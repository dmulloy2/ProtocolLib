/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2016 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol;

import com.comphenix.protocol.error.BasicErrorReporter;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.scheduler.ProtocolScheduler;
import com.comphenix.protocol.utility.MinecraftVersion;
import java.util.List;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

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
    public static final String MAXIMUM_MINECRAFT_VERSION = "1.20.4";

    /**
     * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version (1.20.4) was released.
     */
    public static final String MINECRAFT_LAST_RELEASE_DATE = "2023-12-07";

    /**
     * Plugins that are currently incompatible with ProtocolLib.
     */
    public static final List<String> INCOMPATIBLE = ImmutableList.of("TagAPI");

    private static Plugin plugin;
    private static ProtocolConfig config;
    private static ProtocolManager manager;
    private static ProtocolScheduler scheduler;
    private static ErrorReporter reporter = new BasicErrorReporter();

    private static boolean updatesDisabled;
    private static boolean initialized;

    protected static void init(Plugin plugin, ProtocolConfig config, ProtocolManager manager,
                               ProtocolScheduler scheduler, ErrorReporter reporter) {
        Validate.isTrue(!initialized, "ProtocolLib has already been initialized.");
        ProtocolLibrary.plugin = plugin;
        ProtocolLibrary.config = config;
        ProtocolLibrary.manager = manager;
        ProtocolLibrary.reporter = reporter;
        ProtocolLibrary.scheduler = scheduler;
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

    public static ProtocolScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Retrieve the current error reporter.
     * @return Current error reporter.
     */
    public static ErrorReporter getErrorReporter() {
        return reporter;
    }

    /**
     * Disables the ProtocolLib update checker.
     */
    public static void disableUpdates() {
        updatesDisabled = true;
    }

    /**
     * Whether updates are currently disabled.
     * @return True if it is, false if not
     */
    public static boolean updatesDisabled() {
        return updatesDisabled;
    }
}
