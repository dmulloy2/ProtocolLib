package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.Converters;
import java.util.Optional;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundResourcePackPopPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Optional<UUID> id} – UUID of the resource pack to remove, or empty to remove all</li>
 * </ul>
 */
public class WrappedClientboundResourcePackPopPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.REMOVE_RESOURCE_PACK;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(Optional.class);

    public WrappedClientboundResourcePackPopPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundResourcePackPopPacket(Optional<UUID> id) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(id)));
    }

    public WrappedClientboundResourcePackPopPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Optional<UUID> getId() {
        return handle.getOptionals(Converters.passthrough(UUID.class)).readSafely(0);
    }

    public void setId(Optional<UUID> id) {
        handle.getOptionals(Converters.passthrough(UUID.class)).writeSafely(0, id);
    }
}
