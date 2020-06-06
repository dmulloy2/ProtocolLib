package com.comphenix.protocol.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

/**
 * Represents a listening scheduler service that returns {@link ListenableScheduledFuture} instead of {@link ScheduledFuture}.
 * @author Kristian
 */
public interface BukkitScheduledExecutorService extends ListeningScheduledExecutorService {
	@Override
	public ListenableScheduledFuture<?> schedule(
			Runnable command, long delay, TimeUnit unit);

	@Override
	public <V> ListenableScheduledFuture<V> schedule(
			Callable<V> callable, long delay, TimeUnit unit);

	@Override
	public ListenableScheduledFuture<?> scheduleAtFixedRate(
			Runnable command, long initialDelay, long period, TimeUnit unit);

	/**
	 * This is not supported by the underlying Bukkit scheduler.
	 */
	@Override
	@Deprecated
	public ListenableScheduledFuture<?> scheduleWithFixedDelay(
			Runnable command, long initialDelay, long delay, TimeUnit unit);
}