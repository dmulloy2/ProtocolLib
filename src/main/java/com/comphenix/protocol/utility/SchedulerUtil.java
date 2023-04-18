package com.comphenix.protocol.utility;

import org.bukkit.plugin.Plugin;

public class SchedulerUtil {

    public static int scheduleSyncRepeatingTask(Plugin plugin, long delay, long period, Runnable runnable) {
        if (Util.isUsingFolia()) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay, period);
            return 1;
        } else {
            return plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
        }
    }

    public static void cancelTask(Plugin plugin, int id) {
        if (Util.isUsingFolia()) {
            plugin.getServer().getGlobalRegionScheduler().cancelTasks(plugin);
        } else {
            plugin.getServer().getScheduler().cancelTask(id);
        }
    }

    public static void execute(Runnable runnable, Plugin plugin) {
        if (Util.isUsingFolia()) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, runnable);
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable);
        }
    }

}
