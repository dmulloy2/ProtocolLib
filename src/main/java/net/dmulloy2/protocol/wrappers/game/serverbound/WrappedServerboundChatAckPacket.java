package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatAckPacket} (game phase, serverbound).
 */
public class WrappedServerboundChatAckPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT_ACK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedServerboundChatAckPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatAckPacket(int offset) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(offset)));
    }

    public WrappedServerboundChatAckPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getOffset() {
        return handle.getIntegers().read(0);
    }

    public void setOffset(int offset) {
        handle.getIntegers().write(0, offset);
    }
}
