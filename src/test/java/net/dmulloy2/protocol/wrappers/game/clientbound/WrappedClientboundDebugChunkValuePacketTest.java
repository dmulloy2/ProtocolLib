package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDebugChunkValuePacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No all-args constructor: update field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.DEBUG_CHUNK_VALUE, new WrappedClientboundDebugChunkValuePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDebugChunkValuePacket w = new WrappedClientboundDebugChunkValuePacket();
        assertEquals(PacketType.Play.Server.DEBUG_CHUNK_VALUE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.DEBUG_CHUNK_VALUE);
        WrappedClientboundDebugChunkValuePacket wrapper = new WrappedClientboundDebugChunkValuePacket(container);
        wrapper.setChunkPos(new ChunkCoordIntPair(3, -5));
        assertEquals(new ChunkCoordIntPair(3, -5), wrapper.getChunkPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDebugChunkValuePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
