package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerSlotStateChangedPacket} (game phase, serverbound).
 */
public class WrappedServerboundContainerSlotStateChangedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CONTAINER_SLOT_STATE_CHANGED;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class)
            .withParam(boolean.class);

    public WrappedServerboundContainerSlotStateChangedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(slotId, containerId, newState)));
    }

    public WrappedServerboundContainerSlotStateChangedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getSlotId() {
        return handle.getIntegers().read(0);
    }

    public void setSlotId(int slotId) {
        handle.getIntegers().write(0, slotId);
    }

    public int getContainerId() {
        return handle.getIntegers().read(1);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(1, containerId);
    }

    public boolean isNewState() {
        return handle.getBooleans().read(0);
    }

    public void setNewState(boolean newState) {
        handle.getBooleans().write(0, newState);
    }
}
