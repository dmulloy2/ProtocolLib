package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

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
        synchronized (this) {
            return this.original.size();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (this) {
            return this.original.isEmpty();
        }
    }

    @Override
    public boolean contains(Object o) {
        synchronized (this) {
            return this.original.contains(o);
        }
    }

    @Override
    public Iterator<Object> iterator() {
        return this.original.iterator();
    }

    @Override
    public void forEach(Consumer<? super Object> action) {
        synchronized (this) {
            this.original.forEach(action);
        }
    }

    @Override
    public Object[] toArray() {
        synchronized (this) {
            return this.original.toArray();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        synchronized (this) {
            //noinspection SuspiciousToArrayCall
            return this.original.toArray(a);
        }
    }

    @Override
    public boolean add(Object o) {
        synchronized (this) {
            this.processInsert(o);
            return this.original.add(o);
        }
    }

    @Override
    public void add(int index, Object element) {
        synchronized (this) {
            this.processInsert(element);
            this.original.add(index, element);
        }
    }

    @Override
    public boolean addAll(Collection<?> c) {
        synchronized (this) {
            c.forEach(this::processInsert);
            return this.original.addAll(c);
        }
    }

    @Override
    public boolean addAll(int index, Collection<?> c) {
        synchronized (this) {
            c.forEach(this::processInsert);
            return this.original.addAll(index, c);
        }
    }

    @Override
    public Object set(int index, Object element) {
        synchronized (this) {
            this.processInsert(element);

            Object prev = this.original.set(index, element);
            this.processRemove(prev);

            return prev;
        }
    }

    @Override
    public boolean remove(Object o) {
        synchronized (this) {
            if (this.original.remove(o)) {
                this.processRemove(o);
                return true;
            }

            return false;
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        synchronized (this) {
            //noinspection SlowListContainsAll
            return this.original.containsAll(c);
        }
    }

    @Override
    public Object remove(int index) {
        synchronized (this) {
            Object removed = this.original.remove(index);
            this.processRemove(removed);
            return removed;
        }
    }

    @Override
    public int indexOf(Object o) {
        synchronized (this) {
            return this.original.indexOf(o);
        }
    }

    @Override
    public int lastIndexOf(Object o) {
        synchronized (this) {
            return this.original.lastIndexOf(o);
        }
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
        synchronized (this) {
            return this.original.subList(fromIndex, toIndex);
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        synchronized (this) {
            c.forEach(element -> {
                if (this.original.contains(element)) {
                    this.processRemove(element);
                }
            });
            return this.original.removeAll(c);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Object> filter) {
        synchronized (this) {
            return this.original.removeIf(object -> {
                boolean shouldRemove = filter.test(object);
                if (shouldRemove) {
                    this.processRemove(object);
                }
                return shouldRemove;
            });
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        synchronized (this) {
            return this.original.retainAll(c);
        }
    }

    @Override
    public void replaceAll(UnaryOperator<Object> operator) {
        synchronized (this) {
            this.original.replaceAll(value -> {
                Object newValue = operator.apply(value);
                if (newValue != value) {
                    this.processRemove(value);
                    this.processInsert(newValue);
                }
                return newValue;
            });
        }
    }

    @Override
    public void sort(Comparator<? super Object> c) {
        synchronized (this) {
            this.original.sort(c);
        }
    }

    @Override
    public void clear() {
        synchronized (this) {
            this.original.forEach(this::processRemove);
            this.original.clear();
        }
    }

    @Override
    public Object get(int index) {
        synchronized (this) {
            return this.original.get(index);
        }
    }

    private void processInsert(Object element) {
        if (element instanceof ChannelFuture channelFuture) {
            Channel channel = channelFuture.channel();
            channel.eventLoop().execute(() -> channel.pipeline().addFirst(this.channelHandler));
        }
    }

    private void processRemove(Object element) {
        if (element instanceof ChannelFuture channelFuture) {
            Channel channel = channelFuture.channel();
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
