package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundTeleportToEntityPacket} (game phase, serverbound).
 */
public class WrappedServerboundTeleportToEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SPECTATE;

    public WrappedServerboundTeleportToEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundTeleportToEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
