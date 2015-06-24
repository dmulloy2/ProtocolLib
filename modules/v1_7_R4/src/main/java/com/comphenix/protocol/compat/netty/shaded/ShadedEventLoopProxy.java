/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.compat.netty.shaded;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.channel.EventLoop;
import net.minecraft.util.io.netty.channel.EventLoopGroup;
import net.minecraft.util.io.netty.util.concurrent.EventExecutor;
import net.minecraft.util.io.netty.util.concurrent.Future;
import net.minecraft.util.io.netty.util.concurrent.ProgressivePromise;
import net.minecraft.util.io.netty.util.concurrent.Promise;
import net.minecraft.util.io.netty.util.concurrent.ScheduledFuture;

/**
 * An event loop proxy.
 * @author Kristian.
 */
abstract class ShadedEventLoopProxy implements EventLoop {
	private static final Runnable EMPTY_RUNNABLE = new Runnable() {
		@Override
		public void run() {
			// Do nothing
		}
	};
	private static final Callable<?> EMPTY_CALLABLE = new Callable<Object>() {
		@Override
		public Object call() throws Exception {
			return null;
		};
	};

	/**
	 * Retrieve the underlying event loop.
	 * @return The event loop.
	 */
	protected abstract EventLoop getDelegate();
	
	/**
	 * Retrieve a callable that does nothing but return NULL.
	 * @return The empty callable.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Callable<T> getEmptyCallable() {
		return (Callable<T>) EMPTY_CALLABLE;
	}
	
	/**
	 * Retrieve a runnable that does nothing.
	 * @return A NO-OP runnable.
	 */
	public static Runnable getEmptyRunnable() {
		return EMPTY_RUNNABLE;
	}
	
	/**
	 * Invoked when a runnable is being scheduled.
	 * @param runnable - the runnable that is scheduling.
	 * @return The runnable to schedule instead. Cannot be NULL.
	 */
	protected abstract Runnable schedulingRunnable(Runnable runnable);

	/**
	 * Invoked when a callable is being scheduled.
	 * @param runnable - the callable that is scheduling.
	 * @return The callable to schedule instead. Cannot be NULL.
	 */
	protected abstract <T> Callable<T> schedulingCallable(Callable<T> callable);
	
	@Override
	public void execute(Runnable command) {
		getDelegate().execute(schedulingRunnable(command));
	}

	@Override
	public <T> Future<T> submit(Callable<T> action) {
		return getDelegate().submit(schedulingCallable(action));
	}

	@Override
	public <T> Future<T> submit(Runnable action, T arg1) {
		return getDelegate().submit(schedulingRunnable(action), arg1);
	}

	@Override
	public Future<?> submit(Runnable action) {
		return getDelegate().submit(schedulingRunnable(action));
	}
	
	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> action, long arg1, TimeUnit arg2) {
		return getDelegate().schedule(schedulingCallable(action), arg1, arg2);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable action, long arg1, TimeUnit arg2) {
		return getDelegate().schedule(schedulingRunnable(action), arg1, arg2);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable action, long arg1, long arg2, TimeUnit arg3) {
		return getDelegate().scheduleAtFixedRate(schedulingRunnable(action), arg1, arg2, arg3);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable action, long arg1, long arg2, TimeUnit arg3) {
		return getDelegate().scheduleWithFixedDelay(schedulingRunnable(action), arg1, arg2, arg3);
	}
	
	// Boiler plate:
	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return getDelegate().awaitTermination(timeout, unit);
	}

	@Override
	public boolean inEventLoop() {
		return getDelegate().inEventLoop();
	}

	@Override
	public boolean inEventLoop(Thread arg0) {
		return getDelegate().inEventLoop(arg0);
	}

	@Override
	public boolean isShutdown() {
		return getDelegate().isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return getDelegate().isTerminated();
	}

	@Override
	public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return getDelegate().invokeAll(tasks);
	}

	@Override
	public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
			TimeUnit unit) throws InterruptedException {
		return getDelegate().invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
			ExecutionException {
		return getDelegate().invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return getDelegate().invokeAny(tasks, timeout, unit);
	}

	@Override
	public boolean isShuttingDown() {
		return getDelegate().isShuttingDown();
	}

	@Override
	public Iterator<EventExecutor> iterator() {
		return getDelegate().iterator();
	}

	@Override
	public <V> Future<V> newFailedFuture(Throwable arg0) {
		return getDelegate().newFailedFuture(arg0);
	}

	@Override
	public EventLoop next() {
		return ((EventLoopGroup) getDelegate()).next();
	}
	
	@Override
	public <V> ProgressivePromise<V> newProgressivePromise() {
		return getDelegate().newProgressivePromise();
	}

	@Override
	public <V> Promise<V> newPromise() {
		return getDelegate().newPromise();
	}

	@Override
	public <V> Future<V> newSucceededFuture(V arg0) {
		return getDelegate().newSucceededFuture(arg0);
	}
	
	@Override
	public EventLoopGroup parent() {
		return getDelegate().parent();
	}

	@Override
	public ChannelFuture register(Channel arg0, ChannelPromise arg1) {
		return getDelegate().register(arg0, arg1);
	}

	@Override
	public ChannelFuture register(Channel arg0) {
		return getDelegate().register(arg0);
	}

	@Override
	public Future<?> shutdownGracefully() {
		return getDelegate().shutdownGracefully();
	}

	@Override
	public Future<?> shutdownGracefully(long arg0, long arg1, TimeUnit arg2) {
		return getDelegate().shutdownGracefully(arg0, arg1, arg2);
	}

	@Override
	public Future<?> terminationFuture() {
		return getDelegate().terminationFuture();
	}
	
	@Override
	@Deprecated
	public void shutdown() {
		getDelegate().shutdown();
	}
	
	@Override
	@Deprecated
	public List<Runnable> shutdownNow() {
		return getDelegate().shutdownNow();
	}
}
