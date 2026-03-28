package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetBorderLerpSizeTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetBorderLerpSize w = new WrapperGameClientboundSetBorderLerpSize();
        w.setOldDiameter(1000.0);
        w.setNewDiameter(500.0);
        w.setSpeed(10000L);
        assertEquals(1000.0, w.getOldDiameter(), 1e-9);
        assertEquals(500.0, w.getNewDiameter(), 1e-9);
        assertEquals(10000L, w.getSpeed());
        assertEquals(PacketType.Play.Server.SET_BORDER_LERP_SIZE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_BORDER_LERP_SIZE);
        raw.getModifier().writeDefaults();
        raw.getDoubles().write(0, 200.0);
        raw.getDoubles().write(1, 100.0);
        raw.getLongs().write(0, 5000L);

        WrapperGameClientboundSetBorderLerpSize w = new WrapperGameClientboundSetBorderLerpSize(raw);
        assertEquals(200.0, w.getOldDiameter(), 1e-9);
        assertEquals(100.0, w.getNewDiameter(), 1e-9);
        assertEquals(5000L, w.getSpeed());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetBorderLerpSize w = new WrapperGameClientboundSetBorderLerpSize();
        w.setSpeed(1000L);

        new WrapperGameClientboundSetBorderLerpSize(w.getHandle()).setSpeed(9999L);

        assertEquals(9999L, w.getSpeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetBorderLerpSize(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
