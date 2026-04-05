package net.dmulloy2.protocol.wrappers.status.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundStatusRequestPacket} (status phase, serverbound).
 */
public class WrappedServerboundStatusRequestPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Status.Client.START;

    public WrappedServerboundStatusRequestPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundStatusRequestPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
