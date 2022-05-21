package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;

import java.util.*;

@SuppressWarnings("NullableProblems")
final class ListeningList implements List<Object> {

	private final List<Object> original;
	private final ChannelHandler channelHandler;

	public ListeningList(List<Object> original, ChannelHandler channelHandler) {
		this.original = original;
		this.channelHandler = channelHandler;

		// no need to copy all elements of the original list, but we need to inject them
		original.forEach(this::processInsert);
	}

	@Override
	public int size() {
		return this.original.size();
	}

	@Override
	public boolean isEmpty() {
		return this.original.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.original.contains(o);
	}

	@Override
	public Iterator<Object> iterator() {
		return this.original.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.original.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		//noinspection SuspiciousToArrayCall
		return this.original.toArray(a);
	}

	@Override
	public boolean add(Object o) {
		this.processInsert(o);
		return this.original.add(o);
	}

	@Override
	public void add(int index, Object element) {
		this.processInsert(element);
		this.original.add(index, element);
	}

	@Override
	public boolean addAll(Collection<?> c) {
		c.forEach(this::processInsert);
		return this.original.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<?> c) {
		c.forEach(this::processInsert);
		return this.original.addAll(index, c);
	}

	@Override
	public Object set(int index, Object element) {
		this.processInsert(element);

		Object prev = this.original.set(index, element);
		this.processRemove(prev);

		return prev;
	}

	@Override
	public boolean remove(Object o) {
		if (this.original.remove(o)) {
			this.processRemove(o);
			return true;
		}

		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.original.containsAll(c);
	}

	@Override
	public Object remove(int index) {
		Object removed = this.original.remove(index);
		this.processRemove(removed);
		return removed;
	}

	@Override
	public int indexOf(Object o) {
		return this.original.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.original.lastIndexOf(o);
	}

	@Override
	public ListIterator<Object> listIterator() {
		return this.original.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return this.original.listIterator(index);
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return this.original.subList(fromIndex, toIndex);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		c.forEach(element -> {
			if (this.original.contains(element)) {
				this.processRemove(element);
			}
		});
		return this.original.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.original.retainAll(c);
	}

	@Override
	public void clear() {
		this.original.forEach(this::processRemove);
		this.original.clear();
	}

	@Override
	public Object get(int index) {
		return this.original.get(index);
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

	public List<Object> getOriginal() {
		return this.original;
	}

	public void unProcessAll() {
		this.original.forEach(this::processRemove);
	}
}
