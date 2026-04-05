package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChunkBatchReceivedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChunkBatchReceivedPacket w = new WrappedServerboundChunkBatchReceivedPacket(0.75f);

        assertEquals(PacketType.Play.Client.CHUNK_BATCH_RECEIVED, w.getHandle().getType());

        ServerboundChunkBatchReceivedPacket p = (ServerboundChunkBatchReceivedPacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.desiredChunksPerTick(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChunkBatchReceivedPacket w = new WrappedServerboundChunkBatchReceivedPacket();

        assertEquals(PacketType.Play.Client.CHUNK_BATCH_RECEIVED, w.getHandle().getType());

        ServerboundChunkBatchReceivedPacket p = (ServerboundChunkBatchReceivedPacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.desiredChunksPerTick(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundChunkBatchReceivedPacket nmsPacket = new ServerboundChunkBatchReceivedPacket(0.75f);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChunkBatchReceivedPacket wrapper = new WrappedServerboundChunkBatchReceivedPacket(container);

        assertEquals(0.75f, wrapper.getDesiredChunksPerTick(), 1e-4f);

        wrapper.setDesiredChunksPerTick(0.25f);

        assertEquals(0.25f, nmsPacket.desiredChunksPerTick(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChunkBatchReceivedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
