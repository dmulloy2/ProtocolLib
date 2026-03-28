package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetChunkCacheRadiusPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int viewDistance} – chunk view distance radius in chunks</li>
 * </ul>
 */
public class WrapperGameClientboundViewDistance extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.VIEW_DISTANCE;

    public WrapperGameClientboundViewDistance() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperGameClientboundViewDistance(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getViewDistance() {
        return handle.getIntegers().read(0);
    }

    public void setViewDistance(int viewDistance) {
        handle.getIntegers().write(0, viewDistance);
    }
}
