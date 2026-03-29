package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundOpenBookPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Hand hand} – the hand holding the book to open</li>
 * </ul>
 */
public class WrappedClientboundOpenBookPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_BOOK;

    public WrappedClientboundOpenBookPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundOpenBookPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public EnumWrappers.Hand getHand() {
        return handle.getHands().read(0);
    }

    public void setHand(EnumWrappers.Hand hand) {
        handle.getHands().write(0, hand);
    }
}
