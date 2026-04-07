package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import java.util.List;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerInfoRemovePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code List<UUID> profileIds} – UUIDs of the player profiles to remove from the tab-list</li>
 * </ul>
 */
public class WrappedClientboundPlayerInfoRemovePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_INFO_REMOVE;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(List.class);

    public WrappedClientboundPlayerInfoRemovePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerInfoRemovePacket(List<UUID> profileIds) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(profileIds)));
    }

    public WrappedClientboundPlayerInfoRemovePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public List<UUID> getProfileIds() {
        return handle.getUUIDLists().readSafely(0);
    }

    public void setProfileIds(List<UUID> profileIds) {
        handle.getUUIDLists().writeSafely(0, profileIds);
    }
}
