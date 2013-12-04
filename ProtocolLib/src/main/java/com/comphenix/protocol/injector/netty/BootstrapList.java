package com.comphenix.protocol.injector.netty;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;

// Hopefully, CB won't version these as well
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;

class BootstrapList extends ForwardingList<ChannelFuture> {
	private List<ChannelFuture> delegate;
	private ChannelHandler handler;
	
	/**
	 * Construct a new bootstrap list.
	 * @param delegate - the delegate.
	 * @param handler - the channel handler to add.
	 */
	public BootstrapList(List<ChannelFuture> delegate, ChannelHandler handler) {
		this.delegate = delegate;
		this.handler = handler;
		
		// Process all existing bootstraps
		for (ChannelFuture future : this)
			processBootstrap(future);
	}

	@Override
	protected List<ChannelFuture> delegate() {
		return delegate;
	}
	
	@Override
	public boolean add(ChannelFuture element) {
		processBootstrap(element);
		return super.add(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends ChannelFuture> collection) {
		List<? extends ChannelFuture> copy = Lists.newArrayList(collection);
		
		// Process the collection before we pass it on
		for (ChannelFuture future : copy) {
			processBootstrap(future);
		}
		return super.addAll(copy);
	}
	
	@Override
	public ChannelFuture set(int index, ChannelFuture element) {
		ChannelFuture old = super.set(index, element);
		
		// Handle the old future, and the newly inserted future
		if (old != element) {
			if (old != null) {
				unprocessBootstrap(old);
			}
			if (element != null) {
				processBootstrap(element);
			}
		}
		return old;
	}
	
	/**
	 * Process a single channel future.
	 * @param future - the future.
	 */
	protected void processBootstrap(ChannelFuture future) {
		future.channel().pipeline().addLast(handler);
	}
	
	/**
	 * Revert any changes we made to the channel future.
	 * @param future - the future.
	 */
	protected void unprocessBootstrap(ChannelFuture future) {
		future.channel().pipeline().remove(handler);
	}
	
	/**
	 * Close and revert all changes.
	 */
	public void close() {
		for (ChannelFuture future : this)
			unprocessBootstrap(future);
	}
}
