package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetBorderSizePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code double diameter} – new world border diameter in blocks</li>
 * </ul>
 */
public class WrappedClientboundSetBorderSizePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_BORDER_SIZE;

    public WrappedClientboundSetBorderSizePacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundSetBorderSizePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public double getDiameter() {
        return handle.getDoubles().read(0);
    }

    public void setDiameter(double diameter) {
        handle.getDoubles().write(0, diameter);
    }
}
