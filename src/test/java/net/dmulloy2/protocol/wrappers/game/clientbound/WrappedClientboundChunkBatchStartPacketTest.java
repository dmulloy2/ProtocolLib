package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChunkBatchStartPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, new WrappedClientboundChunkBatchStartPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundChunkBatchStartPacket w = new WrappedClientboundChunkBatchStartPacket();

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CHUNK_BATCH_START);
        WrappedClientboundChunkBatchStartPacket wrapper = new WrappedClientboundChunkBatchStartPacket(container);

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunkBatchStartPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
