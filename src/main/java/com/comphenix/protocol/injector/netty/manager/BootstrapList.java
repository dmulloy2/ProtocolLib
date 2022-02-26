package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

final class BootstrapList extends ArrayList<Object> {

	private final ChannelHandler channelHandler;

	public BootstrapList(ChannelHandler channelHandler, List<Object> originalList) {
		this.channelHandler = channelHandler;
		this.addAll(originalList);
	}

	@Override
	public boolean add(Object o) {
		this.processInsert(o);
		return super.add(o);
	}

	@Override
	public void add(int index, Object element) {
		this.processInsert(element);
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<?> c) {
		c.forEach(this::processInsert);
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<?> c) {
		c.forEach(this::processInsert);
		return super.addAll(index, c);
	}

	@Override
	public Object set(int index, Object element) {
		this.processInsert(element);

		Object prev = super.set(index, element);
		this.processRemove(prev);

		return prev;
	}

	@Override
	public boolean remove(Object o) {
		if (super.remove(o)) {
			this.processRemove(o);
			return true;
		}

		return false;
	}

	@Override
	public Object remove(int index) {
		Object removed = super.remove(index);
		this.processRemove(removed);
		return removed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		c.forEach(element -> {
			if (this.contains(element)) {
				this.processRemove(element);
			}
		});
		return super.removeAll(c);
	}

	private void processInsert(Object element) {
		if (element instanceof ChannelFuture) {
			Channel channel = ((ChannelFuture) element).channel();
			channel.eventLoop().execute(() -> channel.pipeline().addFirst(this.channelHandler));
		}
	}

	private void processRemove(Object element) {
		if (element instanceof ChannelFuture) {
			Channel channel = ((ChannelFuture) element).channel();
			channel.eventLoop().execute(() -> {
				try {
					channel.pipeline().remove(this.channelHandler);
				} catch (NoSuchElementException ignored) {
					// probably already removed?
				}
			});
		}
	}
}
