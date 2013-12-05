package com.comphenix.protocol.injector.netty;

import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;

// Hopefully, CB won't version these as well
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;

class BootstrapList extends ForwardingList<Object> {
	private List<Object> delegate;
	private ChannelHandler handler;
	
	/**
	 * Construct a new bootstrap list.
	 * @param delegate - the delegate.
	 * @param handler - the channel handler to add.
	 */
	public BootstrapList(List<Object> delegate, ChannelHandler handler) {
		this.delegate = delegate;
		this.handler = handler;
		
		// Process all existing bootstraps
		for (Object item : this) {
			if (item instanceof ChannelFuture) {
				processBootstrap((ChannelFuture) item);
			}
		}
	}

	@Override
	protected List<Object> delegate() {
		return delegate;
	}
	
	@Override
	public boolean add(Object element) {
		processElement(element);
		return super.add(element);
	}
	
	@Override
	public boolean addAll(Collection<? extends Object> collection) {
		List<Object> copy = Lists.newArrayList(collection);
		
		// Process the collection before we pass it on
		for (Object element : copy) {
			processElement(element);
		}
		return super.addAll(copy);
	}
	
	@Override
	public Object set(int index, Object element) {
		Object old = super.set(index, element);
		
		// Handle the old future, and the newly inserted future
		if (old != element) {
			unprocessElement(old);
			processElement(element);
		}
		return old;
	}
	
	/**
	 * Process a single element.
	 * @param element - the element.
	 */
	protected void processElement(Object element) {
		if (element instanceof ChannelFuture) {
			processBootstrap((ChannelFuture) element);
		}
	}
	
	/**
	 * Unprocess a single element.
	 * @param element - the element to unprocess.
	 */
	protected void unprocessElement(Object element) {
		if (element instanceof ChannelFuture) {
			unprocessBootstrap((ChannelFuture) element);
		}
	}
	
	/**
	 * Process a single channel future.
	 * @param future - the future.
	 */
	protected void processBootstrap(ChannelFuture future) {
		// Important: Must be addFirst()
		future.channel().pipeline().addFirst(handler);
	}
	
	/**
	 * Revert any changes we made to the channel future.
	 * @param future - the future.
	 */
	protected void unprocessBootstrap(ChannelFuture future) {
		try {
			future.channel().pipeline().remove(handler);
		} catch (NoSuchElementException e) {
			// Whatever
		}
	}
	
	/**
	 * Close and revert all changes.
	 */
	public void close() {
		for (Object element : this)
			unprocessElement(element);
	}
}
