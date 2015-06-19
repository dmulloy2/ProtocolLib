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

import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.Callable;

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

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.collect.Maps;

public abstract class ShadedChannelProxy implements Channel {
	// Mark that a certain object does not contain a message field
	private static final FieldAccessor MARK_NO_MESSAGE = new FieldAccessor() {
		@Override
        public void set(Object instance, Object value) { }
		@Override
        public Object get(Object instance) { return null; }
		@Override
        public Field getField() { return null; };
	};

	// Looking up packets in inner classes
	private static Map<Class<?>, FieldAccessor> MESSAGE_LOOKUP = Maps.newConcurrentMap();

	// The underlying channel
	protected Channel delegate;
	protected Class<?> messageClass;

	// Event loop proxy
	private transient ShadedEventLoopProxy loopProxy;

	public ShadedChannelProxy(Channel delegate, Class<?> messageClass) {
		this.delegate = delegate;
		this.messageClass = messageClass;
	}

	/**
	 * Invoked when a packet is scheduled for transmission in the event loop.
	 * @param <T> Type
	 * @param callable - callable to schedule for execution.
	 * @param packetAccessor - accessor for modifying the packet in the callable.
	 * @return The callable that will be scheduled, or NULL to cancel.
	 */
	protected abstract <T> Callable<T> onMessageScheduled(Callable<T> callable, FieldAccessor packetAccessor);

	/**
	 * Invoked when a packet is scheduled for transmission in the event loop.
	 * @param runnable - the runnable that contains a packet to be scheduled.
	 * @param packetAccessor - accessor for modifying the packet in the runnable.
	 * @return The runnable that will be scheduled, or NULL to cancel.
	 */
	protected abstract Runnable onMessageScheduled(Runnable runnable, FieldAccessor packetAccessor);

