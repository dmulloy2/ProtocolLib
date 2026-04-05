package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundCookieResponsePacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Identifier key} – namespaced key identifying the cookie</li>
 *   <li>{@code Optional<byte[]> payload} – cookie bytes, or empty if the client has no value</li>
 * </ul>
 */
public class WrappedServerboundCookieResponsePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.COOKIE_RESPONSE;

    public WrappedServerboundCookieResponsePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundCookieResponsePacket(MinecraftKey key, Optional<byte[]> payload) {
        this();
        setKey(key);
        setPayload(payload);
    }

    public WrappedServerboundCookieResponsePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getKey() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setKey(MinecraftKey key) {
        handle.getMinecraftKeys().write(0, key);
    }

    public Optional<byte[]> getPayload() {
        return Optional.ofNullable(handle.getByteArrays().readSafely(0));
    }

    public void setPayload(Optional<byte[]> payload) {
        handle.getByteArrays().write(0, payload == null ? null : payload.orElse(null));
    }
}
