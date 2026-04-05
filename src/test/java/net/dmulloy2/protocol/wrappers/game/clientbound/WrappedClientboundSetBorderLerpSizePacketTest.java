package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderLerpSizePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetBorderLerpSizePacket w = new WrappedClientboundSetBorderLerpSizePacket(3.14, 100.0, -1L);

        assertEquals(PacketType.Play.Server.SET_BORDER_LERP_SIZE, w.getHandle().getType());

        assertEquals(3.14, w.getOldDiameter(), 1e-9);
        assertEquals(100.0, w.getNewDiameter(), 1e-9);
        assertEquals(-1L, w.getSpeed());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetBorderLerpSizePacket w = new WrappedClientboundSetBorderLerpSizePacket();

        assertEquals(PacketType.Play.Server.SET_BORDER_LERP_SIZE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetBorderLerpSizePacket source = new WrappedClientboundSetBorderLerpSizePacket(3.14, 100.0, -1L);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetBorderLerpSizePacket wrapper = new WrappedClientboundSetBorderLerpSizePacket(container);

        assertEquals(3.14, wrapper.getOldDiameter(), 1e-9);
        assertEquals(100.0, wrapper.getNewDiameter(), 1e-9);
        assertEquals(-1L, wrapper.getSpeed());

        wrapper.setOldDiameter(2.71);
        wrapper.setNewDiameter(-5.0);
        wrapper.setSpeed(0L);

        assertEquals(2.71, wrapper.getOldDiameter(), 1e-9);
        assertEquals(-5.0, wrapper.getNewDiameter(), 1e-9);
        assertEquals(0L, wrapper.getSpeed());

        assertEquals(2.71, source.getOldDiameter(), 1e-9);
        assertEquals(-5.0, source.getNewDiameter(), 1e-9);
        assertEquals(0L, source.getSpeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderLerpSizePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
