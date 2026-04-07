package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundHelloPacket} (login phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String serverId} – server ID (20-char hash used in auth)</li>
 *   <li>{@code byte[] publicKey} – server's RSA public key</li>
 *   <li>{@code byte[] challenge} – random 4-byte challenge</li>
 *   <li>{@code boolean shouldAuthenticate} – whether online-mode auth is required</li>
 * </ul>
 */
public class WrappedClientboundHelloPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.ENCRYPTION_BEGIN;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(byte[].class)
            .withParam(byte[].class)
            .withParam(boolean.class);

    public WrappedClientboundHelloPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundHelloPacket(String serverId, byte[] publicKey, byte[] challenge, boolean shouldAuthenticate) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(serverId, publicKey, challenge, shouldAuthenticate)));
    }

    public WrappedClientboundHelloPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getServerId() {
        return handle.getStrings().readSafely(0);
    }

    public void setServerId(String serverId) {
        handle.getStrings().writeSafely(0, serverId);
    }

    public byte[] getPublicKey() {
        return handle.getByteArrays().readSafely(0);
    }

    public void setPublicKey(byte[] publicKey) {
        handle.getByteArrays().writeSafely(0, publicKey);
    }

    public byte[] getChallenge() {
        return handle.getByteArrays().readSafely(1);
    }

    public void setChallenge(byte[] challenge) {
        handle.getByteArrays().writeSafely(1, challenge);
    }

    public boolean isShouldAuthenticate() {
        return handle.getBooleans().readSafely(0);
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        handle.getBooleans().writeSafely(0, shouldAuthenticate);
    }
}
