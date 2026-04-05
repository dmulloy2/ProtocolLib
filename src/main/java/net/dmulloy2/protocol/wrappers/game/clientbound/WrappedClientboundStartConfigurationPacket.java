package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundStartConfigurationPacket} (game phase, clientbound).
 */
public class WrappedClientboundStartConfigurationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.START_CONFIGURATION;

    public WrappedClientboundStartConfigurationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundStartConfigurationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
