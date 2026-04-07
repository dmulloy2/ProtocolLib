package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundCookieResponsePacket} (configuration phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Identifier key} – namespaced key identifying the cookie</li>
 *   <li>{@code byte[] payload} – cookie bytes, or {@code null} if the client has no value</li>
 * </ul>
 */
public class WrappedServerboundCookieResponsePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.COOKIE_RESPONSE;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(byte[].class);

    public WrappedServerboundCookieResponsePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundCookieResponsePacket(MinecraftKey key, byte[] payload) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(key, payload)));
    }

    public WrappedServerboundCookieResponsePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getKey() {
        return handle.getMinecraftKeys().readSafely(0);
    }

    public void setKey(MinecraftKey key) {
        handle.getMinecraftKeys().writeSafely(0, key);
    }

    public byte[] getPayload() {
        return handle.getByteArrays().readSafely(0);
    }

    public void setPayload(byte[] payload) {
        handle.getByteArrays().writeSafely(0, payload);
    }
}
