package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPingRequestPacket} (game phase, serverbound).
 */
public class WrappedServerboundPingRequestPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.PING_REQUEST;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(long.class);

    public WrappedServerboundPingRequestPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPingRequestPacket(long time) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(time)));
    }

    public WrappedServerboundPingRequestPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getTime() {
        return handle.getLongs().read(0);
    }

    public void setTime(long time) {
        handle.getLongs().write(0, time);
    }
}
