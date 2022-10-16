package com.comphenix.protocol.injector.netty.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ProgressivePromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * An abstract event loop implementation which delegates all calls to a given event loop, but proxies all calls which
 * schedule something on the event loop to methods which decide what should happen to the scheduled task.
 */
abstract class NettyEventLoopProxy implements EventLoop {

	private static final Callable<?> EMPTY_CALLABLE = () -> null;
	private static final Runnable EMPTY_RUNNABLE = () -> {
	};

	private final EventLoop delegate;
	private final NettyChannelInjector injector;

	public NettyEventLoopProxy(EventLoop delegate, NettyChannelInjector injector) {
		this.delegate = delegate;
		this.injector = injector;
	}

	private Runnable proxyRunnable(Runnable original) {
		// execute the proxy and check if we need to do anything
		Runnable proxied = this.doProxyRunnable(original);
		if (proxied != null && proxied == original) {
			// was not changed, we need to mark the packet as processed manually
			return () -> {
				this.injector.processedPackets.set(Boolean.TRUE);
				original.run();
			};
		} else {
			// either the action was not executed, or the proxy will set the packet as processes
			return proxied;
		}
	}

	private <T> Callable<T> proxyCallable(Callable<T> original) {
		// execute the proxy and check if we need to do anything
		Callable<T> proxied = this.doProxyCallable(original);
		if (proxied != null && proxied == original) {
			// was not changed, we need to mark the packet as processed manually
			return () -> {
				this.injector.processedPackets.set(Boolean.TRUE);
				return proxied.call();
			};
		} else {
			// either the action was not executed, or the proxy will set the packet as processes
			return proxied;
		}
	}

	/**
	 * Proxies the given runnable. The returned runnable will be executed instead of the original. If this method returns
	 * null a no-op runnable will be scheduled instead, preventing the original action from happening.
	 *
	 * @param original the runnable to proxy.
	 * @return the runnable to execute instead, null to execute no action.
	 */
	protected abstract Runnable doProxyRunnable(Runnable original);

	/**
	 * Proxies the given callable. The returned callable will be executed instead of the original. If this method returns
	 * null a callable which always returns null will be scheduled instead, preventing the original action from
	 * happening.
	 *
	 * @param original the callable to proxy.
	 * @param <T>      the return type of the original callable.
	 * @return the callable to execute instead of the original, null to use a no-op callable instead.
	 */
	protected abstract <T> Callable<T> doProxyCallable(Callable<T> original);

	@Override
	public EventLoopGroup parent() {
		return this.delegate.parent();
	}

	@Override
	public EventLoop next() {
		return this.delegate.next();
	}

	@Override
	public boolean inEventLoop() {
		return this.delegate.inEventLoop();
	}

	@Override
	public boolean inEventLoop(Thread thread) {
		return this.delegate.inEventLoop(thread);
	}

	@Override
	public <V> Promise<V> newPromise() {
		return this.delegate.newPromise();
	}

	@Override
	public <V> ProgressivePromise<V> newProgressivePromise() {
		return this.delegate.newProgressivePromise();
	}

	@Override
	public <V> Future<V> newSucceededFuture(V result) {
		return this.delegate.newSucceededFuture(result);
	}

	@Override
	public <V> Future<V> newFailedFuture(Throwable cause) {
		return this.delegate.newFailedFuture(cause);
	}

	@Override
	public boolean isShuttingDown() {
		return this.delegate.isShuttingDown();
	}

	@Override
	public Future<?> shutdownGracefully() {
		return this.delegate.shutdownGracefully();
	}

	@Override
	public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
		return this.delegate.shutdownGracefully(quietPeriod, timeout, unit);
	}

	@Override
	public Future<?> terminationFuture() {
		return this.delegate.terminationFuture();
	}

	@Override
	@Deprecated
	public void shutdown() {
		this.delegate.shutdown();
	}

	@Override
	@Deprecated
	public List<Runnable> shutdownNow() {
		return this.delegate.shutdownNow();
	}

	@Override
	public Iterator<EventExecutor> iterator() {
		return this.delegate.iterator();
	}

	@Override
	public Future<?> submit(Runnable task) {
		Runnable proxied = this.proxyRunnable(task);
		return this.delegate.submit(proxied == null ? EMPTY_RUNNABLE : proxied);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		Runnable proxied = this.proxyRunnable(task);
		return this.delegate.submit(proxied == null ? EMPTY_RUNNABLE : proxied, result);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Future<T> submit(Callable<T> task) {
		Callable<T> proxied = this.proxyCallable(task);
		return this.delegate.submit(proxied == null ? (Callable<T>) EMPTY_CALLABLE : proxied);
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		Runnable proxied = this.proxyRunnable(command);
		return this.delegate.schedule(proxied == null ? EMPTY_RUNNABLE : proxied, delay, unit);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		Callable<V> proxied = this.proxyCallable(callable);
		return this.delegate.schedule(callable == null ? (Callable<V>) EMPTY_CALLABLE : proxied, delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		Runnable proxied = this.proxyRunnable(command);
		return this.delegate.scheduleAtFixedRate(proxied == null ? EMPTY_RUNNABLE : proxied, initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		Runnable proxied = this.proxyRunnable(command);
		return this.delegate.scheduleWithFixedDelay(proxied == null ? EMPTY_RUNNABLE : proxied, initialDelay, delay, unit);
	}

	@Override
	public boolean isShutdown() {
		return this.delegate.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return this.delegate.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.delegate.awaitTermination(timeout, unit);
	}

	@Override
	public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		return this.delegate.invokeAll(tasks);
	}

	@Override
	public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
			TimeUnit unit) throws InterruptedException {
		return this.delegate.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return this.delegate.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.delegate.invokeAny(tasks, timeout, unit);
	}

	@Override
	public void execute(Runnable command) {
		Runnable proxied = this.proxyRunnable(command);
		if (proxied != null) {
			this.delegate.execute(proxied);
		}
	}

	@Override
	public void forEach(Consumer<? super EventExecutor> action) {
		this.delegate.forEach(action);
	}

	@Override
	public Spliterator<EventExecutor> spliterator() {
		return this.delegate.spliterator();
	}

	@Override
	public ChannelFuture register(Channel channel) {
		return this.delegate.register(channel);
	}

	@Override
	public ChannelFuture register(Channel channel, ChannelPromise promise) {
		return this.delegate.register(channel, promise);
	}
}
