package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientInformationPacket} (configuration phase, serverbound).
 */
public class WrappedServerboundClientInformationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.CLIENT_INFORMATION;

    public WrappedServerboundClientInformationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundClientInformationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
