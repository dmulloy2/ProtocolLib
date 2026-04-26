package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetBorderCenterPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code double x} – new border center X coordinate</li>
 *   <li>{@code double z} – new border center Z coordinate</li>
 * </ul>
 */
public class WrappedClientboundSetBorderCenterPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_BORDER_CENTER;

    public WrappedClientboundSetBorderCenterPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetBorderCenterPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedClientboundSetBorderCenterPacket(double x, double z) {
        this();
        setX(x);
        setZ(z);
    }

    public double getX() {
        return handle.getDoubles().read(0);
    }

    public void setX(double x) {
        handle.getDoubles().write(0, x);
    }

    public double getZ() {
        return handle.getDoubles().read(1);
    }

    public void setZ(double z) {
        handle.getDoubles().write(1, z);
    }
}
