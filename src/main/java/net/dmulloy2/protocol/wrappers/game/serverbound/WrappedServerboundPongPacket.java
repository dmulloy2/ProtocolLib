package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPongPacket} (game phase, serverbound).
 */
public class WrappedServerboundPongPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.PONG;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedServerboundPongPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPongPacket(int id) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id)));
    }

    public WrappedServerboundPongPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }
}
