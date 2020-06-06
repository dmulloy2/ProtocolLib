package com.comphenix.protocol.executors;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.executors.AbstractBukkitService;
import com.comphenix.protocol.executors.BukkitScheduledExecutorService;
import com.comphenix.protocol.executors.PendingTasks;
import com.comphenix.protocol.executors.PluginDisabledListener;
import com.google.common.base.Preconditions;

public class BukkitExecutors {
	private BukkitExecutors() {
		// Don't make it constructable
	}
	
	/**
	 * Retrieves a scheduled executor service for running tasks on the main thread.
	 * @param plugin - plugin that is executing the given tasks.
	 * @return Executor service.
	 */
	public static BukkitScheduledExecutorService newSynchronous(final Plugin plugin) {
		// Bridge destination
		final BukkitScheduler scheduler = getScheduler(plugin);
		Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
		
		BukkitScheduledExecutorService service = new com.comphenix.protocol.executors.AbstractBukkitService(new PendingTasks(plugin, scheduler)) {
			@Override
			protected BukkitTask getTask(Runnable command) {
				return scheduler.runTask(plugin, command);
			}

			@Override
			protected BukkitTask getLaterTask(Runnable task, long ticks) {
				return scheduler.runTaskLater(plugin, task, ticks);
			}
			
			@Override
			protected BukkitTask getTimerTask(long ticksInitial, long ticksDelay, Runnable task) {
				return scheduler.runTaskTimer(plugin, task, ticksInitial, ticksDelay);
			}
		};
		
		PluginDisabledListener.getListener(plugin).addService(service);
		return service;
	}
	
	/**
	 * Retrieves a scheduled executor service for running asynchronous tasks.
	 * @param plugin - plugin that is executing the given tasks.
	 * @return Asynchronous executor service.
	 */
	public static BukkitScheduledExecutorService newAsynchronous(final Plugin plugin) {
		// Bridge destination
		final BukkitScheduler scheduler = getScheduler(plugin);
		Preconditions.checkNotNull(plugin, "plugin cannot be NULL");
		
		BukkitScheduledExecutorService service = new com.comphenix.protocol.executors.AbstractBukkitService(new PendingTasks(plugin, scheduler)) {
			@Override
			protected BukkitTask getTask(Runnable command) {
				return scheduler.runTaskAsynchronously(plugin, command);
			}

			@Override
			protected BukkitTask getLaterTask(Runnable task, long ticks) {
				return scheduler.runTaskLaterAsynchronously(plugin, task, ticks);
			}
			
			@Override
			protected BukkitTask getTimerTask(long ticksInitial, long ticksDelay, Runnable task) {
				return scheduler.runTaskTimerAsynchronously(plugin, task, ticksInitial, ticksDelay);
			}
		};
		
		PluginDisabledListener.getListener(plugin).addService(service);
		return service;
	}

	/**
	 * Retrieve the current Bukkit scheduler.
	 * @return Current scheduler.
	 */
	private static BukkitScheduler getScheduler(Plugin plugin) {
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		
		if (scheduler != null) {
			return scheduler;
		} else {
			throw new IllegalStateException("Unable to retrieve scheduler.");
		}
	}
}
