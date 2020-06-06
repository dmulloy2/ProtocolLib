package com.comphenix.protocol.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.comphenix.protocol.executors.AbstractListeningService.RunnableAbstractFuture;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableScheduledFuture;

class CallableTask<T> extends RunnableAbstractFuture<T> {
	protected final Callable<T> compute;
	
	public CallableTask(Callable<T> compute) {
		Preconditions.checkNotNull(compute, "compute cannot be NULL");
		
		this.compute = compute;
	}
			
	public ListenableScheduledFuture<T> getScheduledFuture(final long startTime, final long nextDelay) {
		return new ListenableScheduledFuture<T>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return CallableTask.this.cancel(mayInterruptIfRunning);
			}
			
			@Override
			public T get() throws InterruptedException, ExecutionException {
				return CallableTask.this.get();
			}
			
			@Override
			public T get(long timeout, TimeUnit unit) throws InterruptedException,
					ExecutionException, TimeoutException {
				return CallableTask.this.get(timeout, unit);
			}
			
			@Override
			public boolean isCancelled() {
				return CallableTask.this.isCancelled();
			}
			
			@Override
			public boolean isDone() {
				return CallableTask.this.isDone();
			}
			
			@Override
			public void addListener(Runnable listener, Executor executor) {
				CallableTask.this.addListener(listener, executor);
			}
			
			@Override
			public int compareTo(Delayed o) {
				return Long.valueOf(getDelay(TimeUnit.NANOSECONDS))
						.compareTo(o.getDelay(TimeUnit.NANOSECONDS));
			}
			
			@Override
			public long getDelay(TimeUnit unit) {
				long current = System.nanoTime();
				
				// Calculate the correct delay
				if (current < startTime || !isPeriodic())
					return unit.convert(startTime - current, TimeUnit.NANOSECONDS);
				else
					return unit.convert(((current - startTime) % nextDelay), TimeUnit.NANOSECONDS);
			}
		
			// @Override
			public boolean isPeriodic() {
				return nextDelay > 0;
			}
		};
	}
	
	/**
	 * Invoked by the thread responsible for computing this future.
	 */
	protected void compute() {
		try {
			// Save result
			if (!isCancelled()) {
				set(compute.call());
			}
		} catch (Throwable e) {
			setException(e);
		}
	}

	@Override
	public void run() {
		compute();
	}
}