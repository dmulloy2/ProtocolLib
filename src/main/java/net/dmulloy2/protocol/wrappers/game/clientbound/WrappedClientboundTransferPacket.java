package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundTransferPacket} (game phase, clientbound).
 */
public class WrappedClientboundTransferPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TRANSFER;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(int.class);

    public WrappedClientboundTransferPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTransferPacket(String host, int port) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(host, port)));
    }

    public WrappedClientboundTransferPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getHost() {
        return handle.getStrings().read(0);
    }

    public void setHost(String host) {
        handle.getStrings().write(0, host);
    }

    public int getPort() {
        return handle.getIntegers().read(0);
    }

    public void setPort(int port) {
        handle.getIntegers().write(0, port);
    }
}
