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

    // TODO: missing field 'knownPacks' (NMS type: List<KnownPack> — record with namespace, id, version Strings)
    //   Use AutoWrapper or a dedicated WrappedKnownPack class; each KnownPack is a simple 3-String record.
    //   Alternatively, handle.getModifier().read(0) returns the raw List<KnownPack>.
}
