package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientTickEndPacket} (game phase, serverbound).
 * This is an empty packet with no fields sent at end of client tick.
 */
public class WrappedServerboundClientTickEndPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CLIENT_TICK_END;

    public WrappedServerboundClientTickEndPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundClientTickEndPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
