package com.comphenix.protocol.scheduler;

import org.bukkit.scheduler.BukkitScheduler;

public class DefaultTask implements Task {
    private final int taskId;
    private final BukkitScheduler scheduler;

    public DefaultTask(BukkitScheduler scheduler, int taskId) {
        this.taskId = taskId;
        this.scheduler = scheduler;
    }

    @Override
    public void cancel() {
        scheduler.cancelTask(taskId);
    }
}
