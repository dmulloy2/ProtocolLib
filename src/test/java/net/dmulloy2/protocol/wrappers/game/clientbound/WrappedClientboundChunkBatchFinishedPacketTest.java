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
    void testCreate() {
        WrappedClientboundChunkBatchFinishedPacket w = new WrappedClientboundChunkBatchFinishedPacket();
        w.setBatchSize(5);

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_FINISHED, w.getHandle().getType());

        ClientboundChunkBatchFinishedPacket p = (ClientboundChunkBatchFinishedPacket) w.getHandle().getHandle();

        assertEquals(5, p.batchSize());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundChunkBatchFinishedPacket nmsPacket = new ClientboundChunkBatchFinishedPacket(10);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChunkBatchFinishedPacket wrapper = new WrappedClientboundChunkBatchFinishedPacket(container);

        assertEquals(10, wrapper.getBatchSize());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundChunkBatchFinishedPacket nmsPacket = new ClientboundChunkBatchFinishedPacket(10);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundChunkBatchFinishedPacket wrapper = new WrappedClientboundChunkBatchFinishedPacket(container);

        wrapper.setBatchSize(20);

        assertEquals(20, wrapper.getBatchSize());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunkBatchFinishedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
