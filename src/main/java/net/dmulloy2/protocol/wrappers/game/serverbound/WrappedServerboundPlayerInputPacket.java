package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerInputPacket} (game phase, serverbound).
 */
public class WrappedServerboundPlayerInputPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.STEER_VEHICLE;

    public WrappedServerboundPlayerInputPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerInputPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
