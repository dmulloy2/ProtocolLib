package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerClickPacket} (game phase, serverbound).
 */
public class WrappedServerboundContainerClickPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.WINDOW_CLICK;

    public WrappedServerboundContainerClickPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundContainerClickPacket(int containerId, int stateId, short slotNum, byte buttonNum) {
        this();
        setContainerId(containerId);
        setStateId(stateId);
        setSlotNum(slotNum);
        setButtonNum(buttonNum);
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
}
