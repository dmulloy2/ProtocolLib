package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundChunksBiomesPacket} (game phase, clientbound).
 */
public class WrappedClientboundChunksBiomesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHUNKS_BIOMES;

    public WrappedClientboundChunksBiomesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundChunksBiomesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // TODO: missing field 'chunkBiomeData' (NMS type: List<ClientboundChunksBiomesPacket.ChunkBiomeData>)
    //   Each ChunkBiomeData holds a ChunkPos and a raw byte[] of biome data.
    //   No ProtocolLib accessor exists; use handle.getModifier().read(0) for the raw List,
    //   or add a dedicated WrappedChunkBiomeData class with AutoWrapper.
}
