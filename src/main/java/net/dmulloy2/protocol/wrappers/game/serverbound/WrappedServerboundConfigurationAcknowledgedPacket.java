package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundConfigurationAcknowledgedPacket} (game phase, serverbound).
 */
public class WrappedServerboundConfigurationAcknowledgedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CONFIGURATION_ACK;

    public WrappedServerboundConfigurationAcknowledgedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundConfigurationAcknowledgedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
