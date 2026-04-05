package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSelectKnownPacks} (configuration phase, clientbound).
 * Selects known packs. The knownPacks field has no ProtocolLib accessor.
 */
public class WrappedClientboundSelectKnownPacksPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.SELECT_KNOWN_PACKS;

    public WrappedClientboundSelectKnownPacksPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSelectKnownPacksPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
