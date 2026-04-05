package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundLoginAcknowledgedPacket} (login phase, serverbound).
 */
public class WrappedServerboundLoginAcknowledgedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Client.LOGIN_ACK;

    public WrappedServerboundLoginAcknowledgedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundLoginAcknowledgedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
