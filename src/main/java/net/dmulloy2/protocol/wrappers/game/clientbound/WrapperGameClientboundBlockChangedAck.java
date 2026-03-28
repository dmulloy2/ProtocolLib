package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundBlockChangedAckPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int sequence} – sequence number of the block change acknowledgement</li>
 * </ul>
 */
public class WrapperGameClientboundBlockChangedAck extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.BLOCK_CHANGED_ACK;

    public WrapperGameClientboundBlockChangedAck() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundBlockChangedAck(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getSequence() {
        return handle.getIntegers().read(0);
    }

    public void setSequence(int sequence) {
        handle.getIntegers().write(0, sequence);
    }
}
