package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import java.util.Optional;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundResourcePackPopPacket} (configuration phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Optional<UUID> id} – UUID of the resource pack to remove, or empty to remove all</li>
 * </ul>
 */
public class WrappedClientboundResourcePackPopPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.REMOVE_RESOURCE_PACK;

    public WrappedClientboundResourcePackPopPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundResourcePackPopPacket(Optional<UUID> id) {
        this();
        setId(id);
    }

    public WrappedClientboundResourcePackPopPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Optional<UUID> getId() {
        return handle.getOptionals(Converters.passthrough(UUID.class)).read(0);
    }

    public void setId(Optional<UUID> id) {
        handle.getOptionals(Converters.passthrough(UUID.class)).write(0, id);
    }
}
