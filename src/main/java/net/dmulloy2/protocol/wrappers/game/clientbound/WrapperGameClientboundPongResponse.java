package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPongResponsePacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code long time} – pong response timestamp token</li>
 * </ul>
 */
public class WrapperGameClientboundPongResponse extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PONG_RESPONSE;

    public WrapperGameClientboundPongResponse() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundPongResponse(PacketContainer packet) {
        super(packet, TYPE);
    }

    public long getTime() {
        return handle.getLongs().read(0);
    }

    public void setTime(long time) {
        handle.getLongs().write(0, time);
    }
}
