package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedLevelChunkData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLightUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLightUpdatePacket w = new WrappedClientboundLightUpdatePacket(3, 7, null);

        assertEquals(PacketType.Play.Server.LIGHT_UPDATE, w.getHandle().getType());

        assertEquals(3, w.getX());
        assertEquals(7, w.getZ());
        assertEquals(null, w.getLightData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLightUpdatePacket w = new WrappedClientboundLightUpdatePacket();

        assertEquals(PacketType.Play.Server.LIGHT_UPDATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLightUpdatePacket source = new WrappedClientboundLightUpdatePacket(3, 7, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLightUpdatePacket wrapper = new WrappedClientboundLightUpdatePacket(container);

        assertEquals(3, wrapper.getX());
        assertEquals(7, wrapper.getZ());
        assertEquals(null, wrapper.getLightData());

        wrapper.setX(9);
        wrapper.setZ(-5);
        wrapper.setLightData(null);

        assertEquals(9, wrapper.getX());
        assertEquals(-5, wrapper.getZ());
        assertEquals(null, wrapper.getLightData());

        assertEquals(9, source.getX());
        assertEquals(-5, source.getZ());
        assertEquals(null, source.getLightData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLightUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
