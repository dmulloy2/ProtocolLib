package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftReflection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

final class InboundPacketInterceptor extends ChannelInboundHandlerAdapter {

    private final NettyChannelInjector injector;

    public InboundPacketInterceptor(NettyChannelInjector injector) {
        this.injector = injector;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (MinecraftReflection.isPacketClass(msg)) {
            // try get packet type
            PacketType.Protocol protocol = this.injector.getInboundProtocol();
            if (protocol == Protocol.UNKNOWN) {
                ProtocolLogger.debug("skipping unknown inbound protocol for {0}", msg.getClass());
                ctx.fireChannelRead(msg);
                return;
            }

            PacketType packetType = PacketRegistry.getPacketType(protocol, msg.getClass());
            if (packetType == null) {
                ProtocolLogger.debug("skipping unknown inbound packet type for {0}", msg.getClass());
                ctx.fireChannelRead(msg);
                return;
            }

            // check if there are any listeners bound for the packet - if not just send the
            // packet down the pipeline
            if (!this.injector.hasInboundListener(packetType)) {
                ctx.fireChannelRead(msg);
                return;
            }

            // don't invoke next handler and transfer packet to injector for further processing
            this.injector.processInbound(ctx, new PacketContainer(packetType, msg));
        } else {
            // just pass the message down the pipeline
            ctx.fireChannelRead(msg);
        }
    }
}
