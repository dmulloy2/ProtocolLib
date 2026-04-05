package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateTagsPacket} (game phase, clientbound).
 */
public class WrappedClientboundUpdateTagsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TAGS;

    public WrappedClientboundUpdateTagsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateTagsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
