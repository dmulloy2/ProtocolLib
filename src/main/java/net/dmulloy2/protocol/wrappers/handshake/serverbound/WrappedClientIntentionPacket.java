package net.dmulloy2.protocol.wrappers.handshake.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientIntentionPacket} (handshake phase, serverbound).
 *
 * <p>The first packet sent by the client to initiate a connection.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code int protocolVersion} – the client's protocol version</li>
 *   <li>{@code String hostName} – the server address the client is connecting to</li>
 *   <li>{@code int port} – the server port</li>
 *   <li>{@code ClientIntent intention} – the client's intent (STATUS, LOGIN, or TRANSFER)</li>
 * </ul>
 */
public class WrappedClientIntentionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Handshake.Client.SET_PROTOCOL;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(String.class)
            .withParam(int.class)
            .withParam(EnumWrappers.getClientIntentClass(), EnumWrappers.getClientIntentConverter());

    public WrappedClientIntentionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientIntentionPacket(int protocolVersion, String hostName, int port, EnumWrappers.ClientIntent intention) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(protocolVersion, hostName, port, intention)));
    }

    public WrappedClientIntentionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getProtocolVersion() {
        return handle.getIntegers().readSafely(0);
    }

    public void setProtocolVersion(int protocolVersion) {
        handle.getIntegers().writeSafely(0, protocolVersion);
    }

    public String getHostName() {
        return handle.getStrings().readSafely(0);
    }

    public void setHostName(String hostName) {
        handle.getStrings().writeSafely(0, hostName);
    }

    public int getPort() {
        return handle.getIntegers().readSafely(1);
    }

    public void setPort(int port) {
        handle.getIntegers().writeSafely(1, port);
    }

    public EnumWrappers.ClientIntent getIntention() {
        return handle.getClientIntents().readSafely(0);
    }

    public void setIntention(EnumWrappers.ClientIntent intention) {
        handle.getClientIntents().writeSafely(0, intention);
    }
}
