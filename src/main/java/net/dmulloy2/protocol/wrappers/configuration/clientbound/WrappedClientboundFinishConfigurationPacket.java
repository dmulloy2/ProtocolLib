package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundFinishConfigurationPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundFinishConfigurationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.FINISH_CONFIGURATION;

    public WrappedClientboundFinishConfigurationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundFinishConfigurationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
