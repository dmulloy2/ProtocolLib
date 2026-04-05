package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;

/**
 * Wrapper for {@code ClientboundSetChunkCacheRadiusPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int viewDistance} – chunk view distance radius in chunks</li>
 * </ul>
 */
public class WrappedClientboundSetChunkCacheRadiusPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.VIEW_DISTANCE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class);

    public WrappedClientboundSetChunkCacheRadiusPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetChunkCacheRadiusPacket(int viewDistance) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(viewDistance)));
    }

    public WrappedClientboundSetChunkCacheRadiusPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getViewDistance() {
        return handle.getIntegers().read(0);
    }

    public void setViewDistance(int viewDistance) {
        handle.getIntegers().write(0, viewDistance);
    }
}
