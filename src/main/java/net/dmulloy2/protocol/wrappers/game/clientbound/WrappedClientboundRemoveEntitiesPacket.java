package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ClientboundRemoveEntitiesPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code List<Integer> entityIds} – list of entity IDs to destroy</li>
 * </ul>
 */
public class WrappedClientboundRemoveEntitiesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_DESTROY;

    public WrappedClientboundRemoveEntitiesPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundRemoveEntitiesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public List<Integer> getEntityIds() {
        return handle.getIntLists().read(0);
    }

    public void setEntityIds(List<Integer> entityIds) {
        handle.getIntLists().write(0, entityIds);
    }
}
