package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.ProtocolLib;
import org.bukkit.plugin.Plugin;

public interface ProtocolScheduler {
    Task scheduleSyncRepeatingTask(Runnable task, long delay, long period);

    Task runTask(Runnable task);

    Task scheduleSyncDelayedTask(Runnable task, long delay);
}
