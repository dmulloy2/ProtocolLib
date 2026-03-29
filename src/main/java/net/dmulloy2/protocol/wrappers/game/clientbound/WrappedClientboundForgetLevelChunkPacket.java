package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundForgetLevelChunkPacket} (Play phase, clientbound).
 *
 * <p>Tells the client to unload the specified chunk column.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code ChunkPos pos} – chunk coordinates of the column to unload</li>
 * </ul>
 */
public class WrappedClientboundForgetLevelChunkPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UNLOAD_CHUNK;

    public WrappedClientboundForgetLevelChunkPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundForgetLevelChunkPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public ChunkCoordIntPair getPos() {
        return handle.getChunkCoordIntPairs().read(0);
    }

    public void setPos(ChunkCoordIntPair pos) {
        handle.getChunkCoordIntPairs().write(0, pos);
    }

    public int getChunkX() {
        return getPos().getChunkX();
    }

    public void setChunkX(int x) {
        setPos(new ChunkCoordIntPair(x, getChunkZ()));
    }

    public int getChunkZ() {
        return getPos().getChunkZ();
    }

    public void setChunkZ(int z) {
        setPos(new ChunkCoordIntPair(getChunkX(), z));
    }
}
