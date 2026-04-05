package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCookieRequestPacket} (configuration phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Identifier key} – namespaced key identifying the cookie to request</li>
 * </ul>
 */
public class WrappedClientboundCookieRequestPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.COOKIE_REQUEST;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter());

    public WrappedClientboundCookieRequestPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCookieRequestPacket(MinecraftKey key) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(key)));
    }

    public WrappedClientboundCookieRequestPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getKey() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setKey(MinecraftKey key) {
        handle.getMinecraftKeys().write(0, key);
    }
}
