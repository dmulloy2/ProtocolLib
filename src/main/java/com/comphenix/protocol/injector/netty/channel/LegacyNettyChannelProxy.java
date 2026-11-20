package com.comphenix.protocol.injector.netty.channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;

/**
 * Channel proxy for netty 4.0 (Minecraft 1.8 - 1.11).
 * <p>
 * {@link NettyChannelProxy} is compiled against the current netty version, where {@code Channel} extends
 * {@code ChannelOutboundInvoker}. The compiler therefore generates bridge methods returning that interface, and netty
 * 4.0 does not have it, so the class fails verification there. A dynamic proxy implements the {@code Channel}
 * interface of the netty version actually present at runtime instead. It is only used when needed, the compiled proxy
 * is faster.
 * <p>
 * Nothing in this class may load {@link NettyChannelProxy} while running on netty 4.0.
 */
final class LegacyNettyChannelProxy implements InvocationHandler {

    private static final boolean REQUIRED = !hasChannelOutboundInvoker();

    private final Channel delegate;
    private final EventLoop eventLoop;
    private final NettyChannelInjector injector;

    private LegacyNettyChannelProxy(Channel delegate, EventLoop eventLoop, NettyChannelInjector injector) {
        this.delegate = delegate;
        this.eventLoop = eventLoop;
        this.injector = injector;
    }

    private static boolean hasChannelOutboundInvoker() {
        try {
            Class.forName("io.netty.channel.ChannelOutboundInvoker", false, Channel.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError exception) {
            return false;
        }
    }

    /**
     * Creates the channel proxy that suits the netty version of the server.
     *
     * @param delegate  the channel to proxy.
     * @param eventLoop the proxied event loop of the channel.
     * @param injector  the injector of the channel.
     * @return the proxied channel.
     */
    static Channel createChannelProxy(Channel delegate, EventLoop eventLoop, NettyChannelInjector injector) {
        if (!REQUIRED) {
            return new NettyChannelProxy(delegate, eventLoop, injector);
        }
        return createDynamicProxy(delegate, eventLoop, injector);
    }

    /**
     * Creates the dynamic channel proxy regardless of the netty version (used directly by tests).
     *
     * @param delegate  the channel to proxy.
     * @param eventLoop the proxied event loop of the channel.
     * @param injector  the injector of the channel.
     * @return the proxied channel.
     */
    static Channel createDynamicProxy(Channel delegate, EventLoop eventLoop, NettyChannelInjector injector) {
        LegacyNettyChannelProxy handler = new LegacyNettyChannelProxy(delegate, eventLoop, injector);
        return (Channel) Proxy.newProxyInstance(LegacyNettyChannelProxy.class.getClassLoader(),
                new Class<?>[]{Channel.class}, handler);
    }

    /**
     * Checks if the given channel is a proxy created by {@link #createChannelProxy(Channel, EventLoop, NettyChannelInjector)}.
     *
     * @param channel the channel to check.
     * @return true if the channel is one of our proxies.
     */
    static boolean isChannelProxy(Object channel) {
        // never touch NettyChannelProxy on netty 4.0, loading it fails there
        if (!REQUIRED && channel instanceof NettyChannelProxy) {
            return true;
        }
        return channel != null && Proxy.isProxyClass(channel.getClass())
                && Proxy.getInvocationHandler(channel) instanceof LegacyNettyChannelProxy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int argumentCount = args == null ? 0 : args.length;
        switch (method.getName()) {
            case "eventLoop":
                if (argumentCount == 0) {
                    return this.eventLoop;
                }
                break;
            case "write":
            case "writeAndFlush":
                boolean flush = method.getName().equals("writeAndFlush");
                if (argumentCount == 1) {
                    return this.write(args[0], this.delegate.newPromise(), flush);
                }
                if (argumentCount == 2 && args[1] instanceof ChannelPromise) {
                    return this.write(args[0], (ChannelPromise) args[1], flush);
                }
                break;
            case "equals":
                if (argumentCount == 1) {
                    return proxy == args[0];
                }
                break;
            case "hashCode":
                if (argumentCount == 0) {
                    return System.identityHashCode(proxy);
                }
                break;
            default:
                break;
        }

        try {
            return method.invoke(this.delegate, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    private Object write(Object msg, ChannelPromise promise, boolean flush) {
        if (this.injector.isPacketEventCallNeeded(msg)) {
            this.injector.processPacketOutbound(msg, packet -> {
                if (flush) {
                    this.delegate.writeAndFlush(packet, promise);
                } else {
                    this.delegate.write(packet, promise);
                }
            });
            return promise;
        }
        return flush ? this.delegate.writeAndFlush(msg, promise) : this.delegate.write(msg, promise);
    }
}
