package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLevelChunkWithLightPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLevelChunkWithLightPacket w = new WrappedClientboundLevelChunkWithLightPacket(3, 7);

        assertEquals(PacketType.Play.Server.MAP_CHUNK, w.getHandle().getType());

        assertEquals(3, w.getX());
        assertEquals(7, w.getZ());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLevelChunkWithLightPacket w = new WrappedClientboundLevelChunkWithLightPacket();

        assertEquals(PacketType.Play.Server.MAP_CHUNK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLevelChunkWithLightPacket source = new WrappedClientboundLevelChunkWithLightPacket(3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLevelChunkWithLightPacket wrapper = new WrappedClientboundLevelChunkWithLightPacket(container);

        assertEquals(3, wrapper.getX());
        assertEquals(7, wrapper.getZ());

        wrapper.setX(9);
        wrapper.setZ(-5);

        assertEquals(9, wrapper.getX());
        assertEquals(-5, wrapper.getZ());

        assertEquals(9, source.getX());
        assertEquals(-5, source.getZ());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLevelChunkWithLightPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
