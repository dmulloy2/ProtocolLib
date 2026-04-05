package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundServerLinksPacket} (game phase, clientbound).
 */
public class WrappedClientboundServerLinksPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SERVER_LINKS;

    public WrappedClientboundServerLinksPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundServerLinksPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
