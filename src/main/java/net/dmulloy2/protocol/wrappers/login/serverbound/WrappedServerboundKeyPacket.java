package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundKeyPacket} (login phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code byte[] keyBytes} – RSA-encrypted shared secret</li>
 *   <li>{@code byte[] encryptedChallenge} – RSA-encrypted server challenge</li>
 * </ul>
 */
public class WrappedServerboundKeyPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Client.ENCRYPTION_BEGIN;

    public WrappedServerboundKeyPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundKeyPacket(byte[] keyBytes, byte[] encryptedChallenge) {
        this();
        setKeyBytes(keyBytes);
        setEncryptedChallenge(encryptedChallenge);
    }

    public WrappedServerboundKeyPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public byte[] getKeyBytes() {
        return handle.getByteArrays().read(0);
    }

    public void setKeyBytes(byte[] keyBytes) {
        handle.getByteArrays().write(0, keyBytes);
    }

    public byte[] getEncryptedChallenge() {
        return handle.getByteArrays().read(1);
    }

    public void setEncryptedChallenge(byte[] encryptedChallenge) {
        handle.getByteArrays().write(1, encryptedChallenge);
    }
}
