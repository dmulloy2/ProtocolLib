package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSelectKnownPacks} (configuration phase, serverbound).
 * Selects known packs. The knownPacks field has no ProtocolLib accessor.
 */
public class WrappedServerboundSelectKnownPacksPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.SELECT_KNOWN_PACKS;

    public WrappedServerboundSelectKnownPacksPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSelectKnownPacksPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
