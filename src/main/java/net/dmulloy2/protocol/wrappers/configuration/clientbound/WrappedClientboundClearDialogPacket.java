package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundClearDialogPacket} (configuration phase, clientbound).
 * This is an empty packet that clears the current dialog.
 */
public class WrappedClientboundClearDialogPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.CLEAR_DIALOG;

    public WrappedClientboundClearDialogPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundClearDialogPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
