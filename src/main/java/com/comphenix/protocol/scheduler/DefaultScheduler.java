package com.comphenix.protocol.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class DefaultScheduler implements ProtocolScheduler {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public DefaultScheduler(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    @Override
    public Task scheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        int taskId = scheduler.scheduleSyncRepeatingTask(plugin, task, delay, period);
        return taskId >= 0 ? new DefaultTask(scheduler, taskId) : null;
    }

    @Override
    public Task runTask(Runnable task) {
        int taskId = scheduler.runTask(plugin, task).getTaskId();
        return taskId >= 0 ? new DefaultTask(scheduler, taskId) : null;
    }

    @Override
    public Task scheduleSyncDelayedTask(Runnable task, long delay) {
        int taskId = scheduler.scheduleSyncDelayedTask(plugin, task, delay);
        return taskId >= 0 ? new DefaultTask(scheduler, taskId) : null;
    }

    @Override
    public Task runTaskAsync(Runnable task) {
    	int taskId = scheduler.runTaskAsynchronously(plugin, task).getTaskId();
        return taskId >= 0 ? new DefaultTask(scheduler, taskId) : null;
    }
}
