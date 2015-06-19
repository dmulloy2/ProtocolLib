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
import java.util.ListIterator;
import java.util.concurrent.Callable;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;

import com.google.common.collect.Lists;

public class ShadedBootstrapList implements List<Object> {
	private List<Object> delegate;
	private ChannelHandler handler;
	
	/**
	 * Construct a new bootstrap list.
	 * @param delegate - the delegate.
	 * @param handler - the channel handler to add.
	 */
	public ShadedBootstrapList(List<Object> delegate, ChannelHandler handler) {
		this.delegate = delegate;
		this.handler = handler;
		
		// Process all existing bootstraps
		for (Object item : this) {
			processElement(item);
		}
	}

	@Override
	public synchronized boolean add(Object element) {
		processElement(element);
		return delegate.add(element);
	}
	
	@Override
	public synchronized boolean addAll(Collection<? extends Object> collection) {
		List<Object> copy = Lists.newArrayList(collection);
		
		// Process the collection before we pass it on
		for (Object element : copy) {
			processElement(element);
		}
		return delegate.addAll(copy);
	}
	
	@Override
	public synchronized Object set(int index, Object element) {
		Object old = delegate.set(index, element);
		
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
		final Channel channel = future.channel();
		
		// For thread safety - see ChannelInjector.close()
		channel.eventLoop().submit(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				channel.pipeline().remove(handler);
				return null;
			}
		});
	}
	
	/**
	 * Close and revert all changes.
	 */
	public synchronized void close() {
		for (Object element : this)
			unprocessElement(element);
	}

	// Boiler plate
	@Override
	public synchronized int size() {
		return delegate.size();
	}

	@Override
	public synchronized boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return delegate.contains(o);
	}

	@Override
	public synchronized Iterator<Object> iterator() {
		return delegate.iterator();
	}

	@Override
	public synchronized Object[] toArray() {
		return delegate.toArray();
	}

	@Override
	public synchronized <T> T[] toArray(T[] a) {
		return delegate.toArray(a);
	}

	@Override
	public synchronized boolean remove(Object o) {
		return delegate.remove(o);
	}

	@Override
	public synchronized boolean containsAll(Collection<?> c) {
		return delegate.containsAll(c);
	}

	@Override
	public synchronized boolean addAll(int index, Collection<? extends Object> c) {
		return delegate.addAll(index, c);
	}

	@Override
	public synchronized boolean removeAll(Collection<?> c) {
		return delegate.removeAll(c);
	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {
		return delegate.retainAll(c);
	}

	@Override
	public synchronized void clear() {
		delegate.clear();
	}

	@Override
	public synchronized Object get(int index) {
		return delegate.get(index);
	}

	@Override
	public synchronized void add(int index, Object element) {
		delegate.add(index, element);
	}

	@Override
	public synchronized Object remove(int index) {
		return delegate.remove(index);
	}

	@Override
	public synchronized int indexOf(Object o) {
		return delegate.indexOf(o);
	}

	@Override
	public synchronized int lastIndexOf(Object o) {
		return delegate.lastIndexOf(o);
	}

	@Override
	public synchronized ListIterator<Object> listIterator() {
		return delegate.listIterator();
	}

	@Override
	public synchronized ListIterator<Object> listIterator(int index) {
		return delegate.listIterator(index);
	}

	@Override
	public synchronized List<Object> subList(int fromIndex, int toIndex) {
		return delegate.subList(fromIndex, toIndex);
	}
	// End boiler plate
}
