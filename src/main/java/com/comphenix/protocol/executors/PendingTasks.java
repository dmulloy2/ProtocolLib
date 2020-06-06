package com.comphenix.protocol.executors;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

class PendingTasks {
	/**
	 * Represents a wrapper for a cancelable task.
	 * 
	 * @author Kristian
	 */
	private interface CancelableFuture {
		void cancel();
		boolean isTaskCancelled();
	}
	
	// Every pending task
	private final Set<CancelableFuture> pending = new HashSet<>();
	private final Object pendingLock = new Object();
	
	// Handle arbitrary cancelation
	private final Plugin plugin;
	private final BukkitScheduler scheduler;
	private BukkitTask cancellationTask;
	
	public PendingTasks(Plugin plugin, BukkitScheduler scheduler) {
		this.plugin = plugin;
		this.scheduler = scheduler;
	}
	
	public void add(final BukkitTask task, final Future<?> future) {
		add(new CancelableFuture() {
			@Override
			public boolean isTaskCancelled() {
				// If completed, check its cancellation state
				if (future.isDone())
					return future.isCancelled();
				
				return !(scheduler.isCurrentlyRunning(task.getTaskId()) || 
						 scheduler.isQueued(task.getTaskId()));
			}
			
			@Override
			public void cancel() {
				// Make sure 
				task.cancel();
				future.cancel(true);
			}
		});
	}

	private CancelableFuture add(CancelableFuture task) {
		synchronized (pendingLock) {
			pending.add(task);
			pendingLock.notifyAll();
			beginCancellationTask();
			return task;
		}
	}
	
	private void beginCancellationTask() {
		if (cancellationTask == null) {
			cancellationTask = scheduler.runTaskTimer(plugin, () -> {
				// Check for cancellations
				synchronized (pendingLock) {
					boolean changed = false;

					for (Iterator<CancelableFuture> it = pending.iterator(); it.hasNext(); ) {
						CancelableFuture future = it.next();

						// Remove cancelled tasks
						if (future.isTaskCancelled()) {
							future.cancel();
							it.remove();
							changed = true;
						}
					}

					// Notify waiting threads
					if (changed) {
						pendingLock.notifyAll();
					}
				}

				// Stop if we are out of tasks
				if (isTerminated()) {
					cancellationTask.cancel();
					cancellationTask = null;
				}
			}, 1, 1);
		}
	}

	/**
	 * Cancel all pending tasks.
	 */
	public void cancel() {
		for (CancelableFuture task : pending) {
			task.cancel();
		}
	}
	
	/**
	 * Wait until all pending tasks have completed.
	 * @param timeout - the current timeout.
	 * @param unit - unit of the timeout.
	 * @return TRUE if every pending task has terminated, FALSE if we reached the timeout.
	 * @throws InterruptedException
	 */
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		long expire = System.nanoTime() + unit.toNanos(timeout);
		
		synchronized (pendingLock) {
			// Wait until the tasks have all terminated
			while (!isTerminated()) {
				// Check timeout
				if (expire < System.nanoTime())
					return false;
				unit.timedWait(pendingLock, timeout);
			}
		}
		// Timeout!
		return false;
	}
	
	/**
	 * Determine if all tasks have completed executing.
	 * @return TRUE if they have, FALSE otherwise.
	 */
	public boolean isTerminated() {
		return pending.isEmpty();
	}
}
