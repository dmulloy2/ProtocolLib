package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundForgetLevelChunkPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundForgetLevelChunkPacket w = new WrappedClientboundForgetLevelChunkPacket();
        w.setPos(new ChunkCoordIntPair(3, -5));

        assertEquals(PacketType.Play.Server.UNLOAD_CHUNK, w.getHandle().getType());

        ClientboundForgetLevelChunkPacket p = (ClientboundForgetLevelChunkPacket) w.getHandle().getHandle();

        assertNotNull(p.pos());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundForgetLevelChunkPacket nmsPacket = new ClientboundForgetLevelChunkPacket(
                new ChunkPos(10, 20)
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundForgetLevelChunkPacket wrapper = new WrappedClientboundForgetLevelChunkPacket(container);

        assertEquals(10, wrapper.getChunkX());
        assertEquals(20, wrapper.getChunkZ());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundForgetLevelChunkPacket nmsPacket = new ClientboundForgetLevelChunkPacket(
                new ChunkPos(10, 20)
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundForgetLevelChunkPacket wrapper = new WrappedClientboundForgetLevelChunkPacket(container);

        wrapper.setChunkX(7);

        assertEquals(7, wrapper.getChunkX());
        assertEquals(20, wrapper.getChunkZ());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundForgetLevelChunkPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
