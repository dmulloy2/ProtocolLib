package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
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

    public WrappedClientboundHelloPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundHelloPacket(String serverId, byte[] publicKey, byte[] challenge, boolean shouldAuthenticate) {
        this();
        setServerId(serverId);
        setPublicKey(publicKey);
        setChallenge(challenge);
        setShouldAuthenticate(shouldAuthenticate);
    }

    public WrappedClientboundHelloPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getServerId() {
        return handle.getStrings().read(0);
    }

    public void setServerId(String serverId) {
        handle.getStrings().write(0, serverId);
    }

    public byte[] getPublicKey() {
        return handle.getByteArrays().read(0);
    }

    public void setPublicKey(byte[] publicKey) {
        handle.getByteArrays().write(0, publicKey);
    }

    public byte[] getChallenge() {
        return handle.getByteArrays().read(1);
    }

    public void setChallenge(byte[] challenge) {
        handle.getByteArrays().write(1, challenge);
    }

    public boolean isShouldAuthenticate() {
        return handle.getBooleans().read(0);
    }

    public void setShouldAuthenticate(boolean shouldAuthenticate) {
        handle.getBooleans().write(0, shouldAuthenticate);
    }
}
