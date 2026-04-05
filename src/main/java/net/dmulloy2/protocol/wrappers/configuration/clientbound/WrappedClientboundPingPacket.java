package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPingPacket} (configuration phase, clientbound).
 */
public class WrappedClientboundPingPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.PING;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedClientboundPingPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPingPacket(int id) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(id)));
    }

    public WrappedClientboundPingPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }
}
