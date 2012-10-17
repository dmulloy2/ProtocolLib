package com.comphenix.protocol.injector;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Represents a single delayed task.
 * 
 * @author Kristian
 */
public class DelayedSingleTask {

	protected int taskID = -1;
	protected Plugin plugin;
	protected BukkitScheduler scheduler;
	protected boolean closed;
	
	/**
	 * Create a single task scheduler.
	 * @param plugin - owner plugin.
	 */
	public DelayedSingleTask(Plugin plugin) {
		this.plugin = plugin;
		this.scheduler = plugin.getServer().getScheduler();
	}
	
	/**
	 * Create a single task scheduler.
	 * @param plugin - owner plugin.
	 * @param scheduler - specialized scheduler.
	 */
	public DelayedSingleTask(Plugin plugin, BukkitScheduler scheduler) {
		this.plugin = plugin;
		this.scheduler = scheduler;
	}

	/**
	 * Schedule a single task for execution. 
	 * <p>
	 * Any previously scheduled task will be automatically cancelled.
	 * <p>
	 * Note that a tick delay of zero will execute the task immediately. 
	 * 
	 * @param ticksDelay - number of ticks before the task is executed.
	 * @param task - the task to schedule.
	 * @return TRUE if the task was successfully scheduled or executed, FALSE otherwise.
	 */
	public boolean schedule(long ticksDelay, Runnable task) {
		if (ticksDelay < 0)
			throw new IllegalArgumentException("Tick delay cannot be negative.");
		if (task == null)
			throw new IllegalArgumentException("task cannot be NULL");
		if (closed)
			return false;
		
		// Special case
		if (ticksDelay == 0) {
			task.run();
			return true;
		}
		
		// Boilerplate, boilerplate
		final Runnable dispatch = task;
		
		// Don't run multiple tasks!
		cancel();
		taskID = scheduler.scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				dispatch.run();
				taskID = -1;
			}
		}, ticksDelay);
		
		return isRunning();
	}

	/**
	 * Whether or not a future task is scheduled to be executed.
	 * @return TRUE if a current task has been scheduled for execution, FALSE otherwise.
	 */
	public boolean isRunning() {
		return taskID >= 0;
	}
	
	/**
	 * Cancel a future task from being executed.
	 * @return TRUE if a task was cancelled, FALSE otherwise.
	 */
	public boolean cancel() {
		if (isRunning()) {
			scheduler.cancelTask(taskID);
			taskID = -1;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Retrieve the raw task ID.
	 * @return Raw task ID, or negative one if no task has been scheduled.
	 */
	public int getTaskID() {
		return taskID;
	}

	/**
	 * Retrieve the plugin this task belongs to.
	 * @return The plugin scheduling the current taks.
	 */
	public Plugin getPlugin() {
		return plugin;
	}
	
	/**
	 * Stop the current task and all future tasks scheduled by this instance.
	 */
	public synchronized void close() {
		if (!closed) {
			cancel();
			plugin = null;
			scheduler = null;
			closed = true;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		close();
	}
}
