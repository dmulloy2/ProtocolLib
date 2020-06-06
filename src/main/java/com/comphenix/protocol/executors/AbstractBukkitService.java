package com.comphenix.protocol.executors;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ListenableScheduledFuture;

import org.bukkit.scheduler.BukkitTask;

abstract class AbstractBukkitService 
	extends AbstractListeningService implements BukkitScheduledExecutorService {

	private static final long MILLISECONDS_PER_TICK = 50;
	private static final long NANOSECONDS_PER_TICK = 1000000 * MILLISECONDS_PER_TICK;
	
	private volatile boolean shutdown;
	private final PendingTasks tasks;
	
	public AbstractBukkitService(PendingTasks tasks) {
		this.tasks = tasks;
	}
	
	@Override
	protected <T> RunnableAbstractFuture<T> newTaskFor(Runnable runnable, T value) {
		return newTaskFor(Executors.callable(runnable, value));
	}
	
	@Override
	protected <T> RunnableAbstractFuture<T> newTaskFor(final Callable<T> callable) {
		validateState();
		return new CallableTask<T>(callable);
	}
	
	@Override
	public void execute(Runnable command) {
		validateState();
		
		if (command instanceof RunnableFuture) {
			tasks.add(getTask(command), (Future<?>) command);
		} else {
			// Submit it first
			submit(command);
		}
	}
	
	// Bridge to Bukkit
	protected abstract BukkitTask getTask(Runnable command);
	protected abstract BukkitTask getLaterTask(Runnable task, long ticks);
	protected abstract BukkitTask getTimerTask(long ticksInitial, long ticksDelay, Runnable task);
	
	@Override
	public List<Runnable> shutdownNow() {
		shutdown();
		tasks.cancel();
		
		// We don't support this
		return Collections.emptyList();
	}
	
	@Override
	public void shutdown() {
		shutdown = true;
	}
	
	private void validateState() {
		if (shutdown) {
			throw new RejectedExecutionException("Executor service has shut down. Cannot start new tasks.");
		}
	}

	private long toTicks(long delay, TimeUnit unit) {
		return Math.round(unit.toMillis(delay) / (double)MILLISECONDS_PER_TICK);
	}
	
	@Override
	public ListenableScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return schedule(Executors.callable(command), delay, unit);
	}

	@Override
	public <V> ListenableScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		long ticks = toTicks(delay, unit);
		
		// Construct future task and Bukkit task
		CallableTask<V> task = new CallableTask<V>(callable);
		BukkitTask bukkitTask = getLaterTask(task, ticks);
		
		tasks.add(bukkitTask, task);
		return task.getScheduledFuture(System.nanoTime() + delay * NANOSECONDS_PER_TICK, 0);
	}

	@Override
	public ListenableScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,
			long period, TimeUnit unit) {

		long ticksInitial = toTicks(initialDelay, unit);
		long ticksDelay = toTicks(period, unit);
		
		// Construct future task and Bukkit task
		CallableTask<?> task = new CallableTask<Object>(Executors.callable(command)) {
			protected void compute() {
				// Do nothing more. This future can only be finished by cancellation
				try {
					compute.call();
				} catch (Exception e) {
					// Let Bukkit handle this
					throw Throwables.propagate(e);
				}
			}
		};
		BukkitTask bukkitTask = getTimerTask(ticksInitial, ticksDelay, task);
		
		tasks.add(bukkitTask, task);
		return task.getScheduledFuture(
				System.nanoTime() + ticksInitial * NANOSECONDS_PER_TICK, 
				ticksDelay * NANOSECONDS_PER_TICK);
	}

	// Not supported!
	@Deprecated
	@Override
	public ListenableScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return scheduleAtFixedRate(command, initialDelay, delay, unit);
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return tasks.awaitTermination(timeout, unit);
	}

	@Override
	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public boolean isTerminated() {
		return tasks.isTerminated();
	}
}