package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundForgetLevelChunkPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundForgetLevelChunkPacket w = new WrappedClientboundForgetLevelChunkPacket(new ChunkCoordIntPair(3, -5));

        assertEquals(PacketType.Play.Server.UNLOAD_CHUNK, w.getHandle().getType());

        assertEquals(new ChunkCoordIntPair(3, -5), w.getPos());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundForgetLevelChunkPacket w = new WrappedClientboundForgetLevelChunkPacket();

        assertEquals(PacketType.Play.Server.UNLOAD_CHUNK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundForgetLevelChunkPacket source = new WrappedClientboundForgetLevelChunkPacket(new ChunkCoordIntPair(3, -5));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundForgetLevelChunkPacket wrapper = new WrappedClientboundForgetLevelChunkPacket(container);

        assertEquals(new ChunkCoordIntPair(3, -5), wrapper.getPos());

        wrapper.setPos(new ChunkCoordIntPair(-10, 20));

        assertEquals(new ChunkCoordIntPair(-10, 20), wrapper.getPos());

        assertEquals(new ChunkCoordIntPair(-10, 20), source.getPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundForgetLevelChunkPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
