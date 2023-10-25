package com.comphenix.protocol.scheduler;

public interface ProtocolScheduler {
    Task scheduleSyncRepeatingTask(Runnable task, long delay, long period);

    Task runTask(Runnable task);

    Task scheduleSyncDelayedTask(Runnable task, long delay);

    Task runTaskAsync(Runnable task);
}
