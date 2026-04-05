package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundKeepAlivePacket} (game phase, serverbound).
 */
public class WrappedServerboundKeepAlivePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.KEEP_ALIVE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(long.class);

    public WrappedServerboundKeepAlivePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundKeepAlivePacket(long id) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id)));
    }

    public WrappedServerboundKeepAlivePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getId() {
        return handle.getLongs().read(0);
    }

    public void setId(long id) {
        handle.getLongs().write(0, id);
    }
}
