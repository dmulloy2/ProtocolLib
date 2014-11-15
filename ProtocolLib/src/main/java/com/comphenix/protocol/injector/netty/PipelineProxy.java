package com.comphenix.protocol.injector.netty;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelPromise;
import net.minecraft.util.io.netty.util.concurrent.EventExecutorGroup;

/**
 * A pipeline proxy.
 * @author Kristian
 */
public class PipelineProxy implements ChannelPipeline {
	protected final ChannelPipeline pipeline;
	protected final Channel channel;

	public PipelineProxy(ChannelPipeline pipeline, Channel channel) {
		this.pipeline = pipeline;
		this.channel = channel;
	}

	@Override
	public ChannelPipeline addAfter(EventExecutorGroup arg0, String arg1, String arg2, ChannelHandler arg3) {
		pipeline.addAfter(arg0, arg1, arg2, arg3);
		return this;
	}

	@Override
	public ChannelPipeline addAfter(String arg0, String arg1, ChannelHandler arg2) {
		pipeline.addAfter(arg0, arg1, arg2);
		return this;
	}

	@Override
	public ChannelPipeline addBefore(EventExecutorGroup arg0, String arg1, String arg2, ChannelHandler arg3) {
		pipeline.addBefore(arg0, arg1, arg2, arg3);
		return this;
	}

	@Override
	public ChannelPipeline addBefore(String arg0, String arg1, ChannelHandler arg2) {
		pipeline.addBefore(arg0, arg1, arg2);
		return this;
	}

	@Override
	public ChannelPipeline addFirst(ChannelHandler... arg0) {
		pipeline.addFirst(arg0);
		return this;
	}

	@Override
	public ChannelPipeline addFirst(EventExecutorGroup arg0, ChannelHandler... arg1) {
		pipeline.addFirst(arg0, arg1);
		return this;
	}

	@Override
	public ChannelPipeline addFirst(EventExecutorGroup arg0, String arg1, ChannelHandler arg2) {
		pipeline.addFirst(arg0, arg1, arg2);
		return this;
	}

	@Override
	public ChannelPipeline addFirst(String arg0, ChannelHandler arg1) {
		pipeline.addFirst(arg0, arg1);
		return this;
	}

	@Override
	public ChannelPipeline addLast(ChannelHandler... arg0) {
		pipeline.addLast(arg0);
		return this;
	}

	@Override
	public ChannelPipeline addLast(EventExecutorGroup arg0, ChannelHandler... arg1) {
		pipeline.addLast(arg0, arg1);
		return this;
	}

	@Override
	public ChannelPipeline addLast(EventExecutorGroup arg0, String arg1, ChannelHandler arg2) {
		pipeline.addLast(arg0, arg1, arg2);
		return this;
	}

	@Override
	public ChannelPipeline addLast(String arg0, ChannelHandler arg1) {
		pipeline.addLast(arg0, arg1);
		return this;
	}

	@Override
	public ChannelFuture bind(SocketAddress arg0, ChannelPromise arg1) {
		return pipeline.bind(arg0, arg1);
	}

	@Override
	public ChannelFuture bind(SocketAddress arg0) {
		return pipeline.bind(arg0);
	}

	@Override
	public Channel channel() {
		return channel;
	}

	@Override
	public ChannelFuture close() {
		return pipeline.close();
	}

	@Override
	public ChannelFuture close(ChannelPromise arg0) {
		return pipeline.close(arg0);
	}

	@Override
	public ChannelFuture connect(SocketAddress arg0, ChannelPromise arg1) {
		return pipeline.connect(arg0, arg1);
	}

	@Override
	public ChannelFuture connect(SocketAddress arg0, SocketAddress arg1, ChannelPromise arg2) {
		return pipeline.connect(arg0, arg1, arg2);
	}

	@Override
    public ChannelFuture connect(SocketAddress arg0, SocketAddress arg1) {
		return pipeline.connect(arg0, arg1);
	}

	@Override
	public ChannelFuture connect(SocketAddress arg0) {
		return pipeline.connect(arg0);
	}

	@Override
	public ChannelHandlerContext context(ChannelHandler arg0) {
		return pipeline.context(arg0);
	}

	@Override
    public ChannelHandlerContext context(Class<? extends ChannelHandler> arg0) {
		return pipeline.context(arg0);
	}

	@Override
	public ChannelHandlerContext context(String arg0) {
		return pipeline.context(arg0);
	}

