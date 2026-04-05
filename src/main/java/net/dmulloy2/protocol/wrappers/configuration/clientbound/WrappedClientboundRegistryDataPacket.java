package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRegistryDataPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundRegistryDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.REGISTRY_DATA;

    public WrappedClientboundRegistryDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRegistryDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
