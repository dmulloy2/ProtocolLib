package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import net.dmulloy2.protocol.AbstractPacket;
import java.util.Optional;

/**
 * Wrapper for {@code ServerboundCustomClickActionPacket} (game phase, serverbound).
 *
 * <p>Sent when the client clicks a custom button identified by a resource-location key.
 */
public class WrappedServerboundCustomClickActionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CUSTOM_CLICK_ACTION;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(Optional.class, Converters.optional(BukkitConverters.getNbtConverter()));

    public WrappedServerboundCustomClickActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundCustomClickActionPacket(MinecraftKey id, Optional<NbtBase<?>> payload) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(id, payload)));
    }

    public WrappedServerboundCustomClickActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getId() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setId(MinecraftKey id) {
        handle.getMinecraftKeys().write(0, id);
    }
}
