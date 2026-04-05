package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundTestInstanceBlockActionPacket} (game phase, serverbound).
 * Test instance block action. Complex fields with no ProtocolLib accessor.
 */
public class WrappedServerboundTestInstanceBlockActionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION;

    public WrappedServerboundTestInstanceBlockActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundTestInstanceBlockActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }
}
