package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundEntityTagQueryPacket} (game phase, serverbound).
 */
public class WrappedServerboundEntityTagQueryPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ENTITY_NBT_QUERY;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class);

    public WrappedServerboundEntityTagQueryPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundEntityTagQueryPacket(int transactionId, int entityId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(transactionId, entityId)));
    }

    public WrappedServerboundEntityTagQueryPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int transactionId) {
        handle.getIntegers().write(0, transactionId);
    }

    public int getEntityId() {
        return handle.getIntegers().read(1);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(1, entityId);
    }
}
