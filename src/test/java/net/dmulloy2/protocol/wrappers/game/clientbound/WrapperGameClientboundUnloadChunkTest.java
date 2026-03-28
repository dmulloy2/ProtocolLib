package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundUnloadChunkTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundUnloadChunk w = new WrapperGameClientboundUnloadChunk();
        w.setPos(new ChunkCoordIntPair(3, -5));
        assertEquals(3, w.getChunkX());
        assertEquals(-5, w.getChunkZ());
        assertEquals(PacketType.Play.Server.UNLOAD_CHUNK, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.UNLOAD_CHUNK);
        raw.getModifier().writeDefaults();
        raw.getChunkCoordIntPairs().write(0, new ChunkCoordIntPair(10, 20));

        WrapperGameClientboundUnloadChunk w = new WrapperGameClientboundUnloadChunk(raw);
        assertEquals(10, w.getChunkX());
        assertEquals(20, w.getChunkZ());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundUnloadChunk w = new WrapperGameClientboundUnloadChunk();
        w.setPos(new ChunkCoordIntPair(0, 0));

        new WrapperGameClientboundUnloadChunk(w.getHandle()).setChunkX(7);

        assertEquals(7, w.getChunkX());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundUnloadChunk(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
