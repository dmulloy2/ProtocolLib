package com.comphenix.protocol.injector.netty;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;

import net.minecraft.util.io.netty.buffer.ByteBufAllocator;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelConfig;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelMetadata;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelProgressivePromise;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.channel.EventLoop;
import net.minecraft.util.io.netty.util.Attribute;
import net.minecraft.util.io.netty.util.AttributeKey;
import net.minecraft.util.io.netty.util.concurrent.EventExecutor;

abstract class ChannelProxy implements Channel {
	private static Constructor<? extends ChannelFuture> FUTURE_CONSTRUCTOR;
	
	// The underlying channel
	private Channel delegate;

	public ChannelProxy(Channel delegate) {
		this.delegate = delegate;
	}

	/**
	 * Invoked when a packet is being transmitted.
	 * @param message - the packet to transmit.
	 * @return The object to transmit.
	 */
	protected abstract Object onMessageWritten(Object message);
	
	/**
	 * The future we return when packets are being cancelled.
	 * @return A succeeded future.
	 */
	protected ChannelFuture getSucceededFuture() {
		try {
			if (FUTURE_CONSTRUCTOR == null) {
				@SuppressWarnings("unchecked")
				Class<? extends ChannelFuture> succededFuture = 
				(Class<? extends ChannelFuture>) ChannelProxy.class.getClassLoader().
					loadClass("net.minecraft.util.io.netty.channel.SucceededChannelFuture");
				
				FUTURE_CONSTRUCTOR = succededFuture.getConstructor(Channel.class, EventExecutor.class);
			}		
			return FUTURE_CONSTRUCTOR.newInstance(this, null);
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot get succeeded future.");
		} catch (Exception e) {
			throw new RuntimeException("Cannot construct completed future.");
		}
	}
	
	public <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey) {
		return delegate.attr(paramAttributeKey);
	}

	public ChannelFuture bind(SocketAddress paramSocketAddress) {
		return delegate.bind(paramSocketAddress);
	}

	public ChannelPipeline pipeline() {
		return delegate.pipeline();
	}

	public ChannelFuture connect(SocketAddress paramSocketAddress) {
		return delegate.connect(paramSocketAddress);
	}

	public ByteBufAllocator alloc() {
		return delegate.alloc();
	}

	public ChannelPromise newPromise() {
		return delegate.newPromise();
	}

	public EventLoop eventLoop() {
		return delegate.eventLoop();
	}

	public ChannelFuture connect(SocketAddress paramSocketAddress1,
			SocketAddress paramSocketAddress2) {
		return delegate.connect(paramSocketAddress1, paramSocketAddress2);
	}

	public ChannelProgressivePromise newProgressivePromise() {
		return delegate.newProgressivePromise();
	}

	public Channel parent() {
		return delegate.parent();
	}

	public ChannelConfig config() {
		return delegate.config();
	}

	public ChannelFuture newSucceededFuture() {
		return delegate.newSucceededFuture();
	}

	public boolean isOpen() {
		return delegate.isOpen();
	}

	public ChannelFuture disconnect() {
		return delegate.disconnect();
	}

	public boolean isRegistered() {
		return delegate.isRegistered();
	}

	public ChannelFuture newFailedFuture(Throwable paramThrowable) {
		return delegate.newFailedFuture(paramThrowable);
	}

	public ChannelFuture close() {
		return delegate.close();
	}

	public boolean isActive() {
		return delegate.isActive();
	}

	@Deprecated
	public ChannelFuture deregister() {
		return delegate.deregister();
	}

	public ChannelPromise voidPromise() {
		return delegate.voidPromise();
	}

	public ChannelMetadata metadata() {
		return delegate.metadata();
	}

	public ChannelFuture bind(SocketAddress paramSocketAddress,
			ChannelPromise paramChannelPromise) {
		return delegate.bind(paramSocketAddress, paramChannelPromise);
	}

	public SocketAddress localAddress() {
		return delegate.localAddress();
	}

	public SocketAddress remoteAddress() {
		return delegate.remoteAddress();
	}

	public ChannelFuture connect(SocketAddress paramSocketAddress,
			ChannelPromise paramChannelPromise) {
		return delegate.connect(paramSocketAddress, paramChannelPromise);
	}

	public ChannelFuture closeFuture() {
		return delegate.closeFuture();
	}

	public boolean isWritable() {
		return delegate.isWritable();
	}

	public Channel flush() {
		return delegate.flush();
	}

	public ChannelFuture connect(SocketAddress paramSocketAddress1,
			SocketAddress paramSocketAddress2, ChannelPromise paramChannelPromise) {
		return delegate.connect(paramSocketAddress1, paramSocketAddress2, paramChannelPromise);
	}

	public Channel read() {
		return delegate.read();
	}

	public Unsafe unsafe() {
		return delegate.unsafe();
	}

	public ChannelFuture disconnect(ChannelPromise paramChannelPromise) {
		return delegate.disconnect(paramChannelPromise);
	}

	public ChannelFuture close(ChannelPromise paramChannelPromise) {
		return delegate.close(paramChannelPromise);
	}

	@Deprecated
	public ChannelFuture deregister(ChannelPromise paramChannelPromise) {
		return delegate.deregister(paramChannelPromise);
	}

	public ChannelFuture write(Object message) {
		Object result = onMessageWritten(message);
		
		if (result != null)
			return delegate.write(result);
		return getSucceededFuture();
	}

	public ChannelFuture write(Object message, ChannelPromise paramChannelPromise) {
		Object result = onMessageWritten(message);
		
		if (result != null)
			return delegate.write(message, paramChannelPromise);
		return getSucceededFuture();
	}

	public ChannelFuture writeAndFlush(Object message, ChannelPromise paramChannelPromise) {
		Object result = onMessageWritten(message);
		
		if (result != null)
			return delegate.writeAndFlush(message, paramChannelPromise);
		return getSucceededFuture();
	}

	public ChannelFuture writeAndFlush(Object message) {
		Object result = onMessageWritten(message);
		
		if (result != null)
			return delegate.writeAndFlush(message);
		return getSucceededFuture();
	}

	public int compareTo(Channel o) {
		return delegate.compareTo(o);
	}
}