	// We have to call the depreciated methods to properly implement the proxy
	@SuppressWarnings("deprecation")
	@Override
	public ChannelFuture deregister() {
		return pipeline.deregister();
	}

	@SuppressWarnings("deprecation")
	@Override
	public ChannelFuture deregister(ChannelPromise arg0) {
		return pipeline.deregister(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ChannelPipeline fireChannelUnregistered() {
		pipeline.fireChannelUnregistered();
		return this;
	}

	@Override
	public ChannelFuture disconnect() {
		return pipeline.disconnect();
	}

	@Override
	public ChannelFuture disconnect(ChannelPromise arg0) {
		return pipeline.disconnect(arg0);
	}

	@Override
	public ChannelPipeline fireChannelActive() {
		pipeline.fireChannelActive();
		return this;
	}

	@Override
	public ChannelPipeline fireChannelInactive() {
		pipeline.fireChannelInactive();
		return this;
	}

	@Override
	public ChannelPipeline fireChannelRead(Object arg0) {
		pipeline.fireChannelRead(arg0);
		return this;
	}

	@Override
	public ChannelPipeline fireChannelReadComplete() {
		pipeline.fireChannelReadComplete();
		return this;
	}

	@Override
	public ChannelPipeline fireChannelRegistered() {
		pipeline.fireChannelRegistered();
		return this;
	}

	@Override
	public ChannelPipeline fireChannelWritabilityChanged() {
		pipeline.fireChannelWritabilityChanged();
		return this;
	}

	@Override
	public ChannelPipeline fireExceptionCaught(Throwable arg0) {
		pipeline.fireExceptionCaught(arg0);
		return this;
	}

	@Override
	public ChannelPipeline fireUserEventTriggered(Object arg0) {
		pipeline.fireUserEventTriggered(arg0);
		return this;
	}

	@Override
	public ChannelHandler first() {
		return pipeline.first();
	}

	@Override
	public ChannelHandlerContext firstContext() {
		return pipeline.firstContext();
	}

	@Override
	public ChannelPipeline flush() {
		pipeline.flush();
		return this;
	}

	@Override
	public <T extends ChannelHandler> T get(Class<T> arg0) {
		return pipeline.get(arg0);
	}

	@Override
	public ChannelHandler get(String arg0) {
		return pipeline.get(arg0);
	}

	@Override
	public Iterator<Entry<String, ChannelHandler>> iterator() {
		return pipeline.iterator();
	}

	@Override
	public ChannelHandler last() {
		return pipeline.last();
	}

	@Override
	public ChannelHandlerContext lastContext() {
		return pipeline.lastContext();
	}

	@Override
	public List<String> names() {
		return pipeline.names();
	}

	@Override
	public ChannelPipeline read() {
		pipeline.read();
		return this;
	}

	@Override
	public ChannelPipeline remove(ChannelHandler arg0) {
		pipeline.remove(arg0);
		return this;
	}

	@Override
	public <T extends ChannelHandler> T remove(Class<T> arg0) {
		return pipeline.remove(arg0);
	}

	@Override
	public ChannelHandler remove(String arg0) {
		return pipeline.remove(arg0);
	}

	@Override
	public ChannelHandler removeFirst() {
		return pipeline.removeFirst();
	}

	@Override
	public ChannelHandler removeLast() {
		return pipeline.removeLast();
	}

	@Override
	public ChannelPipeline replace(ChannelHandler arg0, String arg1, ChannelHandler arg2) {
		pipeline.replace(arg0, arg1, arg2);
		return this;
	}

	@Override
	public <T extends ChannelHandler> T replace(Class<T> arg0, String arg1, ChannelHandler arg2) {
		return pipeline.replace(arg0, arg1, arg2);
	}

	@Override
	public ChannelHandler replace(String arg0, String arg1, ChannelHandler arg2) {
		return pipeline.replace(arg0, arg1, arg2);
	}

	@Override
	public Map<String, ChannelHandler> toMap() {
		return pipeline.toMap();
	}

	@Override
	public ChannelFuture write(Object arg0, ChannelPromise arg1) {
		return pipeline.write(arg0, arg1);
	}

	@Override
	public ChannelFuture write(Object arg0) {
		return pipeline.write(arg0);
	}

	@Override
	public ChannelFuture writeAndFlush(Object arg0, ChannelPromise arg1) {
		return pipeline.writeAndFlush(arg0, arg1);
	}

	@Override
	public ChannelFuture writeAndFlush(Object arg0) {
		return pipeline.writeAndFlush(arg0);
	}
}