package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundResetChatPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundResetChatPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.RESET_CHAT;

    public WrappedClientboundResetChatPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundResetChatPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
