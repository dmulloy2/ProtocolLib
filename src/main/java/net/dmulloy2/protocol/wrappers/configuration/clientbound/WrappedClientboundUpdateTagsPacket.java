package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateTagsPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundUpdateTagsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.UPDATE_TAGS;

    public WrappedClientboundUpdateTagsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateTagsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'tags' (NMS type: Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload>)
    //   Same as the game-phase UpdateTags packet. No ProtocolLib accessor exists.
    //   Use handle.getModifier().read(0) to get the raw Map.
}
