package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPingPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int id} – ping ID echoed back by the client</li>
 * </ul>
 */
public class WrappedClientboundPingPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PING;

    public WrappedClientboundPingPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundPingPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }
}