	@Override
    public <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey) {
		return delegate.attr(paramAttributeKey);
	}

	@Override
    public ChannelFuture bind(SocketAddress paramSocketAddress) {
		return delegate.bind(paramSocketAddress);
	}

	@Override
    public ChannelPipeline pipeline() {
		return delegate.pipeline();
	}

	@Override
    public ChannelFuture connect(SocketAddress paramSocketAddress) {
		return delegate.connect(paramSocketAddress);
	}

	@Override
    public ByteBufAllocator alloc() {
		return delegate.alloc();
	}

	@Override
    public ChannelPromise newPromise() {
		return delegate.newPromise();
	}

	@Override
    public EventLoop eventLoop() {
		if (loopProxy == null) {
			loopProxy = new ShadedEventLoopProxy() {
				@Override
				protected EventLoop getDelegate() {
					return delegate.eventLoop();
				}

				@Override
				protected Runnable schedulingRunnable(final Runnable runnable) {
					final FieldAccessor accessor = getMessageAccessor(runnable);

					if (accessor != null) {
						Runnable result = onMessageScheduled(runnable, accessor);;
						return result != null ? result : getEmptyRunnable();
					}
					return runnable;
				}

				@Override
				protected <T> Callable<T> schedulingCallable(Callable<T> callable) {
					FieldAccessor accessor = getMessageAccessor(callable);

					if (accessor != null) {
						Callable<T> result = onMessageScheduled(callable, accessor);;
						return result != null ? result : ShadedEventLoopProxy.<T>getEmptyCallable();
					}
					return callable;
				}
			};
		}
		return loopProxy;
	}

	/**
	 * Retrieve a way to access the packet field of an object.
	 * @param value - the object.
	 * @return The packet field accessor, or NULL if not found.
	 */
	private FieldAccessor getMessageAccessor(Object value) {
		Class<?> clazz = value.getClass();
		FieldAccessor accessor = MESSAGE_LOOKUP.get(clazz);

		if (accessor == null) {
			try {
				accessor = Accessors.getFieldAccessor(clazz, messageClass, true);
			} catch (IllegalArgumentException e) {
				accessor = MARK_NO_MESSAGE;
			}
			// Save the result
			MESSAGE_LOOKUP.put(clazz, accessor);
		}
		return accessor != MARK_NO_MESSAGE ? accessor : null;
	}

	@Override
    public ChannelFuture connect(SocketAddress paramSocketAddress1,
			SocketAddress paramSocketAddress2) {
		return delegate.connect(paramSocketAddress1, paramSocketAddress2);
	}

	@Override
    public ChannelProgressivePromise newProgressivePromise() {
		return delegate.newProgressivePromise();
	}

	@Override
    public Channel parent() {
		return delegate.parent();
	}

	@Override
    public ChannelConfig config() {
		return delegate.config();
	}

	@Override
    public ChannelFuture newSucceededFuture() {
		return delegate.newSucceededFuture();
	}

	@Override
    public boolean isOpen() {
		return delegate.isOpen();
	}

	@Override
    public ChannelFuture disconnect() {
		return delegate.disconnect();
	}

	@Override
    public boolean isRegistered() {
		return delegate.isRegistered();
	}

	@Override
    public ChannelFuture newFailedFuture(Throwable paramThrowable) {
		return delegate.newFailedFuture(paramThrowable);
	}

	@Override
    public ChannelFuture close() {
		return delegate.close();
	}

	@Override
    public boolean isActive() {
		return delegate.isActive();
	}

	@Override
    @Deprecated
	public ChannelFuture deregister() {
		return delegate.deregister();
	}

	@Override
    public ChannelPromise voidPromise() {
		return delegate.voidPromise();
	}

	@Override
    public ChannelMetadata metadata() {
		return delegate.metadata();
	}

	@Override
    public ChannelFuture bind(SocketAddress paramSocketAddress,
			ChannelPromise paramChannelPromise) {
		return delegate.bind(paramSocketAddress, paramChannelPromise);
	}

	@Override
    public SocketAddress localAddress() {
		return delegate.localAddress();
	}

	@Override
    public SocketAddress remoteAddress() {
		return delegate.remoteAddress();
	}

	@Override
    public ChannelFuture connect(SocketAddress paramSocketAddress,
			ChannelPromise paramChannelPromise) {
		return delegate.connect(paramSocketAddress, paramChannelPromise);
	}

	@Override
    public ChannelFuture closeFuture() {
		return delegate.closeFuture();
	}

	@Override
    public boolean isWritable() {
		return delegate.isWritable();
	}

	@Override
    public Channel flush() {
		return delegate.flush();
	}

	@Override
    public ChannelFuture connect(SocketAddress paramSocketAddress1,
			SocketAddress paramSocketAddress2, ChannelPromise paramChannelPromise) {
		return delegate.connect(paramSocketAddress1, paramSocketAddress2, paramChannelPromise);
	}

	@Override
    public Channel read() {
		return delegate.read();
	}

	@Override
    public Unsafe unsafe() {
		return delegate.unsafe();
	}

	@Override
    public ChannelFuture disconnect(ChannelPromise paramChannelPromise) {
		return delegate.disconnect(paramChannelPromise);
	}

	@Override
    public ChannelFuture close(ChannelPromise paramChannelPromise) {
		return delegate.close(paramChannelPromise);
	}

	@Override
    @Deprecated
	public ChannelFuture deregister(ChannelPromise paramChannelPromise) {
		return delegate.deregister(paramChannelPromise);
	}

	@Override
    public ChannelFuture write(Object paramObject) {
		return delegate.write(paramObject);
	}

	@Override
    public ChannelFuture write(Object paramObject, ChannelPromise paramChannelPromise) {
		return delegate.write(paramObject, paramChannelPromise);
	}

	@Override
    public ChannelFuture writeAndFlush(Object paramObject, ChannelPromise paramChannelPromise) {
		return delegate.writeAndFlush(paramObject, paramChannelPromise);
	}

	@Override
    public ChannelFuture writeAndFlush(Object paramObject) {
		return delegate.writeAndFlush(paramObject);
	}

	@Override
    public int compareTo(Channel o) {
		return delegate.compareTo(o);
	}
}
