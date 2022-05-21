package com.comphenix.protocol.injector.netty.channel;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;

/**
 * A netty channel which has all methods delegated to another given channel except for the event loop which is proxied
 * separately. This class can not extend from AbstractChannel as the {@code newUnsafe()} method is protected and can
 * therefore not be called in the delegate channel.
 */
final class NettyChannelProxy implements Channel {

	private final Channel delegate;
	private final EventLoop eventLoop;

	public NettyChannelProxy(Channel delegate, EventLoop eventLoop) {
		this.delegate = delegate;
		this.eventLoop = eventLoop;
	}

	@Override
	public EventLoop eventLoop() {
		return this.eventLoop;
	}

	@Override
	public Channel parent() {
		return this.delegate.parent();
	}

	@Override
	public ChannelConfig config() {
		return this.delegate.config();
	}

	@Override
	public boolean isOpen() {
		return this.delegate.isOpen();
	}

	@Override
	public boolean isRegistered() {
		return this.delegate.isRegistered();
	}

	@Override
	public boolean isActive() {
		return this.delegate.isActive();
	}

	@Override
	public ChannelMetadata metadata() {
		return this.delegate.metadata();
	}

	@Override
	public SocketAddress localAddress() {
		return this.delegate.localAddress();
	}

	@Override
	public SocketAddress remoteAddress() {
		return this.delegate.remoteAddress();
	}

	@Override
	public ChannelFuture closeFuture() {
		return this.delegate.closeFuture();
	}

	@Override
	public boolean isWritable() {
		return this.delegate.isWritable();
	}

	@Override
	public Unsafe unsafe() {
		return this.delegate.unsafe();
	}

	@Override
	public ChannelPipeline pipeline() {
		return this.delegate.pipeline();
	}

	@Override
	public ByteBufAllocator alloc() {
		return this.delegate.alloc();
	}

	@Override
	public ChannelPromise newPromise() {
		return this.delegate.newPromise();
	}

	@Override
	public ChannelProgressivePromise newProgressivePromise() {
		return this.delegate.newProgressivePromise();
	}

	@Override
	public ChannelFuture newSucceededFuture() {
		return this.delegate.newSucceededFuture();
	}

	@Override
	public ChannelFuture newFailedFuture(Throwable cause) {
		return this.delegate.newFailedFuture(cause);
	}

	@Override
	public ChannelPromise voidPromise() {
		return this.delegate.voidPromise();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress) {
		return this.delegate.bind(localAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress) {
		return this.delegate.connect(remoteAddress);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
		return this.delegate.connect(remoteAddress, localAddress);
	}

	@Override
	public ChannelFuture disconnect() {
		return this.delegate.disconnect();
	}

	@Override
	public ChannelFuture close() {
		return this.delegate.close();
	}

	@Override
	public ChannelFuture deregister() {
		return this.delegate.deregister();
	}

	@Override
	public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
		return this.delegate.bind(localAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
		return this.delegate.connect(remoteAddress, promise);
	}

	@Override
	public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress,
			ChannelPromise promise) {
		return this.delegate.connect(remoteAddress, localAddress, promise);
	}

	@Override
	public ChannelFuture disconnect(ChannelPromise promise) {
		return this.delegate.disconnect(promise);
	}

	@Override
	public ChannelFuture close(ChannelPromise promise) {
		return this.delegate.close(promise);
	}

	@Override
	public ChannelFuture deregister(ChannelPromise promise) {
		return this.delegate.deregister(promise);
	}

	@Override
	public Channel read() {
		return this.delegate.read();
	}

	@Override
	public ChannelFuture write(Object msg) {
		return this.delegate.write(msg);
	}

	@Override
	public ChannelFuture write(Object msg, ChannelPromise promise) {
		return this.delegate.write(msg, promise);
	}

	@Override
	public Channel flush() {
		return this.delegate.flush();
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
		return this.delegate.writeAndFlush(msg, promise);
	}

	@Override
	public ChannelFuture writeAndFlush(Object msg) {
		return this.delegate.writeAndFlush(msg);
	}

	@Override
	public <T> Attribute<T> attr(AttributeKey<T> key) {
		return this.delegate.attr(key);
	}

	@Override
	public int compareTo(@NotNull Channel o) {
		return this.delegate.compareTo(o);
	}
}
