package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerClickPacket} (game phase, serverbound).
 *
 * <p>NMS field order: {@code containerId, stateId, slotNum, buttonNum, containerInput (global 4),
 * changedSlots (global 5), carriedItem (global 6)}
 */
public class WrappedServerboundContainerClickPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.WINDOW_CLICK;

    /**
     * Mirrors {@code ContainerInput} enum. Constants must match NMS names exactly.
     * Global field index 4.
     */
    public enum ContainerInput {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL
    }

    public WrappedServerboundContainerClickPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundContainerClickPacket(int containerId, int stateId, short slotNum,
            byte buttonNum, ContainerInput containerInput) {
        this();
        setContainerId(containerId);
        setStateId(stateId);
        setSlotNum(slotNum);
        setButtonNum(buttonNum);
        setContainerInput(containerInput);
    }

    public WrappedServerboundContainerClickPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public int getStateId() {
        return handle.getIntegers().read(1);
    }

    public void setStateId(int stateId) {
        handle.getIntegers().write(1, stateId);
    }

    public short getSlotNum() {
        return handle.getShorts().read(0);
    }

    public void setSlotNum(short slotNum) {
        handle.getShorts().write(0, slotNum);
    }

    public byte getButtonNum() {
        return handle.getBytes().read(0);
    }

    public void setButtonNum(byte buttonNum) {
        handle.getBytes().write(0, buttonNum);
    }

    /** Returns the inventory action type. Global field index 4. */
    public ContainerInput getContainerInput() {
        return handle.getEnumModifier(ContainerInput.class, 4).read(0);
    }

    public void setContainerInput(ContainerInput containerInput) {
        handle.getEnumModifier(ContainerInput.class, 4).write(0, containerInput);
    }

    // TODO: missing field 'changedSlots' (NMS type: Int2ObjectMap<HashedStack>, global index 5)
    //   HashedStack wraps an ItemStack with a hash; no ProtocolLib accessor exists.
    //   Use handle.getModifier().read(5) for the raw Int2ObjectMap.
    // TODO: missing field 'carriedItem' (NMS type: HashedStack — the item on the cursor, global index 6)
    //   Use handle.getModifier().read(6) for the raw HashedStack.
}
