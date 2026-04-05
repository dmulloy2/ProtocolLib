package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundShowDialogPacket} (configuration phase, clientbound).
 * Sends a dialog to the client.
 */
public class WrappedClientboundShowDialogPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.SHOW_DIALOG;

    public WrappedClientboundShowDialogPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundShowDialogPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
