package net.dmulloy2.protocol.wrappers.handshake.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
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

    /**
     * The client's intended next connection state, matching
     * {@code net.minecraft.network.protocol.handshake.ClientIntent}.
     */
    public enum ClientIntent {
        STATUS, LOGIN, TRANSFER
    }

    public WrappedClientIntentionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientIntentionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getProtocolVersion() {
        return handle.getIntegers().read(0);
    }

    public void setProtocolVersion(int protocolVersion) {
        handle.getIntegers().write(0, protocolVersion);
    }

    public String getHostName() {
        return handle.getStrings().read(0);
    }

    public void setHostName(String hostName) {
        handle.getStrings().write(0, hostName);
    }

    public int getPort() {
        return handle.getIntegers().read(1);
    }

    public void setPort(int port) {
        handle.getIntegers().write(1, port);
    }

    public ClientIntent getIntention() {
        return handle.getEnumModifier(ClientIntent.class, 3).read(0);
    }

    public void setIntention(ClientIntent intention) {
        handle.getEnumModifier(ClientIntent.class, 3).write(0, intention);
    }
}
