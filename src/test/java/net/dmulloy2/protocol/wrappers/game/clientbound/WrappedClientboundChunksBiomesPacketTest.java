package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.WrappedChunkBiomeData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChunksBiomesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        List<WrappedChunkBiomeData> data = List.of(
                new WrappedChunkBiomeData(new ChunkCoordIntPair(1, 2), new byte[] { 1, 2, 3 }),
                new WrappedChunkBiomeData(new ChunkCoordIntPair(-3, 4), new byte[] { 4, 5 }));

        WrappedClientboundChunksBiomesPacket wrapper = new WrappedClientboundChunksBiomesPacket(data);

        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, wrapper.getHandle().getType());
        assertEquals(data, wrapper.getChunkBiomeData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundChunksBiomesPacket w = new WrappedClientboundChunksBiomesPacket();

        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new WrappedClientboundChunksBiomesPacket(List.of()).getHandle();
        WrappedClientboundChunksBiomesPacket wrapper = new WrappedClientboundChunksBiomesPacket(container);
        List<WrappedChunkBiomeData> data = List.of(
                new WrappedChunkBiomeData(new ChunkCoordIntPair(9, 10), new byte[] { 6, 7, 8 }));

        wrapper.setChunkBiomeData(data);

        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, wrapper.getHandle().getType());
        assertEquals(data, wrapper.getChunkBiomeData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunksBiomesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
