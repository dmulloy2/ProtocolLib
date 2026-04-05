package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChunkBatchFinishedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundChunkBatchFinishedPacket w = new WrappedClientboundChunkBatchFinishedPacket(3);

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_FINISHED, w.getHandle().getType());

        ClientboundChunkBatchFinishedPacket p = (ClientboundChunkBatchFinishedPacket) w.getHandle().getHandle();

        assertEquals(3, p.batchSize());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundChunkBatchFinishedPacket w = new WrappedClientboundChunkBatchFinishedPacket();

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_FINISHED, w.getHandle().getType());

        ClientboundChunkBatchFinishedPacket p = (ClientboundChunkBatchFinishedPacket) w.getHandle().getHandle();

        assertEquals(0, p.batchSize());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundChunkBatchFinishedPacket nmsPacket = new ClientboundChunkBatchFinishedPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChunkBatchFinishedPacket wrapper = new WrappedClientboundChunkBatchFinishedPacket(container);

        assertEquals(3, wrapper.getBatchSize());

        wrapper.setBatchSize(9);

        assertEquals(9, nmsPacket.batchSize());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunkBatchFinishedPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
