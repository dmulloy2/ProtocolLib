package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;

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

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int[].class);

    public WrappedClientboundRemoveEntitiesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRemoveEntitiesPacket(int... entityIds) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entityIds)));
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
