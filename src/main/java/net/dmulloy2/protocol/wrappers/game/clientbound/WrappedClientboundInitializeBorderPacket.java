package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundInitializeBorderPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code double newCenterX}</li>
 *   <li>{@code double newCenterZ}</li>
 *   <li>{@code double oldSize}</li>
 *   <li>{@code double newSize}</li>
 *   <li>{@code long lerpTime}</li>
 *   <li>{@code int newAbsoluteMaxSize}</li>
 *   <li>{@code int warningBlocks}</li>
 *   <li>{@code int warningTime}</li>
 * </ul>
 */
public class WrappedClientboundInitializeBorderPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.INITIALIZE_BORDER;

    public WrappedClientboundInitializeBorderPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundInitializeBorderPacket(double newCenterX, double newCenterZ, double oldSize, double newSize, long lerpTime, int newAbsoluteMaxSize, int warningBlocks, int warningTime) {
        this();
        setNewCenterX(newCenterX);
        setNewCenterZ(newCenterZ);
        setOldSize(oldSize);
        setNewSize(newSize);
        setLerpTime(lerpTime);
        setNewAbsoluteMaxSize(newAbsoluteMaxSize);
        setWarningBlocks(warningBlocks);
        setWarningTime(warningTime);
    }

    public WrappedClientboundInitializeBorderPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public double getNewCenterX() {
        return handle.getDoubles().read(0);
    }

    public void setNewCenterX(double newCenterX) {
        handle.getDoubles().write(0, newCenterX);
    }

    public double getNewCenterZ() {
        return handle.getDoubles().read(1);
    }

    public void setNewCenterZ(double newCenterZ) {
        handle.getDoubles().write(1, newCenterZ);
    }

    public double getOldSize() {
        return handle.getDoubles().read(2);
    }

    public void setOldSize(double oldSize) {
        handle.getDoubles().write(2, oldSize);
    }

    public double getNewSize() {
        return handle.getDoubles().read(3);
    }

    public void setNewSize(double newSize) {
        handle.getDoubles().write(3, newSize);
    }

    public long getLerpTime() {
        return handle.getLongs().read(0);
    }

    public void setLerpTime(long lerpTime) {
        handle.getLongs().write(0, lerpTime);
    }

    public int getNewAbsoluteMaxSize() {
        return handle.getIntegers().read(0);
    }

    public void setNewAbsoluteMaxSize(int newAbsoluteMaxSize) {
        handle.getIntegers().write(0, newAbsoluteMaxSize);
    }

    public int getWarningBlocks() {
        return handle.getIntegers().read(1);
    }

    public void setWarningBlocks(int warningBlocks) {
        handle.getIntegers().write(1, warningBlocks);
    }

    public int getWarningTime() {
        return handle.getIntegers().read(2);
    }

    public void setWarningTime(int warningTime) {
        handle.getIntegers().write(2, warningTime);
    }
}
