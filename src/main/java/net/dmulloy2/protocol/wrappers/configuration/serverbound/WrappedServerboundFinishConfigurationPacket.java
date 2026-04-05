package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundFinishConfigurationPacket} (configuration phase, serverbound).
 */
public class WrappedServerboundFinishConfigurationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.FINISH_CONFIGURATION;

    public WrappedServerboundFinishConfigurationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundFinishConfigurationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
