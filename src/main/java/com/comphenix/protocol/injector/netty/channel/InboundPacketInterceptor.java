package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.netty.ChannelListener;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftReflection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InboundPacketInterceptor extends ChannelInboundHandlerAdapter {

	private final NettyChannelInjector injector;
	private final ChannelListener channelListener;

	public InboundPacketInterceptor(NettyChannelInjector injector, ChannelListener listener) {
		this.injector = injector;
		this.channelListener = listener;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (MinecraftReflection.isPacketClass(msg)) {
			// process the login if the packet is one before posting the packet to any
			// handler to provide "real" data the method invocation will do nothing if the
			// packet is not a login packet
			this.injector.tryProcessLogin(msg);

			PacketType.Protocol protocol = this.injector.getInboundProtocol();
			PacketType packetType = PacketRegistry.getPacketType(protocol, msg.getClass());
			
			// TODO: ignore packet or throw error?
			if (packetType == null) {
				ctx.fireChannelRead(msg);
				return;
			}

			// check if there are any listeners bound for the packet - if not just post the
			// packet down the pipeline
			if (!this.channelListener.hasInboundListener(packetType)) {
				ctx.fireChannelRead(msg);
				return;
			}

			// call all inbound listeners
			this.injector.processInboundPacket(ctx, msg, packetType);
		} else {
			// just pass the message down the pipeline
			ctx.fireChannelRead(msg);
		}
	}
}
