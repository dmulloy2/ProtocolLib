package com.comphenix.protocol.utility;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

/**
 * Allows us to stay backwards compatible with older versions of Bukkit.
 * 
 * @author Kristian
 */
public class WrappedScheduler {
	/**
	 * Represents a backwards compatible Bukkit task.
	 */
	public static interface TaskWrapper {
		/**
		 * Cancel the current task.
		 */
		public void cancel();
	}
	
	/**
	 * Schedule a given task for a single asynchronous execution.
	 * @param plugin - the owner plugin.
	 * @param runnable - the task to run.
	 * @param firstDelay - the amount of time to wait until executing the task.
	 * @return A cancel token.
	 */
	public static TaskWrapper runAsynchronouslyOnce(final Plugin plugin, Runnable runnable, long firstDelay) {
		return runAsynchronouslyRepeat(plugin, plugin.getServer().getScheduler(), runnable, firstDelay, -1L);
	}
	
	/**
	 * Schedule a given task for multiple asynchronous executions.
	 * @param plugin - the owner plugin.
	 * @param runnable - the task to run.
	 * @param firstDelay - the amount of time to wait until executing the task for the first time.
	 * @param repeatDelay - the amount of time inbetween each execution. If less than zero, the task is only executed once.
	 * @return A cancel token.
	 */
	public static TaskWrapper runAsynchronouslyRepeat(final Plugin plugin, Runnable runnable, long firstDelay, long repeatDelay) {
		return runAsynchronouslyRepeat(plugin, plugin.getServer().getScheduler(), runnable, firstDelay, repeatDelay);
	}
	
	/**
	 * Schedule a given task for asynchronous execution.
	 * @param plugin - the owner plugin.
	 * @param scheduler - the current Bukkit scheduler.
	 * @param runnable - the task to run.
	 * @param firstDelay - the amount of time to wait until executing the task for the first time.
	 * @param repeatDelay - the amount of time inbetween each execution. If less than zero, the task is only executed once.
	 * @return A cancel token.
	 */
	public static TaskWrapper runAsynchronouslyRepeat(final Plugin plugin, final BukkitScheduler scheduler, Runnable runnable, long firstDelay, long repeatDelay) {
		try {
			@SuppressWarnings("deprecation")
			final int taskID = scheduler.scheduleAsyncRepeatingTask(plugin, runnable, firstDelay, repeatDelay);

			// Return the cancellable object
			return new TaskWrapper() {
				@Override
				public void cancel() {
					scheduler.cancelTask(taskID);
				}
			};
			
		} catch (NoSuchMethodError e) {
			return tryUpdatedVersion(plugin, scheduler, runnable, firstDelay, repeatDelay);
		}
	}
	
	/**
	 * Attempt to do the same with the updated scheduling method.
	 * @param plugin - the owner plugin.
	 * @param scheduler - the current Bukkit scheduler.
	 * @param runnable - the task to run.
	 * @param firstDelay - the amount of time to wait until executing the task for the first time.
	 * @param repeatDelay - the amount of time inbetween each execution. If less than zero, the task is only executed once.
	 * @return A cancel token.
	 */
	private static TaskWrapper tryUpdatedVersion(final Plugin plugin, final BukkitScheduler scheduler, Runnable runnable, long firstDelay, long repeatDelay) {
		final BukkitTask task = scheduler.runTaskTimerAsynchronously(plugin, runnable, firstDelay, repeatDelay);
		
		return new TaskWrapper() {
			@Override
			public void cancel() {
				task.cancel();
			}
		};
	}
}
