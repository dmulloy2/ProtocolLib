package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLoginCompressionPacket} (login phase, clientbound).
 */
public class WrappedClientboundLoginCompressionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.SET_COMPRESSION;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedClientboundLoginCompressionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLoginCompressionPacket(int compressionThreshold) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(compressionThreshold)));
    }

    public WrappedClientboundLoginCompressionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getCompressionThreshold() {
        return handle.getIntegers().read(0);
    }

    public void setCompressionThreshold(int compressionThreshold) {
        handle.getIntegers().write(0, compressionThreshold);
    }
}
