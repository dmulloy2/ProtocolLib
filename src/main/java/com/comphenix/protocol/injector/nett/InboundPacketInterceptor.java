package com.comphenix.protocol.injector.nett;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.utility.MinecraftReflection;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InboundPacketInterceptor extends ChannelInboundHandlerAdapter {

	private final NettyChannelInjector injector;
	private final ChannelListener channelListener;

	public InboundPacketInterceptor(NettyChannelInjector injector, ChannelListener channelListener) {
		this.injector = injector;
		this.channelListener = channelListener;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (this.shouldInterceptMessage(msg)) {
			// process the login if the packet is one before posting the packet to any handler to provide "real" data
			// the method invocation will do nothing if the packet is not a login packet
			this.injector.tryProcessLogin(msg);

			// call packet handlers, a null result indicates that we shouldn't change anything
			PacketEvent interceptionResult = this.channelListener.onPacketReceiving(this.injector, msg, null);
			if (interceptionResult == null) {
				ctx.fireChannelRead(msg);
				return;
			}

			// fire the intercepted packet down the pipeline if it wasn't cancelled
			if (!interceptionResult.isCancelled()) {
				ctx.fireChannelRead(interceptionResult.getPacket().getHandle());
			}
		} else {
			// just pass the message down the pipeline
			ctx.fireChannelRead(msg);
		}
	}

	private boolean shouldInterceptMessage(Object msg) {
		// only intercept minecraft packets and no garbage from other stuff in the channel
		return MinecraftReflection.getPacketClass().isAssignableFrom(msg.getClass());
	}
}
