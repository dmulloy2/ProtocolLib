package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundClearTitlesPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code boolean resetTimes} – if {@code true}, also resets fade-in/stay/fade-out times</li>
 * </ul>
 */
public class WrappedClientboundClearTitlesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CLEAR_TITLES;

    public WrappedClientboundClearTitlesPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundClearTitlesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isResetTimes() {
        return handle.getBooleans().read(0);
    }

    public void setResetTimes(boolean resetTimes) {
        handle.getBooleans().write(0, resetTimes);
    }
}
