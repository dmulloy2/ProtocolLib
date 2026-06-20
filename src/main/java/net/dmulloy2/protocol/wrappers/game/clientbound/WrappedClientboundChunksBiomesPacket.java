package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChunkBiomeData;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ClientboundChunksBiomesPacket} (game phase, clientbound).
 */
public class WrappedClientboundChunksBiomesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.CHUNKS_BIOMES;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(List.class, BukkitConverters.getListConverter(WrappedChunkBiomeData.CONVERTER));

    public WrappedClientboundChunksBiomesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundChunksBiomesPacket(List<WrappedChunkBiomeData> chunkBiomeData) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(chunkBiomeData)));
    }

    public WrappedClientboundChunksBiomesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public List<WrappedChunkBiomeData> getChunkBiomeData() {
        return handle.getLists(WrappedChunkBiomeData.CONVERTER).read(0);
    }

    public void setChunkBiomeData(List<WrappedChunkBiomeData> chunkBiomeData) {
        handle.getLists(WrappedChunkBiomeData.CONVERTER).write(0, chunkBiomeData);
    }
}
