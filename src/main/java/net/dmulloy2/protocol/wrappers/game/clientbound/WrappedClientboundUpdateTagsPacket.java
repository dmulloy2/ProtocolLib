package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedTagPayload;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Map;

/**
 * Wrapper for {@code ClientboundUpdateTagsPacket} (game phase, clientbound).
 */
public class WrappedClientboundUpdateTagsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TAGS;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(Map.class, BukkitConverters.getMapConverter(
                    WrappedTagPayload.REGISTRY_KEY_CONVERTER,
                    WrappedTagPayload.CONVERTER));

    public WrappedClientboundUpdateTagsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateTagsPacket(Map<MinecraftKey, WrappedTagPayload> tags) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(tags)));
    }

    public WrappedClientboundUpdateTagsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Map<MinecraftKey, WrappedTagPayload> getTags() {
        return handle.getMaps(WrappedTagPayload.REGISTRY_KEY_CONVERTER, WrappedTagPayload.CONVERTER).read(0);
    }

    public void setTags(Map<MinecraftKey, WrappedTagPayload> tags) {
        handle.getMaps(WrappedTagPayload.REGISTRY_KEY_CONVERTER, WrappedTagPayload.CONVERTER).write(0, tags);
    }
}
