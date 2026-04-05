package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundShowDialogPacket} (game phase, clientbound).
 * Sends a dialog to the client. The dialog field has no ProtocolLib accessor.
 */
public class WrappedClientboundShowDialogPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SHOW_DIALOG;

    public WrappedClientboundShowDialogPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundShowDialogPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
