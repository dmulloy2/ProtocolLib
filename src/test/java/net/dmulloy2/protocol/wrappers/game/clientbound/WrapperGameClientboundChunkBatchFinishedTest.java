package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundChunkBatchFinishedTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundChunkBatchFinished w = new WrapperGameClientboundChunkBatchFinished();
        w.setBatchSize(5);
        assertEquals(5, w.getBatchSize());
        assertEquals(PacketType.Play.Server.CHUNK_BATCH_FINISHED, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.CHUNK_BATCH_FINISHED);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 10);

        WrapperGameClientboundChunkBatchFinished w = new WrapperGameClientboundChunkBatchFinished(raw);
        assertEquals(10, w.getBatchSize());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundChunkBatchFinished w = new WrapperGameClientboundChunkBatchFinished();
        w.setBatchSize(1);

        new WrapperGameClientboundChunkBatchFinished(w.getHandle()).setBatchSize(20);

        assertEquals(20, w.getBatchSize());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundChunkBatchFinished(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
