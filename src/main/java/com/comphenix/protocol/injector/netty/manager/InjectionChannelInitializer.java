package com.comphenix.protocol.injector.netty.manager;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InjectionChannelInitializer extends ChannelInboundHandlerAdapter {

	private final String inboundHandlerName;
	private final ChannelInboundHandler handler;

	public InjectionChannelInitializer(String inboundHandlerName, ChannelInboundHandler handler) {
		this.inboundHandlerName = inboundHandlerName;
		this.handler = handler;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof Channel) {
			Channel channel = (Channel) msg;
			channel.pipeline().addLast(this.inboundHandlerName, this.handler);
		}

		// forward to all other handlers in the pipeline
		ctx.fireChannelRead(msg);
	}

	@Override
	public boolean isSharable() {
		// we do it this way to prevent the lookup overheat
		return true;
	}
}
