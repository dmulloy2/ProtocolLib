package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundDebugChunkValuePacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code ChunkPos chunkPos} – the chunk position associated with this debug value</li>
 *   <li>{@code DebugSubscription.Update<?> update} – the debug update data (opaque, no ProtocolLib accessor)</li>
 * </ul>
 */
public class WrappedClientboundDebugChunkValuePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DEBUG_CHUNK_VALUE;

    public WrappedClientboundDebugChunkValuePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDebugChunkValuePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public ChunkCoordIntPair getChunkPos() {
        return handle.getChunkCoordIntPairs().read(0);
    }

    public void setChunkPos(ChunkCoordIntPair chunkPos) {
        handle.getChunkCoordIntPairs().write(0, chunkPos);
    }
}
