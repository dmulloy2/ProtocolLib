package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.plugin.Plugin;

/** Just modified BukkitRunnable */
public abstract class UniversalRunnable implements Runnable {
    Task task;

    public synchronized void cancel() throws IllegalStateException {
        checkScheduled();
        task.cancel();
    }
    
    /**
     * Schedules this in the scheduler to run on next tick.
     *
     * @param plugin the reference to the plugin scheduling task
     * @return {@link Task}
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see ProtocolScheduler#runTask(Runnable)
     */

    public synchronized Task runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(ProtocolLibrary.getScheduler().runTask(this));
    }

    /**
     * Schedules this to run after the specified number of server ticks.
     *
     * @param plugin the reference to the plugin scheduling task
     * @param delay  the ticks to wait before running the task
     * @return {@link Task}
     * @throws IllegalArgumentException if plugin is null
     * @throws IllegalStateException    if this was already scheduled
     * @see ProtocolScheduler#scheduleSyncDelayedTask(Runnable, long)
     */

    public synchronized Task runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        checkNotYetScheduled();
        return setupTask(ProtocolLibrary.getScheduler().scheduleSyncDelayedTask(this, delay));
    }

    private void checkScheduled() {
        if (task == null) {
            throw new IllegalStateException("Not scheduled yet");
        }
    }

    private void checkNotYetScheduled() {
        if (task != null) {
            throw new IllegalStateException("Already scheduled");
        }
    }


    private Task setupTask(final Task task) {
        this.task = task;
        return task;
    }


}
