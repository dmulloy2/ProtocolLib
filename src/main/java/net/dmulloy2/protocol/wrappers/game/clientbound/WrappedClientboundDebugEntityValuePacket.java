package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDebugEntityValuePacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code int entityId} – the entity ID</li>
 *   <li>{@code DebugSubscription.Update<?> update} – the debug update data (opaque, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundDebugEntityValuePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DEBUG_ENTITY_VALUE;

    public WrappedClientboundDebugEntityValuePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugEntityValuePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }
}
