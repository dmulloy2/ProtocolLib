package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetBorderWarningDelayPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int warningDelay} – time in seconds before the border warning colour appears</li>
 * </ul>
 */
public class WrapperGameClientboundSetBorderWarningDelay extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_BORDER_WARNING_DELAY;

    public WrapperGameClientboundSetBorderWarningDelay() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundSetBorderWarningDelay(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** @return warning delay in seconds */
    public int getWarningDelay() {
        return handle.getIntegers().read(0);
    }

    /** @param warningDelay warning delay in seconds */
    public void setWarningDelay(int warningDelay) {
        handle.getIntegers().write(0, warningDelay);
    }
}
