package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InboundProtocolReader extends ChannelInboundHandlerAdapter {

    private final NettyChannelInjector injector;

    private PacketType.Protocol protocol = null;

    public InboundProtocolReader(NettyChannelInjector injector) {
        this.injector = injector;
    }

    public PacketType.Protocol getProtocol() {
        return protocol;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.protocol = this.injector.getCurrentProtocol(PacketType.Sender.CLIENT);
        ctx.fireChannelRead(msg);
    }
}
