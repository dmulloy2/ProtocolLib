package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundBundleDelimiterPacket} (game phase, clientbound).
 */
public class WrappedClientboundBundleDelimiterPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BUNDLE;

    public WrappedClientboundBundleDelimiterPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundBundleDelimiterPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
