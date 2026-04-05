package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundStoreCookiePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Identifier key} – namespaced key identifying the cookie</li>
 *   <li>{@code byte[] payload} – cookie data (max 5120 bytes)</li>
 * </ul>
 */
public class WrappedClientboundStoreCookiePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.STORE_COOKIE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(byte[].class);

    public WrappedClientboundStoreCookiePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundStoreCookiePacket(MinecraftKey key, byte[] payload) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(key, payload)));
    }

    public WrappedClientboundStoreCookiePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getKey() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setKey(MinecraftKey key) {
        handle.getMinecraftKeys().write(0, key);
    }

    public byte[] getPayload() {
        return handle.getByteArrays().read(0);
    }

    public void setPayload(byte[] payload) {
        handle.getByteArrays().write(0, payload);
    }
}
