package net.dmulloy2.protocol.wrappers.status.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPongResponsePacket} (status phase, clientbound).
 */
public class WrappedClientboundPongResponsePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Status.Server.PONG;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(long.class);

    public WrappedClientboundPongResponsePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPongResponsePacket(long time) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(time)));
    }

    public WrappedClientboundPongResponsePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getTime() {
        return handle.getLongs().read(0);
    }

    public void setTime(long time) {
        handle.getLongs().write(0, time);
    }
}
