package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetBorderLerpSizePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code double oldDiameter} – current border diameter in blocks</li>
 *   <li>{@code double newDiameter} – target border diameter in blocks</li>
 *   <li>{@code long speed} – interpolation time in milliseconds</li>
 * </ul>
 */
public class WrappedClientboundSetBorderLerpSizePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_BORDER_LERP_SIZE;

    public WrappedClientboundSetBorderLerpSizePacket() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrappedClientboundSetBorderLerpSizePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public double getOldDiameter() {
        return handle.getDoubles().read(0);
    }

    public void setOldDiameter(double oldDiameter) {
        handle.getDoubles().write(0, oldDiameter);
    }

    public double getNewDiameter() {
        return handle.getDoubles().read(1);
    }

    public void setNewDiameter(double newDiameter) {
        handle.getDoubles().write(1, newDiameter);
    }

    /** @return interpolation speed in milliseconds */
    public long getSpeed() {
        return handle.getLongs().read(0);
    }

    /** @param speed interpolation speed in milliseconds */
    public void setSpeed(long speed) {
        handle.getLongs().write(0, speed);
    }
}
