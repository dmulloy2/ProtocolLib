package com.comphenix.protocol.utility;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class SchedulerUtil {
    private Object foliaScheduler;
    private MethodAccessor runAtFixedRate;
    private MethodAccessor cancelTasks;
    private MethodAccessor execute;

    private static SchedulerUtil getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final SchedulerUtil INSTANCE = new SchedulerUtil();
    }

    private SchedulerUtil() {
        if (Util.isUsingFolia()) {
            MethodAccessor getScheduler = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getGlobalRegionScheduler");
            foliaScheduler = getScheduler.invoke(Bukkit.getServer());

            runAtFixedRate = Accessors.getMethodAccessor(foliaScheduler.getClass(), "runAtFixedRate", Plugin.class,
                    Consumer.class, long.class, long.class);
            cancelTasks = Accessors.getMethodAccessor(foliaScheduler.getClass(), "cancelTasks", Plugin.class);
            execute = Accessors.getMethodAccessor(foliaScheduler.getClass(), "execute", Plugin.class, Runnable.class);
        }
    }

    public static int scheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        return getInstance().doScheduleSyncRepeatingTask(plugin, runnable, delay, period);
    }

    private int doScheduleSyncRepeatingTask(Plugin plugin, Runnable runnable, long delay, long period) {
        if (Util.isUsingFolia()) {
            runAtFixedRate.invoke(foliaScheduler, plugin, (Consumer<Object>)(task -> runnable.run()), delay, period);
            return 1;
        } else {
            return plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, runnable, delay, period);
        }
    }

    private void doCancelTask(Plugin plugin, int id) {
        if (Util.isUsingFolia()) {
            cancelTasks.invoke(foliaScheduler, plugin);
        } else {
            plugin.getServer().getScheduler().cancelTask(id);
        }
    }

    public static void cancelTask(Plugin plugin, int id) {
        getInstance().doCancelTask(plugin, id);
    }

    private void doExecute(Plugin plugin, Runnable runnable) {
        if (Util.isUsingFolia()) {
            execute.invoke(foliaScheduler, plugin, runnable);
        } else {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable);
        }
    }

    public static void execute(Plugin plugin, Runnable runnable) {
        getInstance().doExecute(plugin, runnable);
    }
}
