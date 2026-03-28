package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetBorderWarningDistancePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int warningDistance} – distance in blocks from the border at which the warning
 *       colour appears</li>
 * </ul>
 */
public class WrapperGameClientboundSetBorderWarningDistance extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE;

    public WrapperGameClientboundSetBorderWarningDistance() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundSetBorderWarningDistance(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** @return warning distance in blocks */
    public int getWarningDistance() {
        return handle.getIntegers().read(0);
    }

    /** @param warningDistance warning distance in blocks */
    public void setWarningDistance(int warningDistance) {
        handle.getIntegers().write(0, warningDistance);
    }
}
