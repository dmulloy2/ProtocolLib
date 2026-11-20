package com.comphenix.protocol.injector.netty.channel;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.status.StatusProtocols;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelProtocolUtilTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testCachedProtocolFollowsCodecReplacement() {
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("encoder", new PacketEncoder<>(StatusProtocols.CLIENTBOUND));
        ChannelProtocolUtil.ProtocolCache cache = new ChannelProtocolUtil.ProtocolCache();

        assertEquals(PacketType.Protocol.STATUS, ChannelProtocolUtil.getProtocol(channel, PacketType.Sender.SERVER, cache));
        assertEquals(PacketType.Protocol.STATUS, ChannelProtocolUtil.getProtocol(channel, PacketType.Sender.SERVER, cache));

        // Minecraft replaces the codec handler whenever the protocol changes, which invalidates the cached value
        channel.pipeline().replace("encoder", "encoder", new PacketEncoder<>(LoginProtocols.CLIENTBOUND));
        assertEquals(PacketType.Protocol.LOGIN, ChannelProtocolUtil.getProtocol(channel, PacketType.Sender.SERVER, cache));
        assertEquals(PacketType.Protocol.LOGIN, ChannelProtocolUtil.PROTOCOL_RESOLVER.apply(channel, PacketType.Sender.SERVER));

        channel.pipeline().remove("encoder");
        assertEquals(PacketType.Protocol.UNKNOWN, ChannelProtocolUtil.getProtocol(channel, PacketType.Sender.SERVER, cache));
    }

	/*
    @Test
    public void testProtocolResolving() {
        Channel channel = new LocalServerChannel();
        channel.attr(NetworkManager.e).set(EnumProtocol.e.b(EnumProtocolDirection.a)); // ATTRIBUTE_SERVERBOUND_PROTOCOL -> Protocol.CONFIG.codec(SERVERBOUND)
        channel.attr(NetworkManager.f).set(EnumProtocol.b.b(EnumProtocolDirection.b)); // ATTRIBUTE_CLIENTBOUND_PROTOCOL -> Protocol.PLAY.codec(CLIENTBOUND)

        PacketType.Protocol serverBoundProtocol = ChannelProtocolUtil.PROTOCOL_RESOLVER.apply(channel, PacketType.Sender.CLIENT);
        Assertions.assertEquals(PacketType.Protocol.CONFIGURATION, serverBoundProtocol);

        PacketType.Protocol clientBoundProtocol = ChannelProtocolUtil.PROTOCOL_RESOLVER.apply(channel, PacketType.Sender.SERVER);
        Assertions.assertEquals(PacketType.Protocol.PLAY, clientBoundProtocol);
    }
	 */
}
