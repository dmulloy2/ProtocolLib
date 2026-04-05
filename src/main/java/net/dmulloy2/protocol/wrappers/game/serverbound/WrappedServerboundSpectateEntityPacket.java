package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSpectateEntityPacket} (Play phase, serverbound).
 *
 * <p>Sent by a spectating client to request that the camera be attached to a specific entity.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity to spectate</li>
 * </ul>
 */
public class WrappedServerboundSpectateEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SPECTATE_ENTITY;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedServerboundSpectateEntityPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSpectateEntityPacket(int entityId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(entityId)));
    }

    public WrappedServerboundSpectateEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }
}
