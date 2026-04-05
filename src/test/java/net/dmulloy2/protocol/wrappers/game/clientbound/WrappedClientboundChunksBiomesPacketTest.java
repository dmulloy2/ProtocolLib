package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundChunksBiomesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, new WrappedClientboundChunksBiomesPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundChunksBiomesPacket w = new WrappedClientboundChunksBiomesPacket();

        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.CHUNKS_BIOMES);
        WrappedClientboundChunksBiomesPacket wrapper = new WrappedClientboundChunksBiomesPacket(container);

        assertEquals(PacketType.Play.Server.CHUNKS_BIOMES, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundChunksBiomesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
