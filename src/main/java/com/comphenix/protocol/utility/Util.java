/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.utility;

/**
 * General utility class
 *
 * @author dmulloy2
 */
public final class Util {

    private static final boolean SPIGOT = classExists("org.spigotmc.SpigotConfig");
    private static final boolean FOLIA = classExists("io.papermc.paper.threadedregions.RegionizedServer");

    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    /**
     * Whether this server is running Spigot or a Spigot fork. This works by checking if the SpigotConfig exists, which
     * should be true of all forks.
     *
     * @return True if it is, false if not.
     */
    public static boolean isUsingSpigot() {
        return SPIGOT;
    }

    public static boolean isUsingFolia() {
        return FOLIA;
    }

    /**
     * Whether the given class is loaded by ProtocolLib's class loader or one of its parents (the server and the JDK),
     * so it lives at least as long as ProtocolLib. Static caches must only keep such classes: a class of another plugin
     * would keep that plugin's class loader alive after the plugin is disabled or reloaded.
     *
     * @param type the class to check.
     * @return true if the class may be kept in a static cache of ProtocolLib.
     */
    public static boolean isLoadedByProtocolLibOrParent(Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        if (loader == null) {
            return true;
        }

        for (ClassLoader current = Util.class.getClassLoader(); current != null; current = current.getParent()) {
            if (current == loader) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the server is getting reloaded by walking down the current thread stack trace.
     *
     * @return true if the server is getting reloaded, false otherwise.
     */
    public static boolean isCurrentlyReloading() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            String clazz = element.getClassName();
            if (clazz.startsWith("org.bukkit.craftbukkit.")
                    && clazz.endsWith(".CraftServer")
                    && element.getMethodName().equals("reload")) {
                return true;
            }
        }
        return false;
    }
}
