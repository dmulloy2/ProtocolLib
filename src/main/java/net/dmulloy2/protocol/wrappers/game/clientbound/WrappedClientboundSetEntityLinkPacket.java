package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetEntityLinkPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int attachedEntityId} – entity ID of the entity being attached/leashed</li>
 *   <li>{@code int holdingEntityId} – entity ID of the holder (fence post or player); -1 to detach</li>
 * </ul>
 */
public class WrappedClientboundSetEntityLinkPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ATTACH_ENTITY;

    public WrappedClientboundSetEntityLinkPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetEntityLinkPacket(int attachedEntityId, int holdingEntityId) {
        this();
        setAttachedEntityId(attachedEntityId);
        setHoldingEntityId(holdingEntityId);
    }

    public WrappedClientboundSetEntityLinkPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getAttachedEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setAttachedEntityId(int attachedEntityId) {
        handle.getIntegers().write(0, attachedEntityId);
    }

    public int getHoldingEntityId() {
        return handle.getIntegers().read(1);
    }

    public void setHoldingEntityId(int holdingEntityId) {
        handle.getIntegers().write(1, holdingEntityId);
    }
}
