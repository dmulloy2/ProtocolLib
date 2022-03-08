package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.injector.netty.WirePacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

final class WirePacketEncoder extends MessageToByteEncoder<WirePacket> {

	@Override
	protected void encode(ChannelHandlerContext ctx, WirePacket msg, ByteBuf out) throws Exception {
		msg.writeFully(out);
	}

	@Override
	public boolean acceptOutboundMessage(Object msg) {
		return msg instanceof WirePacket;
	}

	@Override
	public boolean isSharable() {
		// we do it this way to prevent the lookup overheat
		return true;
	}
}
