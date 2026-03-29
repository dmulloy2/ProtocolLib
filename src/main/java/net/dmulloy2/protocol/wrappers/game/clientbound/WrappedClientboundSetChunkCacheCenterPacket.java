package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetChunkCacheCenterPacket} (Play phase, clientbound).
 *
 * <p>Updates the chunk from which the client's view distance is calculated.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int chunkX} – chunk X coordinate of the new centre</li>
 *   <li>{@code int chunkZ} – chunk Z coordinate of the new centre</li>
 * </ul>
 */
public class WrappedClientboundSetChunkCacheCenterPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.VIEW_CENTRE;

    public WrappedClientboundSetChunkCacheCenterPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundSetChunkCacheCenterPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getChunkX() {
        return handle.getIntegers().read(0);
    }

    public void setChunkX(int chunkX) {
        handle.getIntegers().write(0, chunkX);
    }

    public int getChunkZ() {
        return handle.getIntegers().read(1);
    }

    public void setChunkZ(int chunkZ) {
        handle.getIntegers().write(1, chunkZ);
    }
}
