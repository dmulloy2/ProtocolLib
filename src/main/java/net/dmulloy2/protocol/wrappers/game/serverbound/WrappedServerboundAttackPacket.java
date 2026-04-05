package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundAttackPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – ID of the entity being attacked</li>
 * </ul>
 */
public class WrappedServerboundAttackPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ATTACK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedServerboundAttackPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundAttackPacket(int entityId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(entityId)));
    }

    public WrappedServerboundAttackPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the ID of the entity being attacked.
     */
    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the ID of the entity being attacked.
     */
    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }
}
