package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChunkBatchStartPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundChunkBatchStartPacket w = new WrappedClientboundChunkBatchStartPacket();

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, w.getHandle().getType());

        ClientboundChunkBatchStartPacket p = (ClientboundChunkBatchStartPacket) w.getHandle().getHandle();

        assertNotNull(p);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CHUNK_BATCH_START);
        container.getModifier().writeDefaults();

        WrappedClientboundChunkBatchStartPacket wrapper = new WrappedClientboundChunkBatchStartPacket(container);

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, wrapper.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CHUNK_BATCH_START);
        container.getModifier().writeDefaults();

        WrappedClientboundChunkBatchStartPacket wrapper = new WrappedClientboundChunkBatchStartPacket(container);

        assertEquals(PacketType.Play.Server.CHUNK_BATCH_START, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunkBatchStartPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
