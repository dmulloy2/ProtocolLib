package com.comphenix.protocol.injector.nett;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@Sharable
final class WirePacketEncoder extends MessageToByteEncoder<WirePacket> {

	@Override
	protected void encode(ChannelHandlerContext ctx, WirePacket msg, ByteBuf out) throws Exception {
		msg.writeFully(out);
	}
}
